package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * 奇点物品与数据对象互转工具。
 */
public class SingularityUtils {
    public static Singularity getSingularity(Identifier id) {
        return SingularityReloadListener.INSTANCE.getSingularity(id);
    }

    public static Singularity getSingularity(ItemStack stack) {
        Identifier id = stack.get(ModDataComponents.SINGULARITY_ID.get());
        return id == null ? null : getSingularity(id);
    }

    public static ItemStack getItemForSingularity(Singularity singularity) {
        ItemStack stack = new ItemStack(ModItems.singularity.get());
        stack.set(ModDataComponents.SINGULARITY_ID.get(), singularity.getRegistryName());
        return stack;
    }
}
