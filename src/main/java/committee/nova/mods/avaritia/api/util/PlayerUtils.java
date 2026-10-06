package committee.nova.mods.avaritia.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

/**
 * PlayerUtil
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 12:58
 */
public class PlayerUtils {
    public static boolean isPlayingMode(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    public static boolean hasEditPermission(ServerPlayer player, BlockPos pos) {
        return !player.server.isUnderSpawnProtection((ServerLevel) player.level(), pos, player)
                && player.level().mayInteract(player, pos)
                && Arrays.stream(Direction.values()).allMatch((e) -> player.mayUseItemAt(pos, e, ItemStack.EMPTY));
    }

    public static boolean checkedPlaceBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        Level level = player.level();
        if (!level.isInWorldBounds(pos) || !level.isLoaded(pos)) {
            return false;
        }

        return hasEditPermission(player, pos) && level.setBlockAndUpdate(pos, state);
    }
}
