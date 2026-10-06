package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
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
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@EventBusSubscriber(modid = "avaritia_gametest")
public final class NeutronSpacePreviewGameTests {
    private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");
    private static final Identifier CAPTURE_ID = Const.rl("neutron_ring_captured_preview");
    private static final Identifier CHEST_ID = Const.rl("neutron_ring_double_chest");
    private static final Identifier ADOPT_OK_ID = Const.rl("neutron_ring_adopt_ok");
    private static final Identifier ADOPT_FULL_ID = Const.rl("neutron_ring_adopt_full");
    private static final Identifier DESELECT_ID = Const.rl("neutron_ring_deselect_default_size");
    private static final Identifier CHUNK_ID = Const.rl("neutron_ring_chunked_preview");
    private static final Identifier EMPTY_TEMPLATE_ID = Const.rl("neutron_ring_empty_template");
    private static final Identifier TWO_CLICK_ID = Const.rl("neutron_ring_two_click_capture_place");
    private static final ResourceKey<Consumer<GameTestHelper>> CAPTURE = ResourceKey.create(Registries.TEST_FUNCTION, CAPTURE_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> CHEST = ResourceKey.create(Registries.TEST_FUNCTION, CHEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> ADOPT_OK = ResourceKey.create(Registries.TEST_FUNCTION, ADOPT_OK_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> ADOPT_FULL = ResourceKey.create(Registries.TEST_FUNCTION, ADOPT_FULL_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> DESELECT = ResourceKey.create(Registries.TEST_FUNCTION, DESELECT_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> CHUNK = ResourceKey.create(Registries.TEST_FUNCTION, CHUNK_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> EMPTY_TEMPLATE = ResourceKey.create(Registries.TEST_FUNCTION, EMPTY_TEMPLATE_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> TWO_CLICK = ResourceKey.create(Registries.TEST_FUNCTION, TWO_CLICK_ID);

    private NeutronSpacePreviewGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, CAPTURE_ID, () -> NeutronSpacePreviewGameTests::capturedPreviewPreservesStates);
        event.register(Registries.TEST_FUNCTION, CHEST_ID, () -> NeutronSpacePreviewGameTests::adjacentDoubleChestIsolatedFromWorld);
        event.register(Registries.TEST_FUNCTION, ADOPT_OK_ID, () -> NeutronSpacePreviewGameTests::adoptMovesEntireLibraryWhenCapacityAllows);
        event.register(Registries.TEST_FUNCTION, ADOPT_FULL_ID, () -> NeutronSpacePreviewGameTests::adoptLeavesBothLibrariesUnchangedWhenCapacityIsInsufficient);
        event.register(Registries.TEST_FUNCTION, DESELECT_ID, () -> NeutronSpacePreviewGameTests::deselectRestoresDefaultCaptureSize);
        event.register(Registries.TEST_FUNCTION, CHUNK_ID, () -> NeutronSpacePreviewGameTests::chunkedTransferKeepsPackedCellsAndHeight);
        event.register(Registries.TEST_FUNCTION, EMPTY_TEMPLATE_ID, () -> NeutronSpacePreviewGameTests::emptyTemplateIsEmptyPreview);
        event.register(Registries.TEST_FUNCTION, TWO_CLICK_ID, () -> NeutronSpacePreviewGameTests::twoClickCapturePlacePreservesChestContents);
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("neutron_ring"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CAPTURE_ID, new FunctionGameTestInstance(CAPTURE, new TestData<>(environment, EMPTY_STRUCTURE, 100, 0, true)));
        event.registerTest(CHEST_ID, new FunctionGameTestInstance(CHEST, new TestData<>(environment, EMPTY_STRUCTURE, 100, 0, true)));
        event.registerTest(ADOPT_OK_ID, new FunctionGameTestInstance(ADOPT_OK, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true)));
        event.registerTest(ADOPT_FULL_ID, new FunctionGameTestInstance(ADOPT_FULL, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true)));
        event.registerTest(DESELECT_ID, new FunctionGameTestInstance(DESELECT, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true)));
        event.registerTest(CHUNK_ID, new FunctionGameTestInstance(CHUNK, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true)));
        event.registerTest(EMPTY_TEMPLATE_ID, new FunctionGameTestInstance(EMPTY_TEMPLATE, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true)));
        event.registerTest(TWO_CLICK_ID, new FunctionGameTestInstance(TWO_CLICK, new TestData<>(environment, EMPTY_STRUCTURE, 200, 0, true)));
    }

