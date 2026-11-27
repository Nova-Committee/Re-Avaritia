package committee.nova.mods.avaritia.api.common.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * @author cnlimiter
 */
@FunctionalInterface
public interface CanInsertFunction {
    boolean apply(int i, ItemStack stack);

    @FunctionalInterface
    public interface Sided {
        boolean apply(int i, ItemStack stack, Direction direction);
    }
}
