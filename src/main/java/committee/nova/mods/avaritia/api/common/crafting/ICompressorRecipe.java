package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

public interface ICompressorRecipe extends Recipe<CraftingInput> {
    int getInputCount();

    int getTimeCost();

}
