package committee.nova.mods.avaritia.api.common.wrapper;

import net.minecraft.world.item.ItemStack;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 14:16
 * Version: 1.0
 */
public class StrictItemStack {
    public final ItemStack stack;

    public StrictItemStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj instanceof StrictItemStack other) {
            return ItemStack.isSameItemSameComponents(this.stack, other.stack)
                    && this.stack.getDamageValue() == other.stack.getDamageValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = ItemStack.hashItemAndComponents(this.stack);
        return h ^ this.stack.getDamageValue();
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}