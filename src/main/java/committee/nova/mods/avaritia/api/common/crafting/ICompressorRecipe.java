package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;

public interface ICompressorRecipe extends Recipe<CraftingInput> {
    int getInputCount();

    int getTimeCost();

}
