package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

public interface ICompressorRecipe extends Recipe<Container> {
    int getInputCount();

    int getTimeRequire();
}