    private static void capturedPreviewPreservesStates(GameTestHelper helper) {
        var player = GameTestPlayers.create(helper, "AvaritiaGT");
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
        SignBlockEntity sign = helper.getBlockEntity(signRel, SignBlockEntity.class);
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
        NeutronRingSavedData store = NeutronRingSavedData.get(helper.getLevel().getServer());
        helper.assertTrue(store.add(library, "preview-fixture", template), "fixture should own the captured space");
        NeutronRingSavedData.SpaceInfo info = store.list(library).getFirst();
        String id = info.id();
        try {
            NeutronSpacePreview.Meta meta = NeutronSpacePreview.meta(template);
            helper.assertTrue(info.sizeX() == meta.sizeX() && info.sizeY() == meta.sizeY()
                            && info.sizeZ() == meta.sizeZ() && info.blocks() == meta.blocks(),
                    "SavedData metadata should match the captured template");
            NeutronSpacePreview preview = NeutronSpacePreview.fromTemplate(template, helper.getLevel().registryAccess());
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
            List<NeutronSpacePreview> chunks = new ArrayList<>();
            int total = Math.min(preview.positions().length, preview.states().length);
            int chunkCount = Math.max(1, (total + NeutronSpacePreview.CELLS_PER_CHUNK - 1) / NeutronSpacePreview.CELLS_PER_CHUNK);
            for (int i = 0; i < chunkCount; i++) {
                int from = i * NeutronSpacePreview.CELLS_PER_CHUNK;
                int to = Math.min(total, from + NeutronSpacePreview.CELLS_PER_CHUNK);
                chunks.add(preview.slice(from, to));
            }
            NeutronSpacePreview assembled = NeutronSpacePreview.assemble(chunks.getFirst(), chunks);
            NeutronSpacePreviewLevel decoded = new NeutronSpacePreviewLevel(
                    helper.getLevel(), helper.absolutePos(minRel), assembled);
            helper.assertTrue(decoded.getBlockEntity(new BlockPos(1, 0, 2)) instanceof SignBlockEntity decodedSign
                            && decodedSign.getFrontText().getMessage(0, false).getString().equals("Preview sign"),
                    "sign text must survive capture and preview reconstruction");
            helper.assertTrue(assembled.stateAt(0, 2, 0).is(Blocks.GLASS)
                            && assembled.stateAt(0, 1, 0).isAir(),
                    "separated vertical layers must not become solid columns");
        } finally {
            store.remove(library, id);
        }
        helper.succeed();
    }

