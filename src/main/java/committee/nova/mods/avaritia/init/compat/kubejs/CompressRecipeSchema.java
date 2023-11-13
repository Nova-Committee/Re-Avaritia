package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * Name: Avaritia-forge / CompressRecipeSchema
 * Author: cnlimiter
 * CreateTime: 2023/11/13 22:52
 * Description:
 */

public interface CompressRecipeSchema {
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<OutputItem> OUTPUT = ItemComponents.OUTPUT.key("result");
    RecipeKey<Long> COMPRESS_TIME = TimeComponent.TICKS.key("timeRequired").optional(240L);
    RecipeKey<Integer> MATERIAL_COUNT = NumberComponent.INT.key("materialCount").optional(1000);
    RecipeSchema SCHEMA = new RecipeSchema(RecipeJS.class, RecipeJS::new, INGREDIENTS, OUTPUT, MATERIAL_COUNT, COMPRESS_TIME);

}
