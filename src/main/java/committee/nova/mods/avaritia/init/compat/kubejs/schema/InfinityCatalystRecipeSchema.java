package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.compat.kubejs.ModKubeRecipe;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.IntBounds;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static committee.nova.mods.avaritia.init.compat.kubejs.KjsUtils.optionalList;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 0:50
 * Name CompressRecipeSchema
 * Description
 */


public interface InfinityCatalystRecipeSchema {
    RecipeKey<String> GROUP = StringComponent.STRING
            .key("group", ComponentRole.INPUT)
            .optional("default");

    RecipeKey<List<Ingredient>> INGREDIENTS = optionalList(IngredientComponent.INGREDIENT, "ingredients", ComponentRole.INPUT);
    RecipeKey<Integer> COUNT = NumberComponent.INT.inputKey("count").optional(1);

    
    RecipeSchema SCHEMA = new RecipeSchema(GROUP, INGREDIENTS, COUNT)
            .constructor(GROUP, INGREDIENTS, COUNT)
            .constructor(GROUP, INGREDIENTS)
            ;
}
