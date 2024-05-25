package committee.nova.mods.avaritia.init.compact.rei.display;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.init.compact.rei.ServerREIPlugin;
import lombok.Getter;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.RecipeManagerContext;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
public class CompressorDisplay extends BasicDisplay {

    private final int timeCost;

    public CompressorDisplay(CompressorRecipe recipe) {
        this(EntryIngredients.ofIngredients(recipe.getIngredients()), Collections.singletonList(EntryIngredients.of(recipe.getResultItem(null))), recipe, recipe.getTimeRequire());

    }

    public CompressorDisplay(List<EntryIngredient> input, List<EntryIngredient> output, CompoundTag tag) {
        this(input, output, (CompressorRecipe) RecipeManagerContext.getInstance().byId(tag, "location"),
                tag.getInt("timeCost"));
    }

    public CompressorDisplay(List<EntryIngredient> input, List<EntryIngredient> output, CompressorRecipe recipe, int timeCost) {
        super(input, output, Optional.ofNullable(recipe).map(Recipe::getId));
        this.timeCost = timeCost;
    }

     Serializer<CompressorDisplay> serializer() {
         return Serializer.ofRecipeLess(CompressorDisplay::new, (display, tag) -> {
             tag.putInt("timeCost", display.getTimeCost());
         });
    }


    @Override
    public List<EntryIngredient> getInputEntries() {
        return this.inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return this.outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ServerREIPlugin.COMPRESSOR;
    }

}
