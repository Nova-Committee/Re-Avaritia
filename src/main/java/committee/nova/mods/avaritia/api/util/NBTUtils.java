package committee.nova.mods.avaritia.api.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:40
 * Version: 1.0
 */
public class NBTUtils {
    public static String getString(ItemStack stack, String key) {
        return stack.hasTag() ? getTagCompound(stack).getString(key) : "";
    }

    public static CompoundTag getTagCompound(ItemStack stack) {
        validateCompound(stack);
        return stack.getTag();
    }

    public static void validateCompound(ItemStack stack) {
        if (!stack.hasTag()) {
            CompoundTag tag = new CompoundTag();
            stack.setTag(tag);
        }
    }

    public static Tag getTag(ItemStack stack, String key) {
        return stack.hasTag() ? getTagCompound(stack).get(key) : null;
    }

    public static ListTag writeToTag(ItemStack[] items) {
        ListTag tagList = new ListTag();
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                tagList.add(item.save(new CompoundTag()));
            }
        }
        return tagList;
    }

    public static void readFromTag(ItemStack[] items, ListTag tagList) {
        for (int i = 0; i < tagList.size(); ++i) {
            items[i] = ItemStack.of(tagList.getCompound(i));
        }
    }

}
