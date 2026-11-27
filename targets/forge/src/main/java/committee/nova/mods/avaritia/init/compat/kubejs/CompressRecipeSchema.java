package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 0:50
 * Name CompressRecipeSchema
 * Description
 */

public interface CompressRecipeSchema {
    RecipeKey<InputItem> INGREDIENT = ItemComponents.INPUT.key("ingredient");
    RecipeKey<OutputItem> OUTPUT = ItemComponents.OUTPUT.key("result");
    RecipeKey<Integer> INPUT_COUNT = NumberComponent.INT.key("inputCount").optional(1000);
    RecipeKey<Integer> TIME_COST = NumberComponent.INT.key("timeCost").optional(240);
    RecipeSchema SCHEMA = new RecipeSchema(RecipeJS.class, RecipeJS::new, INGREDIENT, OUTPUT, INPUT_COUNT, TIME_COST);
}
