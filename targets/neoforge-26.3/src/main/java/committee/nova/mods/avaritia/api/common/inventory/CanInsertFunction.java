package committee.nova.mods.avaritia.api.common.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/14 19:12
 * @Description:
 */
@FunctionalInterface
public interface CanInsertFunction {
    boolean apply(int i, ItemStack stack);

    @FunctionalInterface
    interface Sided {
        boolean apply(int i, ItemStack stack, Direction direction);
    }
}
