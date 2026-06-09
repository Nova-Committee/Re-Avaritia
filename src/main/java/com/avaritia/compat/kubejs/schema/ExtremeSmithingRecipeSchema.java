package com.avaritia.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface ExtremeSmithingRecipeSchema {
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
    RecipeKey<Ingredient> TEMPLATE = IngredientComponent.INGREDIENT.inputKey("template");
    RecipeKey<Ingredient> BASE = IngredientComponent.INGREDIENT.inputKey("base");
    RecipeKey<Ingredient> ADDITION = IngredientComponent.INGREDIENT.inputKey("addition");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, TEMPLATE, BASE, ADDITION)
            .constructor(RESULT, TEMPLATE, BASE, ADDITION);
}
