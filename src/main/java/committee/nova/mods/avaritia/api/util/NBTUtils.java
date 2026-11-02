package committee.nova.mods.avaritia.api.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 12:40
 * Version: 1.0
 */
public class NBTUtils {
    public static String getString(ItemStack stack, String key) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag().getString(key).orElse("") : "";
    }

    public static CompoundTag getTagCompound(ItemStack stack) {
        validateCompound(stack);
        return stack.get(DataComponents.CUSTOM_DATA).copyTag();
    }

    public static void validateCompound(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
        }
    }

    public static Tag getTag(ItemStack stack, String key) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag().get(key) : null;
    }

    public static ListTag writeToTag(HolderLookup.Provider provider, ItemStack[] items) {
        ListTag tagList = new ListTag();
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                ItemStack.CODEC.encodeStart(ops, item).result().ifPresent(tagList::add);
            }
        }
        return tagList;
    }

    public static void readFromTag(HolderLookup.Provider provider, ItemStack[] items, ListTag tagList) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        for (int i = 0; i < tagList.size(); ++i) {
            int index = i;
            ItemStack.CODEC.parse(ops, tagList.get(i)).result().ifPresent(stack -> items[index] = stack);
        }
    }

}
