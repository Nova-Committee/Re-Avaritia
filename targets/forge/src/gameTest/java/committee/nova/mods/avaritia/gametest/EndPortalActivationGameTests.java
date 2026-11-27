package committee.nova.mods.avaritia.gametest;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EndPortalActivationGameTests {
    @GameTest(template = "portable_ui_empty")
    public static void clockwiseFramesDoNotCreateAnOutsidePortal(GameTestHelper helper) {
        checkActivation(helper, 1, Direction.NORTH);
    }

    @GameTest(template = "portable_ui_empty")
    public static void inwardFramesCreateOnlyTheInsidePortal(GameTestHelper helper) {
        checkActivation(helper, 0, Direction.NORTH);
    }

    @GameTest(template = "portable_ui_empty")
    public static void counterclockwiseFramesDoNotCreateAnOutsidePortal(GameTestHelper helper) {
        checkActivation(helper, 3, Direction.SOUTH);
    }

    @GameTest(template = "portable_ui_empty")
    public static void outwardFramesDoNotCreateAnOutsidePortal(GameTestHelper helper) {
        checkActivation(helper, 2, Direction.EAST);
    }

    @GameTest(template = "portable_ui_empty")
    public static void eastEdgeLastEyeCreatesTheInsidePortal(GameTestHelper helper) {
        checkActivation(helper, 0, Direction.EAST, 1, Defect.NONE);
    }

    @GameTest(template = "portable_ui_empty")
    public static void southEdgeLastEyeCreatesTheInsidePortal(GameTestHelper helper) {
        checkActivation(helper, 0, Direction.SOUTH, -1, Defect.NONE);
    }

    @GameTest(template = "portable_ui_empty")
    public static void westLastEyeCreatesTheInsidePortal(GameTestHelper helper) {
        checkActivation(helper, 0, Direction.WEST);
    }

    @GameTest(template = "portable_ui_empty")
    public static void missingFramePreventsActivation(GameTestHelper helper) {
        checkActivation(helper, 0, Direction.NORTH, 0, Defect.MISSING_FRAME);
    }

    @GameTest(template = "portable_ui_empty")
    public static void missingOtherEyePreventsActivation(GameTestHelper helper) {
        checkActivation(helper, 0, Direction.NORTH, 0, Defect.MISSING_EYE);
    }

    @GameTest(template = "portable_ui_empty")
    public static void oneWrongFacingPreventsActivation(GameTestHelper helper) {
        checkActivation(helper, 0, Direction.NORTH, 0, Defect.WRONG_FACING);
    }

    private enum Defect { NONE, MISSING_FRAME, MISSING_EYE, WRONG_FACING }

    private static void checkActivation(GameTestHelper helper, int quarterTurns, Direction lastSide) {
        checkActivation(helper, quarterTurns, lastSide, 0, Defect.NONE);
    }

    private static void checkActivation(GameTestHelper helper, int quarterTurns, Direction lastSide,
                                        int edgeOffset, Defect defect) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(6, 2, 4));
        BlockPos lastFrame = center.relative(lastSide, 2).relative(lastSide.getClockWise(), edgeOffset);
        placeRing(level, center, quarterTurns);
        BlockPos damagedFrame = center.relative(lastSide.getOpposite(), 2);
        switch (defect) {
            case MISSING_FRAME -> level.setBlock(damagedFrame, Blocks.AIR.defaultBlockState(), 2);
            case MISSING_EYE -> level.setBlock(damagedFrame,
                    level.getBlockState(damagedFrame).setValue(EndPortalFrameBlock.HAS_EYE, false), 2);
            case WRONG_FACING -> level.setBlock(damagedFrame,
                    level.getBlockState(damagedFrame).setValue(EndPortalFrameBlock.FACING, lastSide.getClockWise()), 2);
        }
        level.setBlock(lastFrame, level.getBlockState(lastFrame).setValue(EndPortalFrameBlock.HAS_EYE, false), 2);
        ServerPlayer player = createPlayer(level, lastFrame);
        try {
            ItemStack eyes = player.getMainHandItem();
            InteractionResult result = player.gameMode.useItemOn(player, level, eyes, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(lastFrame), Direction.UP, lastFrame, false));
            List<List<Integer>> inside = new ArrayList<>();
            List<List<Integer>> outside = new ArrayList<>();
            int remainingFrames = 0;
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    if (level.getBlockState(center.offset(x, 0, z)).is(Blocks.END_PORTAL_FRAME)) remainingFrames++;
                    if (level.getBlockState(center.offset(x, 0, z)).is(Blocks.END_PORTAL)) {
                        (Math.abs(x) <= 1 && Math.abs(z) <= 1 ? inside : outside).add(List.of(x, 0, z));
                    }
                }
            }
            BlockPattern.BlockPatternMatch match = EndPortalFrameBlock.getOrCreatePortalShape().find(level, lastFrame);
            Map<String, Object> state = new LinkedHashMap<>();
            state.put("quarterTurnsFromInward", quarterTurns);
            state.put("lastSide", lastSide.name());
            state.put("edgeOffset", edgeOffset);
            state.put("defect", defect.name());
            state.put("interactionResult", result.name());
            state.put("eyesAfter", eyes.getCount());
            state.put("lastFrameHasEye", level.getBlockState(lastFrame).is(Blocks.END_PORTAL_FRAME)
                    && level.getBlockState(lastFrame).getValue(EndPortalFrameBlock.HAS_EYE));
            state.put("remainingFrames", remainingFrames);
            state.put("insidePortalBlocks", inside);
            state.put("outsidePortalBlocks", outside);
            state.put("rawPatternMatchAfter", match == null ? null : Map.of(
                    "forwards", match.getForwards().name(), "up", match.getUp().name(),
                    "originRelative", List.of(match.getFrontTopLeft().getX() - center.getX(),
                            match.getFrontTopLeft().getY() - center.getY(), match.getFrontTopLeft().getZ() - center.getZ())));
            writeState("rotation-" + quarterTurns + "-last-" + lastSide.name().toLowerCase()
                    + (edgeOffset == 0 ? "" : "-edge-" + edgeOffset)
                    + (defect == Defect.NONE ? "" : "-" + defect.name().toLowerCase()), state);
            helper.assertTrue(result.consumesAction(), "using an eye on an empty frame must consume the interaction");
            helper.assertTrue(eyes.getCount() == 1, "filling the last eye must consume exactly one eye");
            helper.assertTrue(Boolean.TRUE.equals(state.get("lastFrameHasEye")), "the last frame must receive its eye");
            helper.assertTrue(outside.isEmpty(), "activation must never create portal blocks outside the frame ring: " + outside);
            helper.assertTrue(remainingFrames == (defect == Defect.MISSING_FRAME ? 11 : 12),
                    "filling an eye must preserve every remaining frame");
            helper.assertTrue(inside.size() == (quarterTurns == 0 && defect == Defect.NONE ? 9 : 0),
                    "only a correctly inward-facing ring may activate its nine inside blocks");
            helper.succeed();
        } finally {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }

    private static void placeRing(ServerLevel level, BlockPos center, int quarterTurns) {
        for (Direction inward : Direction.Plane.HORIZONTAL) {
            Direction facing = inward;
            for (int rotation = 0; rotation < quarterTurns; rotation++) facing = facing.getClockWise();
            for (int offset = -1; offset <= 1; offset++) {
                BlockPos pos = center.relative(inward.getOpposite(), 2).relative(inward.getClockWise(), offset);
                level.setBlock(pos, Blocks.END_PORTAL_FRAME.defaultBlockState()
                        .setValue(EndPortalFrameBlock.FACING, facing)
                        .setValue(EndPortalFrameBlock.HAS_EYE, true), 2);
            }
        }
    }

    private static ServerPlayer createPlayer(ServerLevel level, BlockPos frame) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "PortalActivator"));
        new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) { }
            @Override public void send(Packet<?> packet, PacketSendListener callback) { }
        };
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.ENDER_EYE, 2));
        player.setPos(frame.getX() + 0.5, frame.getY() + 1, frame.getZ() + 0.5);
        level.addNewPlayer(player);
        return player;
    }

    private static void writeState(String name, Map<String, Object> state) {
        String output = System.getProperty("avaritia.issue170.output");
        if (output == null) return;
        try {
            String phase = System.getProperty("avaritia.issue170.phase", "baseline");
            Files.writeString(Path.of(output, phase + "-" + name + "-state.json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(state) + "\n");
            Const.LOGGER.info("ISSUE170 {} {}", name, state);
        } catch (java.io.IOException error) {
            throw new java.io.UncheckedIOException(error);
        }
    }
}