    private static void adjacentDoubleChestIsolatedFromWorld(GameTestHelper helper) {
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
        helper.assertTrue(snapshot.getMinY() == 0 && snapshot.getHeight() == 2,
                "snapshot build bounds follow the capture");
        helper.assertFalse(snapshot.hasChunk(0, 0) || snapshot.isLoaded(rightPos),
                "a chunkless snapshot must not advertise loaded chunks");
        helper.assertTrue(snapshot.getChunk(0, 0, ChunkStatus.FULL, false) == null,
                "optional chunk lookup must report unavailable");
        assertUnsupported(() -> snapshot.getChunk(0, 0));
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

    private static void adoptMovesEntireLibraryWhenCapacityAllows(GameTestHelper helper) {
        NeutronRingSavedData data = new NeutronRingSavedData();
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        helper.assertTrue(data.add(from, "keep", new CompoundTag()), "source space");
        helper.assertTrue(data.add(to, "existing", new CompoundTag()), "dest space");
        helper.assertTrue(data.adopt(from, to), "adopt should move the whole library");
        helper.assertTrue(data.list(to).size() == 2, "destination should own both spaces");
        helper.assertTrue(data.list(from).isEmpty(), "source library should be empty");
        helper.succeed();
    }

    private static void adoptLeavesBothLibrariesUnchangedWhenCapacityIsInsufficient(GameTestHelper helper) {
        NeutronRingSavedData data = new NeutronRingSavedData();
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        for (int i = 0; i < NeutronRingSavedData.MAX_SPACES; i++) {
            helper.assertTrue(data.add(to, "owned-" + i, new CompoundTag()), "fill dest");
        }
        helper.assertTrue(data.add(from, "overflow", new CompoundTag()), "source overflow");
        helper.assertFalse(data.adopt(from, to), "adopt must be all-or-nothing");
        helper.assertTrue(data.list(to).size() == NeutronRingSavedData.MAX_SPACES, "dest unchanged");
        helper.assertTrue(data.list(from).size() == 1 && data.list(from).getFirst().name().equals("overflow"),
                "source overflow space preserved");
        helper.succeed();
    }

    private static void deselectRestoresDefaultCaptureSize(GameTestHelper helper) {
        NeutronRingContents selected = NeutronRingContents.create()
                .select("space", new NeutronRingContents.Size(8, 4, 8));
        helper.assertTrue(selected.size().x() == 8, "selected size");
        NeutronRingContents cleared = selected.deselect();
        helper.assertTrue(cleared.selectedId().isEmpty(), "deselected");
        helper.assertTrue(cleared.size().equals(NeutronRingContents.Size.DEFAULT), "default 16 cube restored");
        helper.succeed();
    }

    private static void chunkedTransferKeepsPackedCellsAndHeight(GameTestHelper helper) {
        int height = 40;
        int[] positions = new int[height];
        int[] states = new int[height];
        for (int y = 0; y < height; y++) {
            positions[y] = NeutronSpacePreview.pack(1, y, 2);
            states[y] = 0;
        }
        NeutronSpacePreview preview = new NeutronSpacePreview(16, height, 16, height,
                List.of(), positions, states, List.of());
        List<NeutronSpacePreview> chunks = new ArrayList<>();
        int total = preview.positions().length;
        int chunkCount = Math.max(1, (total + NeutronSpacePreview.CELLS_PER_CHUNK - 1) / NeutronSpacePreview.CELLS_PER_CHUNK);
        for (int i = 0; i < chunkCount; i++) {
            int from = i * NeutronSpacePreview.CELLS_PER_CHUNK;
            int to = Math.min(total, from + NeutronSpacePreview.CELLS_PER_CHUNK);
            chunks.add(preview.slice(from, to));
        }
        NeutronSpacePreview assembled = NeutronSpacePreview.assemble(chunks.getFirst(), chunks);
        helper.assertTrue(assembled.blocks() == height && assembled.positions().length == height, "full height");
        helper.assertTrue(assembled.positions()[0] == NeutronSpacePreview.pack(1, 0, 2), "first cell");
        helper.assertTrue(assembled.positions()[height - 1] == NeutronSpacePreview.pack(1, height - 1, 2), "last cell");
        helper.assertTrue(NeutronSpacePreview.unpackY(assembled.positions()[height - 1]) == height - 1, "y packing");
        helper.succeed();
    }

    private static void emptyTemplateIsEmptyPreview(GameTestHelper helper) {
        NeutronSpacePreview preview = NeutronSpacePreview.fromTemplate(new CompoundTag(), helper.getLevel().registryAccess());
        helper.assertTrue(preview.blocks() == NeutronSpacePreview.EMPTY.blocks(), "empty template");
        helper.succeed();
    }

    private static void twoClickCapturePlacePreservesChestContents(GameTestHelper helper) {
        var player = GameTestPlayers.create(helper, "AvaritiaRingClick");
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        ItemStack ring = new ItemStack(ModItems.neutron_ring.get());
        player.getInventory().setItem(0, ring);
        BlockPos chestRel = new BlockPos(8, 1, 8);
        helper.setBlock(chestRel, Blocks.CHEST);
        ItemStack stored = new ItemStack(Items.DIAMOND, 3);
        stored.set(DataComponents.CUSTOM_NAME, Component.literal("Pocket Gem"));
        CompoundTag mark = new CompoundTag();
        mark.putString("neutron", "kept");
        stored.set(DataComponents.CUSTOM_DATA, CustomData.of(mark));
        ChestBlockEntity chest = helper.getBlockEntity(chestRel, ChestBlockEntity.class);
        chest.setItem(0, stored.copy());

        NeutronRingItem item = (NeutronRingItem) ring.getItem();
        helper.assertTrue(item.onItemUseFirst(ring, useOn(helper, player, chestRel)).consumesAction(),
                "first capture click should set the base");
        helper.assertTrue(helper.getBlockState(chestRel).is(Blocks.CHEST), "first capture click must leave the world");
        helper.assertTrue(ItemStack.matches(chest.getItem(0), stored), "first capture click must leave chest contents");
        NeutronRingContents afterFirst = NeutronRingItem.contents(ring);
        UUID library = afterFirst.storageId();
        NeutronRingSavedData store = NeutronRingSavedData.get(helper.getLevel().getServer());
        helper.assertTrue(afterFirst.captureBase().isPresent() && afterFirst.selectedId().isEmpty(),
                "first capture click must record the base without selecting");
        helper.assertTrue(store.list(library).isEmpty(), "first capture click must not store a space");

        helper.assertTrue(item.useOn(useOn(helper, player, chestRel)).consumesAction(),
                "second capture click should store and clear");
        helper.assertTrue(helper.getBlockState(chestRel).isAir(), "second capture click must clear the source cube");
        helper.assertTrue(store.list(library).size() == 1, "second capture click must store exactly one space");
        String id = store.list(library).getFirst().id();
        helper.assertTrue(NeutronRingItem.contents(ring).captureBase().isEmpty(), "stored capture must clear the base");
        helper.assertEntityNotPresent(EntityType.ITEM);

        handleRing(helper, player, new C2SNeutronRingPack(
                C2SNeutronRingPack.SELECT, id, "", 0, library, NeutronRingContents.Size.DEFAULT));
        helper.assertTrue(id.equals(NeutronRingItem.contents(ring).selectedId().orElse("")),
                "SELECT packet must select the captured space");
        handleRing(helper, player, new C2SNeutronRingPack(
                C2SNeutronRingPack.DESELECT, "", "", 0, library, new NeutronRingContents.Size(1, 1, 1)));
        helper.assertTrue(NeutronRingItem.contents(ring).selectedId().isEmpty(), "DESELECT packet must clear selection");
        helper.assertTrue(NeutronRingItem.contents(ring).size().equals(NeutronRingContents.Size.DEFAULT),
                "DESELECT must restore the default capture size");

        handleRing(helper, player, new C2SNeutronRingPack(
                C2SNeutronRingPack.SELECT, id, "", 0, library, NeutronRingContents.Size.DEFAULT));
        helper.assertTrue(item.onItemUseFirst(ring, useOn(helper, player, chestRel)).consumesAction(),
                "first place click should preview the destination");
        helper.assertTrue(helper.getBlockState(chestRel).isAir(), "first place click must leave the destination empty");
        helper.assertTrue(store.get(library, id).isPresent(), "first place click must keep the saved entry");
        helper.assertTrue(NeutronRingItem.contents(ring).placeBase().isPresent(), "first place click must record the destination");

        helper.assertTrue(item.useOn(useOn(helper, player, chestRel)).consumesAction(),
                "second place click should place and consume");
        helper.assertTrue(helper.getBlockState(chestRel).is(Blocks.CHEST), "second place click must restore the chest");
        ItemStack restored = helper.getBlockEntity(chestRel, ChestBlockEntity.class).getItem(0);
        helper.assertTrue(restored.is(Items.DIAMOND) && restored.getCount() == 3
                        && "Pocket Gem".equals(restored.getHoverName().getString())
                        && "kept".equals(restored.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .copyTag().getStringOr("neutron", "")),
                "placed chest must keep named custom-data contents");
        helper.assertTrue(store.get(library, id).isEmpty(), "successful place must consume the space once");
        helper.assertTrue(NeutronRingItem.contents(ring).selectedId().isEmpty(), "place must deselect");
        helper.assertTrue(NeutronRingItem.contents(ring).size().equals(NeutronRingContents.Size.DEFAULT),
                "place must restore the default capture size");
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }

    private static UseOnContext useOn(GameTestHelper helper, ServerPlayer player, BlockPos relative) {
        BlockPos absolute = helper.absolutePos(relative);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }

    private static void handleRing(GameTestHelper helper, ServerPlayer player, C2SNeutronRingPack packet) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            C2SNeutronRingPack.STREAM_CODEC.encode(buffer, packet);
            C2SNeutronRingPack decoded = C2SNeutronRingPack.STREAM_CODEC.decode(buffer);
            new C2SNeutronRingPack.Handler().handle(decoded,
                    new ServerPayloadContext(player.connection, C2SNeutronRingPack.TYPE.id()));
        } finally {
            buffer.release();
        }
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
