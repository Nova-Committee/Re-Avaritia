package committee.nova.mods.avaritia.init.compact.rei.display;

import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.Collections;
import java.util.Optional;
public class ExtremeTableShapedDisplay extends ExtremeTableDisplay<ShapedExtremeCraftingRecipe> {

    public ExtremeTableShapedDisplay(ShapedExtremeCraftingRecipe recipe) {
        super(EntryIngredients.ofIngredients(recipe.getIngredients()), Collections.singletonList(EntryIngredients.of(recipe.getResultItem(null))), Optional.of(recipe));
    }

    @Override
    public int getWidth() {
        return 9;
    }

    @Override
    public int getHeight() {
        return 9;
    }
}