package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

import static committee.nova.mods.avaritia.init.compat.kubejs.KjsUtils.optionalList;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 0:50
 * Name EternalSingularityRecipeSchema
 * Description
 */

public interface EternalSingularityRecipeSchema {
    RecipeKey<List<Ingredient>> INGREDIENTS = optionalList(IngredientComponent.INGREDIENT, "ingredients", ComponentRole.INPUT);
    RecipeKey<Boolean> CUSTOM = BooleanComponent.BOOLEAN.inputKey("custom").optional(false);
    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, CUSTOM)
            .constructor(INGREDIENTS, CUSTOM)
            .constructor(INGREDIENTS);
}
