package committee.nova.mods.avaritia.gametest;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EndPortalFrameMiningGameTests {
    @GameTest(template = "portable_ui_empty")
    public static void crystalPickaxeDismantlesActivePortal(GameTestHelper helper) {
        checkMining(helper, false, Direction.SOUTH, true, true, false);
    }

    @GameTest(template = "portable_ui_empty")
    public static void infinityPickaxeDismantlesActivePortal(GameTestHelper helper) {
        checkMining(helper, true, Direction.EAST, true, true, false);
    }

    @GameTest(template = "portable_ui_empty")
    public static void filledInactiveFrameReturnsEye(GameTestHelper helper) {
        checkMining(helper, false, Direction.NORTH, true, false, false);
    }

    @GameTest(template = "portable_ui_empty")
    public static void emptyFrameDoesNotReturnEye(GameTestHelper helper) {
        checkMining(helper, true, Direction.WEST, false, false, false);
    }

    @GameTest(template = "portable_ui_empty")
    public static void canceledMiningDoesNotDropOrDismantle(GameTestHelper helper) {
        checkMining(helper, true, Direction.SOUTH, true, true, true);
    }

    @GameTest(template = "portable_ui_empty")
    public static void northEdgeFrameDismantlesActivePortal(GameTestHelper helper) {
        checkMining(helper, true, Direction.NORTH, true, true, false, -1);
    }

    @GameTest(template = "portable_ui_empty")
    public static void westEdgeFrameDismantlesActivePortal(GameTestHelper helper) {
        checkMining(helper, true, Direction.WEST, true, true, false, 1);
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 40)
    public static void abandonedMiningRestoresFrameWithoutSideEffects(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(6, 2, 4));
        BlockPos frame = center.north(2);
        placeRing(level, center);
        fillPortal(level, center);
        BlockState original = level.getBlockState(frame);
        ServerPlayer player = createPlayer(level, frame, ModItems.crystal_pickaxe.get());
        player.gameMode.handleBlockBreakAction(frame, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                Direction.UP, level.getMaxBuildHeight(), 1);
        helper.assertTrue(level.getBlockState(frame).is(ModBlocks.fake_end_portal_frame.get()), "unfinished mining must use the temporary frame");
        player.gameMode.handleBlockBreakAction(frame, ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                Direction.UP, level.getMaxBuildHeight(), 2);
        helper.runAfterDelay(2, () -> {
            try {
                helper.assertTrue(level.getBlockState(frame).equals(original), "abandoned mining must restore the exact frame state");
                helper.assertTrue(countPortal(level, center) == 9, "abandoned mining must retain the active portal");
                helper.assertTrue(countItem(level, frame, Items.END_PORTAL_FRAME) == 0 && countItem(level, frame, Items.ENDER_EYE) == 0,
                        "abandoned mining must not drop a frame or eye");
                helper.succeed();
            } finally {
                level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            }
        });
    }

    private static void checkMining(GameTestHelper helper, boolean infinity, Direction facing,
                                    boolean eye, boolean active, boolean cancel) {
        checkMining(helper, infinity, facing, eye, active, cancel, 0);
    }

    private static void checkMining(GameTestHelper helper, boolean infinity, Direction facing,
                                    boolean eye, boolean active, boolean cancel, int edgeOffset) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(6, 2, 4));
        BlockPos frame = center.relative(facing.getOpposite(), 2).relative(facing.getClockWise(), edgeOffset);
        placeRing(level, center);
        BlockState frameState = level.getBlockState(frame).setValue(EndPortalFrameBlock.HAS_EYE, eye);
        level.setBlock(frame, frameState, 2);
        if (active) fillPortal(level, center);
        BlockPos unrelatedPortal = center.offset(7, 0, 0);
        level.setBlock(unrelatedPortal, Blocks.END_PORTAL.defaultBlockState(), 2);
        ServerPlayer player = createPlayer(level, frame, infinity ? ModItems.infinity_pickaxe.get() : ModItems.crystal_pickaxe.get());
        Consumer<BlockEvent.BreakEvent> listener = event -> {
            if (event.getPlayer() == player && event.getPos().equals(frame) && cancel) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, listener);
        try {
            player.gameMode.handleBlockBreakAction(frame, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                    Direction.UP, level.getMaxBuildHeight(), 1);
            if (level.getBlockState(frame).is(ModBlocks.fake_end_portal_frame.get()) && !infinity) {
                helper.assertTrue(countItem(level, frame, Items.END_PORTAL_FRAME) == 0 && countItem(level, frame, Items.ENDER_EYE) == 0,
                        "starting an incomplete mining action must not emit drops");
                for (int tick = 0; tick < 200; tick++) player.gameMode.tick();
                player.gameMode.handleBlockBreakAction(frame, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                        Direction.UP, level.getMaxBuildHeight(), 2);
            }
            Map<String, Object> state = new LinkedHashMap<>();
            state.put("infinityPickaxe", infinity);
            state.put("facing", facing.name());
            state.put("edgeOffset", edgeOffset);
            state.put("hadEye", eye);
            state.put("active", active);
            state.put("canceled", cancel);
            state.put("frameAfter", level.getBlockState(frame).toString());
            state.put("frameDrops", countItem(level, frame, Items.END_PORTAL_FRAME));
            state.put("eyeDrops", countItem(level, frame, Items.ENDER_EYE));
            state.put("portalBlocksAfter", countPortal(level, center));
            state.put("unrelatedPortalIntact", level.getBlockState(unrelatedPortal).is(Blocks.END_PORTAL));
            writeState((infinity ? "infinity" : "crystal") + "-" + facing.name().toLowerCase()
                    + (edgeOffset == 0 ? "" : "-edge-" + edgeOffset) + (cancel ? "-canceled" : ""), state);
            if (cancel) {
                helper.assertTrue(!level.getBlockState(frame).isAir(), "canceled break must retain the frame");
                helper.assertTrue(countPortal(level, center) == 9, "canceled break must retain the active portal");
                helper.assertTrue(countItem(level, frame, Items.END_PORTAL_FRAME) == 0 && countItem(level, frame, Items.ENDER_EYE) == 0,
                        "canceled break must not duplicate frame or eye drops");
                helper.runAfterDelay(2, () -> {
                    helper.assertTrue(level.getBlockState(frame).equals(frameState), "canceled mining must restore the original frame state");
                    helper.assertTrue(countPortal(level, center) == 9, "restoring a canceled frame must preserve the portal");
                    helper.succeed();
                });
            } else {
                helper.assertTrue(level.getBlockState(frame).isAir(), "completed real mining must remove the frame");
                helper.assertTrue(countItem(level, frame, Items.END_PORTAL_FRAME) == 1, "completed mining must return one frame");
                helper.assertTrue(countItem(level, frame, Items.ENDER_EYE) == (eye ? 1 : 0),
                        "a filled frame must return exactly one eye and an empty frame none");
                helper.assertTrue(countPortal(level, center) == 0, "completed frame mining must deactivate its internal portal");
            }
            helper.assertTrue(level.getBlockState(unrelatedPortal).is(Blocks.END_PORTAL), "unrelated portal must remain intact");
            if (!cancel) helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }

    private static void placeRing(ServerLevel level, BlockPos center) {
        for (Direction inward : Direction.Plane.HORIZONTAL) {
            for (int offset = -1; offset <= 1; offset++) {
                BlockPos pos = center.relative(inward.getOpposite(), 2).relative(inward.getClockWise(), offset);
                level.setBlock(pos, Blocks.END_PORTAL_FRAME.defaultBlockState()
                        .setValue(EndPortalFrameBlock.FACING, inward)
                        .setValue(EndPortalFrameBlock.HAS_EYE, true), 2);
            }
        }
    }

    private static void fillPortal(ServerLevel level, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) level.setBlock(center.offset(x, 0, z), Blocks.END_PORTAL.defaultBlockState(), 2);
        }
    }

    private static int countPortal(ServerLevel level, BlockPos center) {
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) if (level.getBlockState(center.offset(x, 0, z)).is(Blocks.END_PORTAL)) count++;
        }
        return count;
    }

    private static int countItem(ServerLevel level, BlockPos pos, Item item) {
        return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2), entity -> entity.getItem().is(item))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static ServerPlayer createPlayer(ServerLevel level, BlockPos frame, Item tool) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "FrameMiner"));
        new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) { }
            @Override public void send(Packet<?> packet, PacketSendListener callback) { }
        };
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(tool));
        player.setPos(frame.getX() + 0.5, frame.getY(), frame.getZ() + 1.5);
        player.setOnGround(true);
        level.addNewPlayer(player);
        return player;
    }

    private static void writeState(String name, Map<String, Object> state) {
        String output = System.getProperty("avaritia.issue169.output");
        if (output == null) return;
        try {
            String phase = System.getProperty("avaritia.issue169.phase", "baseline");
            Files.writeString(Path.of(output, phase + "-" + name + "-state.json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(state) + "\n");
            Const.LOGGER.info("ISSUE169 {} {}", name, state);
        } catch (java.io.IOException error) {
            throw new java.io.UncheckedIOException(error);
        }
    }
}
