package nova.committee.avaritia.api.common.item;

import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import nova.committee.avaritia.api.util.NBTHelper;

import java.util.Iterator;
import java.util.Set;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 14:18
 * Version: 1.0
 */
public class StackHelper {
    public StackHelper() {
    }

    public static ItemStack withSize(ItemStack stack, int size, boolean container) {
        if (size <= 0) {
            return container && stack.hasContainerItem() ? stack.getContainerItem() : ItemStack.EMPTY;
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
            return !stack1.isEmpty() && stack1.sameItem(stack2);
        }
    }

    public static boolean areStacksEqual(ItemStack stack1, ItemStack stack2) {
        return areItemsEqual(stack1, stack2) && ItemStack.tagMatches(stack1, stack2);
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
            Set<String> stack1Keys = NBTHelper.getTagCompound(stack1).getAllKeys();
            Set<String> stack2Keys = NBTHelper.getTagCompound(stack2).getAllKeys();
            Iterator var4 = stack1Keys.iterator();

            String key;
            do {
                if (!var4.hasNext()) {
                    return true;
                }

                key = (String) var4.next();
                if (!stack2Keys.contains(key)) {
                    return false;
                }
            } while (NbtUtils.compareNbt(NBTHelper.getTag(stack1, key), NBTHelper.getTag(stack2, key), true));

            return false;
        }
    }
}
