package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.item.ItemStack;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:39
 * Version: 1.0
 */
public class SingularityUtils {

    public static ItemStack getItemForSingularity(Singularity singularity) {
        var stack = new ItemStack(ModItems.singularity.get());
        stack.set(ModDataComponents.SINGULARITY_ID, singularity.getRegistryName());
        return stack;
    }

    public static Singularity getSingularity(ItemStack stack) {
        var id = stack.get(ModDataComponents.SINGULARITY_ID);
        if (id != null) {
            return SingularityReloadListener.INSTANCE.getSingularity(id);
        }
        return null;
    }

}
