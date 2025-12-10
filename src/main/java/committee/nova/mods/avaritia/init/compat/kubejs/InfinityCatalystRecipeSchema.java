package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 0:50
 * Name CompressRecipeSchema
 * Description
 */

public interface InfinityCatalystRecipeSchema {
    RecipeKey<String> GROUP = StringComponent.NON_EMPTY.key("group").optional("default");
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<Integer> COUNT = NumberComponent.INT.key("count").optional(1);
    RecipeSchema SCHEMA = new RecipeSchema(RecipeJS.class, RecipeJS::new, GROUP, INGREDIENTS, COUNT);
}
