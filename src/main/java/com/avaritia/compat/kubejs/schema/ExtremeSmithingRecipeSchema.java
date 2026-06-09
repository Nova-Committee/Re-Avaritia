package com.avaritia.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import static com.avaritia.compat.kubejs.KjsUtils.COMPAT_INGREDIENT;
import static com.avaritia.compat.kubejs.KjsUtils.COMPAT_ITEM_STACK;

public interface ExtremeSmithingRecipeSchema {
    RecipeKey<ItemStack> RESULT = COMPAT_ITEM_STACK.outputKey("result");
    RecipeKey<Ingredient> TEMPLATE = COMPAT_INGREDIENT.inputKey("template");
    RecipeKey<Ingredient> BASE = COMPAT_INGREDIENT.inputKey("base");
    RecipeKey<Ingredient> ADDITION = COMPAT_INGREDIENT.inputKey("addition");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, TEMPLATE, BASE, ADDITION)
            .constructor(RESULT, TEMPLATE, BASE, ADDITION);
}
