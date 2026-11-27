package committee.nova.mods.avaritia.api.utils;

import committee.nova.mods.avaritia.common.component.InfinityContainerContents;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class NBTUtils {

    public static void writeClusterItems(ItemStack stack, ItemStack[] items) {
        List<ItemStack> list = new ArrayList<>();

        for (ItemStack item : items) {
            list.add(item.copy());
        }

        InfinityContainerContents contents =
                InfinityContainerContents.fromItems(list);

        stack.set(ModDataComponents.MATTER_CLUSTER.get(), contents);
    }

    public static void readClusterItems(ItemStack[] target, InfinityContainerContents contents) {
        if (contents == null) return;

        var list = contents.getItems();
        for (int i = 0; i < target.length && i < list.size(); i++) {
            target[i] = list.get(i).copy();
        }
    }

    public static InfinityContainerContents getClusterItems(ItemStack stack) {
        return stack.get(ModDataComponents.MATTER_CLUSTER.get());
    }

    public static boolean hasClusterItems(ItemStack stack) {
        return stack.has(ModDataComponents.MATTER_CLUSTER.get());
    }

    public static void clearClusterItems(ItemStack stack) {
        stack.remove(ModDataComponents.MATTER_CLUSTER.get());
    }
}
