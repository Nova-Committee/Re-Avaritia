package committee.nova.mods.avaritia.gametest;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.InteractionHand;

import java.util.UUID;

/** Exercise the existing replacement/restoration consumer for every visual variant. */
@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TemporaryFrameStateGameTests {
    @GameTest(template = "portable_ui_empty")
    public static void miningReplacementAndRestorationPreserveAllVariants(GameTestHelper h) {
        var level = h.getLevel();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "FrameState"), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie) {
            @Override public void send(Packet<?> packet) { }
            @Override public void send(Packet<?> packet, PacketSendListener listener) { }
        };
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        try {
            int index = 0;
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (boolean eye : new boolean[]{false, true}) {
                    BlockPos pos = h.absolutePos(new BlockPos(2 + index % 4, 2, 2 + index / 4));
                    var original = Blocks.END_PORTAL_FRAME.defaultBlockState()
                            .setValue(EndPortalFrameBlock.FACING, facing).setValue(EndPortalFrameBlock.HAS_EYE, eye);
                    level.setBlock(pos, original, 2);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.crystal_pickaxe.get()));
                    NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(player, pos, Direction.UP,
                            PlayerInteractEvent.LeftClickBlock.Action.START));
                    var temporary = level.getBlockState(pos);
                    h.assertTrue(temporary.is(ModBlocks.fake_end_portal_frame.get()), "mining must select the temporary frame");
                    h.assertTrue(temporary.getValue(EndPortalFrameBlock.FACING) == facing
                                    && temporary.getValue(EndPortalFrameBlock.HAS_EYE) == eye,
                            "replacement must preserve facing/eye for " + original);
                    h.assertTrue(temporary.getShape(level, pos).equals(original.getShape(level, pos)),
                            "temporary frame must retain the vanilla eye-dependent outline");
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.STICK));
                    NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(player, pos, Direction.UP,
                            PlayerInteractEvent.LeftClickBlock.Action.START));
                    h.assertTrue(level.getBlockState(pos).equals(original), "switching tools must restore the exact original state");
                    index++;
                }
            }
            Const.LOGGER.info("FRAME_STATE_PASS all 8 variants replaced and restored with vanilla shapes");
            h.succeed();
        } finally {
            player.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}
