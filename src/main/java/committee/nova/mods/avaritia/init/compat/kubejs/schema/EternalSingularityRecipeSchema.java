package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
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
    RecipeKey<String> GROUP = StringComponent.STRING
            .key("group", ComponentRole.INPUT)
            .optional("default");
    RecipeKey<List<Ingredient>> INGREDIENTS = optionalList(IngredientComponent.INGREDIENT, "ingredients", ComponentRole.INPUT);
    RecipeKey<Integer> COUNT = NumberComponent.INT.inputKey("count").optional(1);
    RecipeSchema SCHEMA = new RecipeSchema(GROUP, INGREDIENTS, COUNT)
            .constructor(GROUP, INGREDIENTS, COUNT)
            .constructor(INGREDIENTS, COUNT)
            ;
}
