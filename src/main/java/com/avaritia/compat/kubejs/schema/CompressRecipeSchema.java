package com.avaritia.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface CompressRecipeSchema {
    RecipeKey<Ingredient> INGREDIENT = IngredientComponent.INGREDIENT.inputKey("ingredient");
    RecipeKey<ItemStack> OUTPUT = ItemStackComponent.ITEM_STACK.outputKey("result");
    RecipeKey<Integer> INPUT_COUNT = NumberComponent.INT.inputKey("inputCount").optional(1000);
    RecipeKey<Integer> TIME_COST = NumberComponent.INT.inputKey("timeCost").optional(240);

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENT, OUTPUT, INPUT_COUNT, TIME_COST);
}
