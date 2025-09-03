package committee.nova.mods.avaritia.api.iface;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/1/22 02:48
 * @Description:
 */
public interface IBreakable {
    default boolean breakAll(ItemStack stack, BlockPos pos, Player player) {
        return true;
    }
}
