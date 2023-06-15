package committee.nova.mods.avaritia.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:03
 * Version: 1.0
 */
public class TileEntityUtil {

    public static void dispatchToNearbyPlayers(BlockEntity tile) {
        Level level = tile.getLevel();
        if (level != null) {
            Packet<ClientGamePacketListener> packet = tile.getUpdatePacket();
            if (packet != null) {
                List<? extends Player> players = level.players();
                BlockPos pos = tile.getBlockPos();

                for (Player player : players) {
                    if (player instanceof ServerPlayer mPlayer) {
                        if (isPlayerNearby(mPlayer.getX(), mPlayer.getZ(), (double) pos.getX() + 0.5D, (double) pos.getZ() + 0.5D)) {
                            mPlayer.connection.send(packet);
                        }
                    }
                }

            }
        }
    }

    public static void dispatchToNearbyPlayers(Level level, int x, int y, int z) {
        BlockEntity tile = level.getBlockEntity(new BlockPos(x, y, z));
        if (tile != null) {
            dispatchToNearbyPlayers(tile);
        }

    }

    private static boolean isPlayerNearby(double x1, double z1, double x2, double z2) {
        return Math.hypot(x1 - x2, z1 - z2) < 64.0D;
    }

}
