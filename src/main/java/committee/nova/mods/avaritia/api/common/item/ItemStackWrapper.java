package committee.nova.mods.avaritia.api.common.item;

import net.minecraft.world.item.ItemStack;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:16
 * Version: 1.0
 */
public class ItemStackWrapper {
    public final ItemStack stack;

    public ItemStackWrapper(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object otherobj) {
        if (otherobj instanceof ItemStackWrapper) {
            ItemStackWrapper other = (ItemStackWrapper) otherobj;

            if (stack.getItem().equals(other.stack.getItem()) && stack.getDamageValue() == other.stack.getDamageValue()) {

                if (stack.getTag() == null && other.stack.getTag() == null) {
                    return true;
                } else {
                    if (stack.getTag() == null ^ other.stack.getTag() == null) {
                        return false;
                    } else if (stack.getTag().equals(other.stack.getTag())) {
                        return true;
                    }
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
