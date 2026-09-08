package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSavedData;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSpaces;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreviewLevel;
import committee.nova.mods.avaritia.common.net.C2SNeutronRingPack;
import committee.nova.mods.avaritia.init.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
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
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

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
        SignBlockEntity sign = (SignBlockEntity) helper.getBlockEntity(signRel);
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
        NeutronRingSavedData.SpaceInfo info = store.list(library).get(0);
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
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                try {
                    preview.slice(from, to).write(buffer);
                    chunks.add(NeutronSpacePreview.read(buffer));
                } finally {
                    buffer.release();
                }
            }
            NeutronSpacePreview assembled = NeutronSpacePreview.assemble(chunks.get(0), chunks);
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
            NeutronRingItem.contents(ring).select(selectedId, NeutronRingSpaces.sizeOf(template)).withPlace(place).save(ring);
            NeutronRingContents beforeBrowse = NeutronRingItem.contents(ring);
            new C2SNeutronRingPack(C2SNeutronRingPack.PREVIEW, id, "", 0, library, NeutronRingSpaces.sizeOf(template))
                    .apply(player);
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
        assertUnsupported(snapshot::getFreeMapId);
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

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void captureStoresThenClearsAndFailedPlaceKeepsEntry(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AvaritiaCap"));
        player.setGameMode(GameType.CREATIVE);
        ItemStack ring = new ItemStack(ModItems.neutron_ring.get());
        player.getInventory().setItem(0, ring);
        BlockPos stoneRel = new BlockPos(8, 1, 8);
        helper.setBlock(stoneRel, Blocks.DIAMOND_BLOCK);
        NeutronRingContents bound = NeutronRingItem.bind(player, ring);
        UUID library = bound.storageId();
        BlockPos base = helper.absolutePos(stoneRel);
        CompoundTag template = NeutronRingSpaces.capture(helper.getLevel(), base, NeutronRingContents.Size.DEFAULT);
        NeutronRingSavedData store = NeutronRingSavedData.get(player.server);
        helper.assertTrue(store.add(library, "cube", template), "16-cube capture should store");
        NeutronRingSpaces.clear(helper.getLevel(), base, NeutronRingContents.Size.DEFAULT);
        helper.assertTrue(helper.getBlockState(stoneRel).isAir(), "capture must clear the source cube");
        String id = store.list(library).get(0).id();
        BlockPos oob = new BlockPos(base.getX(), helper.getLevel().getMaxBuildHeight() + 8, base.getZ());
        helper.assertFalse(NeutronRingSpaces.place(helper.getLevel(), oob, store.get(library, id).orElseThrow().template()),
                "out-of-bounds place must fail");
        helper.assertTrue(store.get(library, id).isPresent(), "failed place must keep the saved entry");
        new C2SNeutronRingPack(C2SNeutronRingPack.SET_SIZE, id, "", 0, library, new NeutronRingContents.Size(1, 1, 1))
                .apply(player);
        helper.assertTrue(NeutronRingItem.contents(ring).size().equals(NeutronRingContents.Size.DEFAULT),
                "SET_SIZE must be rejected");
        NeutronRingContents before = NeutronRingItem.contents(ring);
        new C2SNeutronRingPack(C2SNeutronRingPack.SELECT, id, "", 0, UUID.randomUUID(), NeutronRingContents.Size.DEFAULT)
                .apply(player);
        helper.assertTrue(NeutronRingItem.contents(ring).selectedId().equals(before.selectedId()),
                "wrong storageId must not mutate the ring");
        store.remove(library, id);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void twoClickCaptureAndPlacePreservesChestNbt(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "NRClick"));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        ItemStack ring = new ItemStack(ModItems.neutron_ring.get());
        player.getInventory().setItem(0, ring);
        player.getInventory().selected = 0;
        BlockPos stand = helper.absolutePos(new BlockPos(5, 2, 5));
        player.moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0.0F, 0.0F);
        ItemStack held = player.getMainHandItem();

        BlockPos chestRel = new BlockPos(8, 1, 8);
        helper.setBlock(chestRel, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestRel);
        ItemStack gem = new ItemStack(Items.DIAMOND, 3);
        gem.setHoverName(Component.literal("Vault gem"));
        chest.setItem(0, gem);
        chest.setChanged();
        BlockPos chestAbs = helper.absolutePos(chestRel);

        helper.assertTrue(clickFirst(held, player, chestAbs).consumesAction(), "first capture click should set the base");
        helper.assertTrue(helper.getBlockState(chestRel).is(Blocks.CHEST), "first capture click must preserve the world chest");
        helper.assertTrue(chestItemName(helper, chestRel).equals("Vault gem"), "first capture click must keep chest NBT");
        NeutronRingContents afterBase = NeutronRingItem.contents(held);
        UUID library = afterBase.storageId();
        NeutronRingSavedData store = NeutronRingSavedData.get(player.server);
        helper.assertTrue(afterBase.captureBase().filter(pos -> pos.pos().equals(chestAbs)).isPresent(),
                "first capture click stores the clicked block as the capture base");
        helper.assertTrue(store.list(library).isEmpty(), "first capture click must not store a space yet");
        helper.assertTrue(afterBase.size().equals(NeutronRingContents.Size.DEFAULT),
                "unset capture uses the default 16-cube size");

        helper.assertTrue(clickUseOn(player, chestAbs).consumesAction(), "second capture click should store and clear");
        helper.assertTrue(helper.getBlockState(chestRel).isAir(), "second capture click clears the captured cube");
        helper.assertTrue(store.list(library).size() == 1, "second capture click stores exactly one space");
        helper.assertTrue(NeutronRingItem.contents(held).captureBase().isEmpty(), "successful capture clears the base");
        String id = store.list(library).get(0).id();
        CompoundTag template = store.get(library, id).orElseThrow().template();
        NeutronRingContents.Size capturedSize = NeutronRingSpaces.sizeOf(template);
        helper.assertTrue(capturedSize.equals(NeutronRingContents.Size.DEFAULT),
                "item-use capture must use the default size");
        assertNoItemDrops(helper, player);

        sendRing(player, C2SNeutronRingPack.SELECT, id, library);
        NeutronRingContents selected = NeutronRingItem.contents(held);
        helper.assertTrue(selected.selectedId().filter(id::equals).isPresent(), "SELECT packet should select the saved space");
        helper.assertTrue(selected.size().equals(capturedSize), "SELECT adopts the captured template size");

        sendRing(player, C2SNeutronRingPack.DESELECT, id, library);
        NeutronRingContents deselected = NeutronRingItem.contents(held);
        helper.assertTrue(deselected.selectedId().isEmpty(), "DESELECT packet clears the selection");
        helper.assertTrue(deselected.size().equals(NeutronRingContents.Size.DEFAULT),
                "DESELECT restores the default capture size");

        sendRing(player, C2SNeutronRingPack.SELECT, id, library);
        helper.assertTrue(clickFirst(held, player, chestAbs).consumesAction(), "first place click should set the place base");
        helper.assertTrue(helper.getBlockState(chestRel).isAir(), "first place click must not paste the template");
        helper.assertTrue(store.get(library, id).isPresent(), "first place click must keep the saved entry");
        helper.assertTrue(NeutronRingItem.contents(held).placeBase().filter(pos -> pos.pos().equals(chestAbs)).isPresent(),
                "first place click stores the clicked block as the place base");

        helper.assertTrue(clickUseOn(player, chestAbs).consumesAction(), "second place click should paste and consume");
        helper.assertTrue(helper.getBlockState(chestRel).is(Blocks.CHEST), "second place click restores the chest");
        helper.assertTrue(chestItemName(helper, chestRel).equals("Vault gem"),
                "placed chest must keep the named diamond stack");
        ChestBlockEntity placed = (ChestBlockEntity) helper.getBlockEntity(chestRel);
        helper.assertTrue(placed.getItem(0).getCount() == 3 && placed.getItem(0).is(Items.DIAMOND),
                "placed chest must keep the original item count");
        helper.assertTrue(store.list(library).isEmpty(), "successful place consumes the saved entry once");
        helper.assertTrue(NeutronRingItem.contents(held).selectedId().isEmpty(), "place deselects the consumed space");
        helper.assertTrue(NeutronRingItem.contents(held).size().equals(NeutronRingContents.Size.DEFAULT),
                "place restores the default capture size");
        clickUseOn(player, chestAbs);
        helper.assertTrue(store.list(library).isEmpty(), "extra click must not duplicate the placed space");
        helper.assertTrue(helper.getBlockState(chestRel).is(Blocks.CHEST), "extra click must not recapture without a new base");
        assertNoItemDrops(helper, player);
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

    private static InteractionResult clickFirst(ItemStack ring, FakePlayer player, BlockPos absolute) {
        return ring.getItem().onItemUseFirst(ring, useOn(player, absolute));
    }

    private static InteractionResult clickUseOn(FakePlayer player, BlockPos absolute) {
        return player.getMainHandItem().getItem().useOn(useOn(player, absolute));
    }

    private static UseOnContext useOn(FakePlayer player, BlockPos absolute) {
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }

    private static void sendRing(FakePlayer player, int action, String id, UUID library) {
        C2SNeutronRingPack pack = new C2SNeutronRingPack(action, id, "", 0, library, NeutronRingContents.Size.DEFAULT);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        pack.write(buf);
        new C2SNeutronRingPack(buf).apply(player);
    }

    private static String chestItemName(GameTestHelper helper, BlockPos relative) {
        if (!(helper.getBlockEntity(relative) instanceof ChestBlockEntity chest)) {
            return "";
        }
        ItemStack stored = chest.getItem(0);
        return stored.isEmpty() ? "" : stored.getHoverName().getString();
    }

    private static void assertNoItemDrops(GameTestHelper helper, FakePlayer player) {
        helper.assertEntityNotPresent(EntityType.ITEM);
        AABB area = player.getBoundingBox().inflate(16.0);
        List<ItemEntity> nearby = player.serverLevel().getEntities(EntityType.ITEM, area, entity -> true);
        helper.assertTrue(nearby.isEmpty(), "ring capture/place must not drop extra items");
    }

}
