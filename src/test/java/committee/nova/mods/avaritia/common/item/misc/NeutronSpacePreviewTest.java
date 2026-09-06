package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeutronSpacePreviewTest {
    @Test
    void fromTemplateKeepsStatesAndAirGaps() {
        BlockState stairs = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        CompoundTag template = structure(
                2, 3, 1,
                List.of(Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), stairs),
                cell(0, 0, 0, 0),
                cell(0, 1, 0, 1),
                cell(0, 2, 0, 0),
                cell(1, 0, 0, 2));

        NeutronSpacePreview preview = NeutronSpacePreview.fromTemplate(template, registries());
        assertEquals(2, preview.sizeX());
        assertEquals(3, preview.sizeY());
        assertEquals(1, preview.sizeZ());
        assertEquals(3, preview.blocks());
        assertEquals(Blocks.STONE.defaultBlockState(), preview.stateAt(0, 0, 0));
        assertTrue(preview.stateAt(0, 1, 0).isAir());
        assertEquals(Blocks.STONE.defaultBlockState(), preview.stateAt(0, 2, 0));
        assertEquals(stairs, preview.stateAt(1, 0, 0));
        assertTrue(preview.stateAt(1, 1, 0).isAir());
    }

    @Test
    void codecRoundTripKeepsPackedCells() {
        BlockState stairs = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        NeutronSpacePreview preview = NeutronSpacePreview.fromTemplate(structure(
                2, 2, 1,
                List.of(Blocks.STONE.defaultBlockState(), stairs),
                cell(0, 0, 0, 0),
                cell(1, 1, 0, 1)), registries());

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries());
        try {
            NeutronSpacePreview.STREAM_CODEC.encode(buffer, preview);
            NeutronSpacePreview decoded = NeutronSpacePreview.STREAM_CODEC.decode(buffer);
            assertEquals(preview.sizeX(), decoded.sizeX());
            assertEquals(preview.sizeY(), decoded.sizeY());
            assertEquals(preview.sizeZ(), decoded.sizeZ());
            assertEquals(preview.blocks(), decoded.blocks());
            assertEquals(preview.stateAt(0, 0, 0), decoded.stateAt(0, 0, 0));
            assertTrue(decoded.stateAt(0, 1, 0).isAir());
            assertEquals(stairs, decoded.stateAt(1, 1, 0));
        } finally {
            buffer.release();
        }
    }

    @Test
    void metaCountsNonAirCellsFromOldTemplates() {
        NeutronSpacePreview.Meta meta = NeutronSpacePreview.meta(structure(
                1, 2, 1,
                List.of(Blocks.GLASS.defaultBlockState(), Blocks.AIR.defaultBlockState()),
                cell(0, 0, 0, 0),
                cell(0, 1, 0, 1)));
        assertEquals(1, meta.sizeX());
        assertEquals(2, meta.sizeY());
        assertEquals(1, meta.sizeZ());
        assertEquals(1, meta.blocks());
    }

    @Test
    void deselectRestoresDefaultCaptureSize() {
        NeutronRingContents selected = NeutronRingContents.owned(UUID.randomUUID())
                .select("space", new NeutronRingContents.Size(1, 2, 3));
        assertEquals(1, selected.size().x());
        NeutronRingContents cleared = selected.deselect();
        assertEquals(NeutronRingContents.Size.DEFAULT, cleared.size());
        assertTrue(cleared.selectedId().isEmpty());
    }

    @Test
    void chunkedTransferKeepsTheFullCaptureHeight() {
        int[] positions = new int[NeutronSpacePreview.MAX_CELLS];
        int[] states = new int[positions.length];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = NeutronSpacePreview.pack(i & 15, i >> 8, i >> 4 & 15);
        }
        BlockState stairs = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        states[states.length - 1] = 1;
        NeutronSpacePreview full = new NeutronSpacePreview(16, 384, 16, positions.length,
                List.of(Blocks.STONE.defaultBlockState(), stairs), positions, states, List.of());
        List<NeutronSpacePreview> received = new ArrayList<>();
        for (int offset = 0; offset < positions.length; offset += NeutronSpacePreview.CELLS_PER_CHUNK) {
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries());
            try {
                NeutronSpacePreview.STREAM_CODEC.encode(buffer, full.slice(offset, offset + NeutronSpacePreview.CELLS_PER_CHUNK));
                received.add(NeutronSpacePreview.STREAM_CODEC.decode(buffer));
            } finally {
                buffer.release();
            }
        }
        NeutronSpacePreview assembled = NeutronSpacePreview.assemble(received.getFirst(), received);
        assertEquals(Blocks.STONE.defaultBlockState(), assembled.stateAt(0, 0, 0));
        assertEquals(Blocks.STONE.defaultBlockState(), assembled.stateAt(0, 256, 0));
        assertEquals(stairs, assembled.stateAt(15, 383, 15));
    }

    @Test
    void chunkedTransferKeepsBlockEntityDataWithItsCells() {
        int[] positions = new int[2 * NeutronSpacePreview.CELLS_PER_CHUNK + 1];
        int[] states = new int[positions.length];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = NeutronSpacePreview.MAX_CELLS - 1 - i;
        }
        int[] entityOffsets = {0, NeutronSpacePreview.CELLS_PER_CHUNK - 1,
                NeutronSpacePreview.CELLS_PER_CHUNK, positions.length - 2, positions.length - 1};
        Map<Integer, CompoundTag> expected = new HashMap<>();
        List<NeutronSpacePreview.BlockEntityData> entities = new ArrayList<>();
        for (int offset : entityOffsets) {
            states[offset] = 1;
            CompoundTag tag = new CompoundTag();
            tag.putString("CustomName", "{\"text\":\"Preview " + positions[offset] + "\"}");
            expected.put(positions[offset], tag);
            entities.add(new NeutronSpacePreview.BlockEntityData(positions[offset], tag));
        }
        Collections.reverse(entities);
        NeutronSpacePreview full = new NeutronSpacePreview(16, 384, 16, positions.length,
                List.of(Blocks.STONE.defaultBlockState(), Blocks.OAK_SIGN.defaultBlockState()),
                positions, states, entities);
        List<NeutronSpacePreview> received = new ArrayList<>();
        for (int from = 0; from < positions.length; from += NeutronSpacePreview.CELLS_PER_CHUNK) {
            int to = Math.min(positions.length, from + NeutronSpacePreview.CELLS_PER_CHUNK);
            Map<Integer, CompoundTag> expectedChunk = new HashMap<>();
            for (int i = from; i < to; i++) {
                if (expected.containsKey(positions[i])) {
                    expectedChunk.put(positions[i], expected.get(positions[i]));
                }
            }
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries());
            try {
                NeutronSpacePreview.STREAM_CODEC.encode(buffer, full.slice(from, to));
                NeutronSpacePreview decoded = NeutronSpacePreview.STREAM_CODEC.decode(buffer);
                assertEquals(expectedChunk, decoded.blockEntities().stream().collect(Collectors.toMap(
                        NeutronSpacePreview.BlockEntityData::packedPos, NeutronSpacePreview.BlockEntityData::updateTag)),
                        "Block entity tags must travel with their cells, regardless of list or coordinate order");
                received.add(decoded);
            } finally {
                buffer.release();
            }
        }
        NeutronSpacePreview assembled = NeutronSpacePreview.assemble(received.getFirst(), received);
        assertEquals(expected, assembled.blockEntities().stream().collect(Collectors.toMap(
                NeutronSpacePreview.BlockEntityData::packedPos, NeutronSpacePreview.BlockEntityData::updateTag)));
        assertTrue(full.slice(0, 0).blockEntities().isEmpty(), "An empty cell slice must not carry block entity data");
    }

    private static RegistryAccess registries() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    private static CompoundTag structure(int x, int y, int z, List<BlockState> palette, CompoundTag... cells) {
        CompoundTag template = new CompoundTag();
        ListTag size = new ListTag();
        size.add(IntTag.valueOf(x));
        size.add(IntTag.valueOf(y));
        size.add(IntTag.valueOf(z));
        template.put("size", size);
        ListTag paletteTag = new ListTag();
        for (BlockState state : palette) {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }
        template.put("palette", paletteTag);
        ListTag blocks = new ListTag();
        for (CompoundTag cell : cells) {
            blocks.add(cell);
        }
        template.put("blocks", blocks);
        return template;
    }

    private static CompoundTag cell(int x, int y, int z, int state) {
        CompoundTag tag = new CompoundTag();
        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf(x));
        pos.add(IntTag.valueOf(y));
        pos.add(IntTag.valueOf(z));
        tag.put("pos", pos);
        tag.putInt("state", state);
        return tag;
    }
}
