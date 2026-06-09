package com.avaritia.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

import static com.avaritia.compat.kubejs.KjsUtils.optionalList;

public interface InfinityCatalystRecipeSchema {
    RecipeKey<String> GROUP = StringComponent.STRING
            .key("group", ComponentRole.INPUT)
            .optional("default");
    RecipeKey<List<Ingredient>> INGREDIENTS =
            optionalList(IngredientComponent.INGREDIENT, "ingredients", ComponentRole.INPUT);
    RecipeKey<Integer> COUNT = NumberComponent.INT.inputKey("count").optional(1);

    RecipeSchema SCHEMA = new RecipeSchema(GROUP, INGREDIENTS, COUNT)
            .constructor(GROUP, INGREDIENTS, COUNT)
            .constructor(GROUP, INGREDIENTS);
}
