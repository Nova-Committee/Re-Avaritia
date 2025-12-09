package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 0:50
 * Name EternalSingularityRecipeSchema
 * Description
 */

public interface EternalSingularityRecipeSchema {
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeSchema SCHEMA = new RecipeSchema(RecipeJS.class, RecipeJS::new, INGREDIENTS);
}
