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
            if (stack.getItem().equals(other.stack.getItem()) && stack.getDamageValue() == other.stack.getDamageValue()) {
                if (stack.getTag() == null && other.stack.getTag() == null) {
                    return true;
                } else {
                    if (stack.getTag() == null ^ other.stack.getTag() == null) {
                        return false;
                    } else return stack.getTag().equals(other.stack.getTag());
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = stack.getItem().hashCode();
        if (stack.getTag() != null) {
            h ^= stack.getTag().hashCode();
        }
        return h ^ stack.getDamageValue();
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}
