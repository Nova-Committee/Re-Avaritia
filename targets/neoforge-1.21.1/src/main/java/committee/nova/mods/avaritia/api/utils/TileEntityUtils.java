package committee.nova.mods.avaritia.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:03
 * Version: 1.0
 */
public class TileEntityUtils {

    public static void dispatchToNearbyPlayers(BlockEntity tile) {
        var level = tile.getLevel();
        if (level == null)
            return;

        var packet = tile.getUpdatePacket();
        if (packet == null)
            return;

        var players = level.players();
        var pos = tile.getBlockPos();

        for (var player : players) {
            if (player instanceof ServerPlayer mPlayer) {
                if (isPlayerNearby(mPlayer.getX(), mPlayer.getZ(), pos.getX() + 0.5, pos.getZ() + 0.5)) {
                    mPlayer.connection.send(packet);
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
