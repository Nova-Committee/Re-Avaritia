package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

public interface ICompressorRecipe extends Recipe<CraftingInput> {
    @NotNull Ingredient getInput();

    int getInputCount();

    int getTimeCost();

}
