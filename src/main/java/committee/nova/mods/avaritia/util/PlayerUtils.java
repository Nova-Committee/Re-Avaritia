package committee.nova.mods.avaritia.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

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
        return !ServerLifecycleHooks.getCurrentServer().isUnderSpawnProtection((ServerLevel) player.level(), pos, player)
                && Arrays.stream(Direction.values()).allMatch((e) -> player.mayUseItemAt(pos, e, ItemStack.EMPTY));
    }

    public static boolean checkedPlaceBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        if (!hasEditPermission(player, pos)) {
            return false;
        } else {
            Level level = player.level();
            BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos);
            level.setBlockAndUpdate(pos, state);
            BlockEvent.EntityPlaceEvent evt = new BlockEvent.EntityPlaceEvent(before, Blocks.AIR.defaultBlockState(), player);
            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) {
                level.restoringBlockSnapshots = true;
                before.restore(true, false);
                level.restoringBlockSnapshots = false;
                return false;
            } else {
                return true;
            }
        }
    }
}
