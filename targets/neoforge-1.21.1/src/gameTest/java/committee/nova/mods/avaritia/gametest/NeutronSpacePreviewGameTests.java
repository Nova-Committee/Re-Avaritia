package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSavedData;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSpaces;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreviewLevel;
import committee.nova.mods.avaritia.common.net.C2SNeutronRingPack;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeutronSpacePreviewGameTests {
    private static final String TEMPLATE = "portable_ui_empty";

    private NeutronSpacePreviewGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void capturedPreviewPreservesStates(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AvaritiaGT"));
        player.setGameMode(GameType.CREATIVE);
        player.getInventory().clearContent();
        ItemStack ring = new ItemStack(ModItems.neutron_ring.get());

        player.getInventory().setItem(0, ring);
        BlockPos minRel = new BlockPos(1, 1, 1);
        helper.setBlock(minRel, Blocks.STONE);
        helper.setBlock(minRel.offset(1, 0, 0), Blocks.GRASS_BLOCK);
        helper.setBlock(minRel.offset(2, 0, 0),
                Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        helper.setBlock(minRel.offset(3, 0, 0),
                Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        helper.setBlock(minRel.offset(0, 0, 1), Blocks.OAK_FENCE);
        helper.setBlock(minRel.offset(1, 0, 1), Blocks.GLASS);
        helper.setBlock(minRel.offset(2, 0, 1), Blocks.WATER);
        helper.setBlock(minRel.offset(3, 0, 3), Blocks.LAVA);
        BlockPos chestRel = minRel.offset(0, 0, 2);
        helper.setBlock(chestRel, Blocks.CHEST);
        helper.setBlock(minRel.offset(0, 2, 0), Blocks.GLASS);
        BlockPos signRel = minRel.offset(1, 0, 2);
        helper.setBlock(signRel.below(), Blocks.STONE);
        helper.setBlock(signRel, Blocks.OAK_SIGN);
        SignBlockEntity sign = helper.getBlockEntity(signRel);
        sign.setText(sign.getFrontText().setMessage(0, Component.literal("Preview sign")), true);

        BlockState stone = helper.getBlockState(minRel);
        BlockState grass = helper.getBlockState(minRel.offset(1, 0, 0));
        BlockState slab = helper.getBlockState(minRel.offset(2, 0, 0));
        BlockState log = helper.getBlockState(minRel.offset(3, 0, 0));
        BlockState fence = helper.getBlockState(minRel.offset(0, 0, 1));
        BlockState glass = helper.getBlockState(minRel.offset(1, 0, 1));
        BlockState water = helper.getBlockState(minRel.offset(2, 0, 1));
        BlockState lava = helper.getBlockState(minRel.offset(3, 0, 3));
        BlockState chestState = helper.getBlockState(chestRel);

        NeutronRingContents bound = NeutronRingItem.bind(player, ring);
        UUID library = bound.storageId();
        NeutronRingContents.Size captureSize = new NeutronRingContents.Size(8, 4, 8);
        BlockPos baseAbs = helper.absolutePos(minRel.offset(4, 0, 4));
        CompoundTag template = NeutronRingSpaces.capture(helper.getLevel(), baseAbs, captureSize);
        NeutronRingSavedData store = NeutronRingSavedData.get(player.server);
        helper.assertTrue(store.add(library, "preview-fixture", template), "fixture should own the captured space");
        NeutronRingSavedData.SpaceInfo info = store.list(library).getFirst();
        String id = info.id();
        String selectedId = null;
        try {
            NeutronSpacePreview.Meta meta = NeutronSpacePreview.meta(template);
            helper.assertTrue(info.sizeX() == meta.sizeX() && info.sizeY() == meta.sizeY()
                            && info.sizeZ() == meta.sizeZ() && info.blocks() == meta.blocks(),
                    "SavedData metadata should match the captured template");

            NeutronSpacePreview preview = NeutronSpacePreview.fromTemplate(template, helper.getLevel().registryAccess());
            helper.assertTrue(preview.sizeX() == meta.sizeX() && preview.sizeY() == meta.sizeY()
                            && preview.sizeZ() == meta.sizeZ() && preview.blocks() == meta.blocks(),
                    "detailed preview should preserve capture metadata");
            helper.assertTrue(preview.stateAt(0, 0, 0).equals(stone), "stone");
            helper.assertTrue(preview.stateAt(1, 0, 0).equals(grass), "grass");
            helper.assertTrue(preview.stateAt(2, 0, 0).equals(slab), "slab");
            helper.assertTrue(preview.stateAt(3, 0, 0).equals(log), "rotated log");
            helper.assertTrue(preview.stateAt(0, 0, 1).equals(fence), "fence");
            helper.assertTrue(preview.stateAt(1, 0, 1).equals(glass), "glass");
            helper.assertTrue(preview.stateAt(2, 0, 1).equals(water), "source water");
            helper.assertTrue(preview.stateAt(3, 0, 3).equals(lava), "source lava");
            helper.assertTrue(preview.stateAt(0, 0, 2).equals(chestState), "chest");
            helper.assertTrue(preview.stateAt(1, 0, 2).is(Blocks.OAK_SIGN), "sign");
            helper.assertTrue(water.is(Blocks.WATER) && water.getFluidState().isSource(), "fixture source water");
            helper.assertTrue(lava.is(Blocks.LAVA) && lava.getFluidState().isSource(), "fixture source lava");

            List<NeutronSpacePreview> chunks = new ArrayList<>();
            int total = Math.min(preview.positions().length, preview.states().length);
            int chunkCount = Math.max(1, (total + NeutronSpacePreview.CELLS_PER_CHUNK - 1) / NeutronSpacePreview.CELLS_PER_CHUNK);
            for (int i = 0; i < chunkCount; i++) {
                int from = i * NeutronSpacePreview.CELLS_PER_CHUNK;
                int to = Math.min(total, from + NeutronSpacePreview.CELLS_PER_CHUNK);
                RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
                try {
                    NeutronSpacePreview.STREAM_CODEC.encode(buffer, preview.slice(from, to));
                    chunks.add(NeutronSpacePreview.STREAM_CODEC.decode(buffer));
                } finally {
                    buffer.release();
                }
            }
            NeutronSpacePreview assembled = NeutronSpacePreview.assemble(chunks.getFirst(), chunks);
            helper.assertTrue(assembled.stateAt(0, 0, 0).equals(stone), "chunked stone");
            helper.assertTrue(assembled.stateAt(0, 0, 2).equals(chestState), "chunked chest");
            helper.assertTrue(assembled.stateAt(3, 0, 0).equals(log), "chunked rotated log");
            helper.assertTrue(assembled.stateAt(2, 0, 0).equals(slab), "chunked slab");
            helper.assertTrue(assembled.stateAt(2, 0, 1).equals(water), "chunked water");
            helper.assertTrue(assembled.stateAt(3, 0, 3).equals(lava), "chunked lava");
            helper.assertTrue(assembled.stateAt(0, 1, 0).isAir()
                            && assembled.stateAt(0, 2, 0).is(Blocks.GLASS),
                    "separated vertical layers must not become solid columns");
            NeutronSpacePreviewLevel decoded = new NeutronSpacePreviewLevel(
                    helper.getLevel(), helper.absolutePos(minRel), assembled);
            helper.assertTrue(decoded.getBlockEntity(new BlockPos(1, 0, 2)) instanceof SignBlockEntity decodedSign
                            && decodedSign.getFrontText().getMessage(0, false).getString().equals("Preview sign"),
                    "sign text must survive capture, chunk encoding and preview reconstruction");

            helper.assertTrue(store.add(library, "selected-fixture", template), "fixture should own a separate selected entry");
            selectedId = store.list(library).stream().filter(entry -> !entry.id().equals(id))
                    .findFirst().orElseThrow().id();
            GlobalPos place = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(new BlockPos(10, 1, 10)));
            ring.set(ModDataComponents.NEUTRON_RING.get(),
                    NeutronRingItem.contents(ring).select(selectedId, NeutronRingSpaces.sizeOf(template)).withPlace(place));
            NeutronRingContents beforeBrowse = NeutronRingItem.contents(ring);
            C2SNeutronRingPack request = new C2SNeutronRingPack(
                    C2SNeutronRingPack.PREVIEW, id, "", 0, library, NeutronRingSpaces.sizeOf(template));
            new C2SNeutronRingPack.Handler().handle(request,
                    new ServerPayloadContext(player.connection, C2SNeutronRingPack.TYPE.id()));
            NeutronRingContents afterBrowse = NeutronRingItem.contents(ring);
            helper.assertTrue(beforeBrowse.selectedId().equals(afterBrowse.selectedId()),
                    "browsing preview must not change selected id");
            helper.assertTrue(beforeBrowse.placeBase().equals(afterBrowse.placeBase()),
                    "browsing preview must not change selected placement");
            helper.assertTrue(beforeBrowse.size().equals(afterBrowse.size()),
                    "browsing preview must not change selected size");
        } finally {
            store.remove(library, id);
            if (selectedId != null) {
                store.remove(library, selectedId);
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void adjacentDoubleChestIsolatedFromWorld(GameTestHelper helper) {
        BlockPos rightPos = new BlockPos(1, 0, 1);
        BlockPos leftPos = new BlockPos(2, 0, 1);
        BlockState worldRightBefore = helper.getLevel().getBlockState(rightPos);
        BlockState worldLeftBefore = helper.getLevel().getBlockState(leftPos);
        helper.assertFalse(worldRightBefore.is(Blocks.CHEST) || worldLeftBefore.is(Blocks.CHEST),
                "flat-world fixture should not already contain chests at snapshot-local coordinates");

        BlockState right = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.SOUTH)
                .setValue(ChestBlock.TYPE, ChestType.RIGHT);
        BlockState left = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.SOUTH)
                .setValue(ChestBlock.TYPE, ChestType.LEFT);
        NeutronSpacePreview preview = new NeutronSpacePreview(
                4, 2, 4, 2, List.of(right, left),
                new int[]{NeutronSpacePreview.pack(1, 0, 1), NeutronSpacePreview.pack(2, 0, 1)},
                new int[]{0, 1}, List.of());
        NeutronSpacePreviewLevel snapshot = new NeutronSpacePreviewLevel(
                helper.getLevel(), helper.absolutePos(BlockPos.ZERO), preview);

        helper.assertTrue(snapshot.getBlockState(rightPos).equals(right), "snapshot right chest");
        helper.assertTrue(snapshot.getBlockState(leftPos).equals(left), "snapshot left chest");
        helper.assertTrue(snapshot.getBlockState(new BlockPos(3, 0, 1)).isAir(), "unlisted snapshot cell is air");
        helper.assertTrue(snapshot.getBlockState(new BlockPos(8, 0, 1)).isAir(), "outside snapshot is air");
        helper.assertTrue(snapshot.getBlockEntity(new BlockPos(8, 0, 1)) == null, "outside snapshot has no BE");
        helper.assertTrue(snapshot.getMinBuildHeight() == 0 && snapshot.getHeight() == 2,
                "snapshot build bounds follow the capture");
        helper.assertFalse(snapshot.hasChunk(0, 0) || snapshot.isLoaded(rightPos),
                "a chunkless snapshot must not advertise loaded chunks");
        helper.assertTrue(snapshot.getChunk(0, 0, ChunkStatus.FULL, false) == null,
                "optional chunk lookup must report unavailable");
        assertUnsupported(() -> snapshot.getChunk(0, 0));
        assertUnsupported(() -> snapshot.getFreeMapId());
        assertUnsupported(() -> snapshot.explode(null, 1.5, 0.0, 1.5, 1.0F, Level.ExplosionInteraction.BLOCK));
        helper.assertFalse(snapshot.setBlock(rightPos, Blocks.DIRT.defaultBlockState(), 3),
                "preview state must reject world writes");

        helper.assertTrue(snapshot.getBlockEntity(rightPos) instanceof ChestBlockEntity, "right chest BE");
        helper.assertTrue(snapshot.getBlockEntity(leftPos) instanceof ChestBlockEntity, "left chest BE");
        helper.assertTrue(snapshot.blockEntities().size() == 2, "both chest halves must be available to the renderer");
        for (BlockEntity blockEntity : snapshot.blockEntities()) {
            helper.assertTrue(blockEntity.getLevel() == snapshot, "captured BE must be bound to the snapshot");
        }
        @SuppressWarnings("unchecked")
        AbstractChestBlock<ChestBlockEntity> chest = (AbstractChestBlock<ChestBlockEntity>) Blocks.CHEST;
        helper.assertTrue(isDouble(chest.combine(left, snapshot, leftPos, true)),
                "LEFT chest should combine with the adjacent snapshot RIGHT chest");
        helper.assertTrue(isDouble(chest.combine(right, snapshot, rightPos, true)),
                "RIGHT chest should combine with the adjacent snapshot LEFT chest");
        helper.assertTrue(helper.getLevel().getBlockState(rightPos).equals(worldRightBefore)
                        && helper.getLevel().getBlockState(leftPos).equals(worldLeftBefore),
                "preview construction and neighbor resolution must not mutate the live world");
        helper.succeed();
    }

    private static void assertUnsupported(Runnable operation) {
        try {
            operation.run();
        } catch (UnsupportedOperationException expected) {
            return;
        }
        throw new IllegalStateException("Read-only preview unexpectedly supported this operation");
    }

    private static boolean isDouble(DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> result) {
        return result.apply(new DoubleBlockCombiner.Combiner<ChestBlockEntity, Boolean>() {
            @Override
            public Boolean acceptDouble(ChestBlockEntity first, ChestBlockEntity second) {
                return first != null && second != null && first != second;
            }

            @Override
            public Boolean acceptSingle(ChestBlockEntity single) {
                return false;
            }

            @Override
            public Boolean acceptNone() {
                return false;
            }
        });
    }
}
