package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.Static;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:03
 * Version: 1.0
 */
public class TileEntityUtil {

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

    public static <T> Optional<T> get(Class<T> clazz, @Nullable BlockGetter world, BlockPos pos) {
        return get(clazz, world, pos, false);
    }

    public static <T> Optional<T> get(Class<T> clazz, @Nullable BlockGetter world, BlockPos pos, boolean logWrongType) {
        if (!isBlockLoaded(world, pos)) {
            return Optional.empty();
        }

        //TODO: This causes freezes if being called from onLoad
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null) {
            return Optional.empty();
        }

        if (clazz.isInstance(tile)) {
            return Optional.of(clazz.cast(tile));
        } else if (logWrongType) {
            Static.LOGGER.warn("Unexpected TileEntity class at {}, expected {}, but found: {}", pos, clazz, tile.getClass());
        }

        return Optional.empty();
    }

    @SuppressWarnings("deprecation")
    public static boolean isBlockLoaded(@Nullable BlockGetter world, BlockPos pos) {
        if (world == null) {
            return false;
        }
        if (world instanceof LevelReader) {
            return ((LevelReader) world).hasChunkAt(pos);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <HAVE extends BlockEntity, RET extends BlockEntity> BlockEntityTicker<RET> castTicker(BlockEntityType<RET> expected, BlockEntityType<HAVE> have, BlockEntityTicker<? super HAVE> ticker) {
        return have == expected ? (BlockEntityTicker<RET>)ticker : null;
    }

    @Nullable
    public static <HAVE extends BlockEntity, RET extends BlockEntity> BlockEntityTicker<RET> serverTicker(Level level, BlockEntityType<RET> expected, BlockEntityType<HAVE> have, BlockEntityTicker<? super HAVE> ticker) {
        return level.isClientSide ? null : castTicker(expected, have, ticker);
    }

}
