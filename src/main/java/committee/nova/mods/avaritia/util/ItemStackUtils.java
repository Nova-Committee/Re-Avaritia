package committee.nova.mods.avaritia.util;

import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 14:18
 * Version: 1.0
 */
public class ItemStackUtils {
    public ItemStackUtils() {
    }

    public static ItemStack mapEquals(ItemStack stack, Map<ItemStack, Integer> map) {
        for (ItemStack itemStack : map.keySet()) {
            if (itemStack.getItem() == stack.getItem()) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack withSize(ItemStack stack, int size, boolean container) {
        if (size <= 0) {
            return container && stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY;
        } else {
            stack = stack.copy();
            stack.setCount(size);
            return stack;
        }
    }

    public static ItemStack grow(ItemStack stack, int amount) {
        return withSize(stack, stack.getCount() + amount, false);
    }

    public static ItemStack shrink(ItemStack stack, int amount, boolean container) {
        return stack.isEmpty() ? ItemStack.EMPTY : withSize(stack, stack.getCount() - amount, container);
    }

    public static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() && stack2.isEmpty()) {
            return true;
        } else {
            return !stack1.isEmpty() && stack1.is(stack2.getItem());
        }
    }

    public static boolean areStacksEqual(ItemStack stack1, ItemStack stack2) {
        return areItemsEqual(stack1, stack2) && ItemStack.isSameItemSameTags(stack1, stack2);
    }

    public static boolean canCombineStacks(ItemStack stack1, ItemStack stack2) {
        if (!stack1.isEmpty() && stack2.isEmpty()) {
            return true;
        } else {
            return areStacksEqual(stack1, stack2) && stack1.getCount() + stack2.getCount() <= stack1.getMaxStackSize();
        }
    }

    public static ItemStack combineStacks(ItemStack stack1, ItemStack stack2) {
        return stack1.isEmpty() ? stack2.copy() : grow(stack1, stack2.getCount());
    }

    public static boolean compareTags(ItemStack stack1, ItemStack stack2) {
        if (!stack1.hasTag()) {
            return true;
        } else if (stack1.hasTag() && !stack2.hasTag()) {
            return false;
        } else {
            Set<String> stack1Keys = NBTUtils.getTagCompound(stack1).getAllKeys();
            Set<String> stack2Keys = NBTUtils.getTagCompound(stack2).getAllKeys();
            Iterator<String> iterator = stack1Keys.iterator();

            String key;
            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                key = iterator.next();
                if (!stack2Keys.contains(key)) {
                    return false;
                }
            } while (NbtUtils.compareNbt(NBTUtils.getTag(stack1, key), NBTUtils.getTag(stack2, key), true));

            return false;
        }
    }
}
