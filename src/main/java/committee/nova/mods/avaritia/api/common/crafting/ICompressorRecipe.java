package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 13:44
 * Name ICompressorRecipe
 * Description
 */

public interface ICompressorRecipe extends Recipe<Container> {
    int getInputCount();

    int getTimeRequire();
}
