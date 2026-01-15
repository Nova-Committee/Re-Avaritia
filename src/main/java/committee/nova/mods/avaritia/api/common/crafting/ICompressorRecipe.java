package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public interface ICompressorRecipe extends Recipe<Container> {
    Ingredient getInput();
    int getInputCount();
    int getTimeCost();

}
