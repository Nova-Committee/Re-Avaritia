package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Paletted BlockState snapshot of a captured Neutron Ring volume, regenerated from StructureTemplate NBT. */
public record NeutronSpacePreview(int sizeX, int sizeY, int sizeZ, int blocks, List<BlockState> palette,
                                  int[] positions, int[] states, List<BlockEntityData> blockEntities) {
    public static final int MAX_CELLS = NeutronRingContents.MAX_XZ * NeutronRingContents.MAX_Y * NeutronRingContents.MAX_XZ;
    public static final int CELLS_PER_CHUNK = 8192;
    public static final NeutronSpacePreview EMPTY = new NeutronSpacePreview(1, 1, 1, 0, List.of(), new int[0], new int[0], List.of());

    public record BlockEntityData(int packedPos, CompoundTag updateTag) {
        public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BlockEntityData::packedPos,
                ByteBufCodecs.COMPOUND_TAG, BlockEntityData::updateTag,
                BlockEntityData::new);
    }

    public record Meta(int sizeX, int sizeY, int sizeZ, int blocks) {
        public static final Meta EMPTY = new Meta(1, 1, 1, 0);
    }

    private static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE =
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);

    public NeutronSpacePreview {
        sizeX = Mth.clamp(sizeX, 1, NeutronRingContents.MAX_XZ);
        sizeY = Mth.clamp(sizeY, 1, NeutronRingContents.MAX_Y);
        sizeZ = Mth.clamp(sizeZ, 1, NeutronRingContents.MAX_XZ);
        blocks = Math.max(0, Math.min(blocks, MAX_CELLS));
        palette = List.copyOf(palette);
        blockEntities = List.copyOf(blockEntities);
        int count = Math.min(MAX_CELLS, Math.min(positions.length, states.length));
        if (positions.length != count) {
            positions = Arrays.copyOf(positions, count);
        }
        if (states.length != count) {
            states = Arrays.copyOf(states, count);
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, NeutronSpacePreview> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.VAR_INT.encode(buf, data.sizeX());
                ByteBufCodecs.VAR_INT.encode(buf, data.sizeY());
                ByteBufCodecs.VAR_INT.encode(buf, data.sizeZ());
                ByteBufCodecs.VAR_INT.encode(buf, data.blocks());
                ByteBufCodecs.VAR_INT.encode(buf, data.palette().size());
                for (BlockState state : data.palette()) {
                    BLOCK_STATE.encode(buf, state);
                }
                int count = Math.min(data.positions().length, data.states().length);
                ByteBufCodecs.VAR_INT.encode(buf, count);
                for (int i = 0; i < count; i++) {
                    ByteBufCodecs.VAR_INT.encode(buf, data.positions()[i]);
                    ByteBufCodecs.VAR_INT.encode(buf, data.states()[i]);
                }
                ByteBufCodecs.VAR_INT.encode(buf, data.blockEntities().size());
                for (BlockEntityData entity : data.blockEntities()) {
                    BlockEntityData.STREAM_CODEC.encode(buf, entity);
                }
            },
            buf -> {
                int sizeX = ByteBufCodecs.VAR_INT.decode(buf);
                int sizeY = ByteBufCodecs.VAR_INT.decode(buf);
                int sizeZ = ByteBufCodecs.VAR_INT.decode(buf);
                int blocks = ByteBufCodecs.VAR_INT.decode(buf);
                int paletteSize = readCount(buf);
                List<BlockState> palette = new ArrayList<>(paletteSize);
                for (int i = 0; i < paletteSize; i++) {
                    palette.add(BLOCK_STATE.decode(buf));
                }
                int count = readCount(buf);
                int[] positions = new int[count];
                int[] states = new int[count];
                for (int i = 0; i < count; i++) {
                    positions[i] = ByteBufCodecs.VAR_INT.decode(buf);
                    states[i] = ByteBufCodecs.VAR_INT.decode(buf);
                }
                int entityCount = readCount(buf);
                List<BlockEntityData> entities = new ArrayList<>(entityCount);
                for (int i = 0; i < entityCount; i++) {
                    entities.add(BlockEntityData.STREAM_CODEC.decode(buf));
                }
                return new NeutronSpacePreview(sizeX, sizeY, sizeZ, blocks, palette, positions, states, entities);
            });

    private static int readCount(RegistryFriendlyByteBuf buf) {
        int count = ByteBufCodecs.VAR_INT.decode(buf);
        if (count < 0 || count > MAX_CELLS) {
            throw new DecoderException("Neutron preview count outside captured volume: " + count);
        }
        return count;
    }

    public static int pack(int x, int y, int z) {
        return x | z << 4 | y << 8;
    }

    public static int unpackX(int packed) {
        return packed & 0xF;
    }

    public static int unpackZ(int packed) {
        return packed >> 4 & 0xF;
    }

    public static int unpackY(int packed) {
        return packed >> 8 & 0x1FF;
    }

    public static Meta meta(CompoundTag template) {
        SizeBounds size = sizeOf(template);
        ListTag paletteTag = paletteTag(template);
        ListTag blocksTag = template.getList("blocks", Tag.TAG_COMPOUND);
        boolean[] air = new boolean[paletteTag.size()];
        var blockLookup = BuiltInRegistries.BLOCK.asLookup();
        for (int i = 0; i < paletteTag.size(); i++) {
            air[i] = NbtUtils.readBlockState(blockLookup, paletteTag.getCompound(i)).isAir();
        }
        int count = 0;
        int limit = Math.min(MAX_CELLS, blocksTag.size());
        for (int i = 0; i < limit; i++) {
            CompoundTag block = blocksTag.getCompound(i);
            int index = block.getInt("state");
            if (index < 0 || index >= air.length || air[index]) {
                continue;
            }
            ListTag pos = block.getList("pos", Tag.TAG_INT);
            if (pos.size() < 3) {
                continue;
            }
            int x = pos.getInt(0);
            int y = pos.getInt(1);
            int z = pos.getInt(2);
            if (x < 0 || y < 0 || z < 0 || x >= size.sizeX || y >= size.sizeY || z >= size.sizeZ) {
                continue;
            }
            count++;
        }
        return new Meta(size.sizeX, size.sizeY, size.sizeZ, count);
    }

    public static NeutronSpacePreview fromTemplate(CompoundTag template, HolderLookup.Provider registries) {
        if (template == null || template.isEmpty()) {
            return EMPTY;
        }
        SizeBounds size = sizeOf(template);
        ListTag paletteTag = paletteTag(template);
        var blocks = registries.lookupOrThrow(Registries.BLOCK);
        List<BlockState> sourcePalette = new ArrayList<>(paletteTag.size());
        for (int i = 0; i < paletteTag.size(); i++) {
            sourcePalette.add(NbtUtils.readBlockState(blocks, paletteTag.getCompound(i)));
        }
        ListTag blocksTag = template.getList("blocks", Tag.TAG_COMPOUND);
        Map<BlockState, Integer> index = new HashMap<>();
        List<BlockState> palette = new ArrayList<>();
        int[] positions = new int[Math.min(MAX_CELLS, blocksTag.size())];
        int[] states = new int[positions.length];
        List<BlockEntityData> entities = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < blocksTag.size() && count < MAX_CELLS; i++) {
            CompoundTag block = blocksTag.getCompound(i);
            int sourceIndex = block.getInt("state");
            if (sourceIndex < 0 || sourceIndex >= sourcePalette.size()) {
                continue;
            }
            BlockState state = sourcePalette.get(sourceIndex);
            if (state.isAir()) {
                continue;
            }
            ListTag posTag = block.getList("pos", Tag.TAG_INT);
            if (posTag.size() < 3) {
                continue;
            }
            int x = posTag.getInt(0);
            int y = posTag.getInt(1);
            int z = posTag.getInt(2);
            if (x < 0 || y < 0 || z < 0 || x >= size.sizeX || y >= size.sizeY || z >= size.sizeZ) {
                continue;
            }
            int packed = pack(x, y, z);
            int paletteIndex = index.computeIfAbsent(state, ignored -> {
                palette.add(state);
                return palette.size() - 1;
            });
            positions[count] = packed;
            states[count] = paletteIndex;
            count++;
            if (block.contains("nbt", Tag.TAG_COMPOUND)) {
                CompoundTag update = clientUpdateTag(new BlockPos(x, y, z), state, block.getCompound("nbt"), registries);
                if (update != null && !update.isEmpty()) {
                    entities.add(new BlockEntityData(packed, update));
                }
            }
        }
        return new NeutronSpacePreview(size.sizeX, size.sizeY, size.sizeZ, count, palette,
                Arrays.copyOf(positions, count), Arrays.copyOf(states, count), entities);
    }

    public BlockState stateAt(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ) {
            return Blocks.AIR.defaultBlockState();
        }
        int packed = pack(x, y, z);
        int count = Math.min(positions.length, states.length);
        for (int i = 0; i < count; i++) {
            if (positions[i] == packed) {
                int index = states[i];
                return index >= 0 && index < palette.size() ? palette.get(index) : Blocks.AIR.defaultBlockState();
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    public NeutronSpacePreview slice(int from, int to) {
        int count = Math.min(positions.length, states.length);
        from = Mth.clamp(from, 0, count);
        to = Mth.clamp(to, from, count);
        List<BlockEntityData> entities = List.of();
        if (from < to && !blockEntities.isEmpty()) {
            IntOpenHashSet included = new IntOpenHashSet(positions, from, to - from);
            entities = new ArrayList<>();
            for (BlockEntityData entity : blockEntities) {
                if (included.contains(entity.packedPos())) {
                    entities.add(entity);
                }
            }
        }
        return new NeutronSpacePreview(sizeX, sizeY, sizeZ, blocks, palette,
                Arrays.copyOfRange(positions, from, to), Arrays.copyOfRange(states, from, to),
                entities);
    }

    public static NeutronSpacePreview assemble(NeutronSpacePreview header, List<NeutronSpacePreview> chunks) {
        int total = 0;
        int entityCount = 0;
        for (NeutronSpacePreview chunk : chunks) {
            total += Math.min(chunk.positions().length, chunk.states().length);
            entityCount += chunk.blockEntities().size();
        }
        total = Math.min(MAX_CELLS, total);
        int[] positions = new int[total];
        int[] states = new int[total];
        List<BlockEntityData> entities = new ArrayList<>(entityCount);
        int cursor = 0;
        for (NeutronSpacePreview chunk : chunks) {
            int count = Math.min(chunk.positions().length, chunk.states().length);
            int copy = Math.min(count, total - cursor);
            System.arraycopy(chunk.positions(), 0, positions, cursor, copy);
            System.arraycopy(chunk.states(), 0, states, cursor, copy);
            for (BlockEntityData entity : chunk.blockEntities()) {
                entities.add(entity);
            }
            cursor += copy;
            if (cursor >= total) {
                break;
            }
        }
        return new NeutronSpacePreview(header.sizeX, header.sizeY, header.sizeZ, header.blocks, header.palette,
                positions, states, entities);
    }

    @Nullable
    private static CompoundTag clientUpdateTag(BlockPos pos, BlockState state, CompoundTag templateNbt,
                                               HolderLookup.Provider registries) {
        try {
            BlockEntity blockEntity = BlockEntity.loadStatic(pos, state, templateNbt.copy(), registries);
            if (blockEntity == null) {
                return null;
            }
            return blockEntity.getUpdateTag(registries);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static ListTag paletteTag(CompoundTag template) {
        if (template.contains("palettes", Tag.TAG_LIST)) {
            ListTag palettes = template.getList("palettes", Tag.TAG_LIST);
            return palettes.isEmpty() ? new ListTag() : palettes.getList(0);
        }
        return template.getList("palette", Tag.TAG_COMPOUND);
    }

    private static SizeBounds sizeOf(CompoundTag template) {
        ListTag size = template.getList("size", Tag.TAG_INT);
        int sizeX = size.size() > 0 ? size.getInt(0) : 1;
        int sizeY = size.size() > 1 ? size.getInt(1) : 1;
        int sizeZ = size.size() > 2 ? size.getInt(2) : 1;
        return new SizeBounds(
                Mth.clamp(sizeX, 1, NeutronRingContents.MAX_XZ),
                Mth.clamp(sizeY, 1, NeutronRingContents.MAX_Y),
                Mth.clamp(sizeZ, 1, NeutronRingContents.MAX_XZ));
    }

    private record SizeBounds(int sizeX, int sizeY, int sizeZ) {
    }
}
