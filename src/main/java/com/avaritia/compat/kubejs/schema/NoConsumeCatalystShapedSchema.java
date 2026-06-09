package com.avaritia.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.MapRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.IntBounds;
import dev.latvian.mods.kubejs.util.TinyMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

import static com.avaritia.compat.kubejs.KjsUtils.optionalList;

public interface NoConsumeCatalystShapedSchema {
    RecipeKey<List<String>> PATTERN = optionalList(StringComponent.OPTIONAL_STRING, "pattern", ComponentRole.INPUT);
    RecipeKey<TinyMap<Character, Ingredient>> KEY =
            MapRecipeComponent.patternOf(IngredientComponent.INGREDIENT, IntBounds.DEFAULT).inputKey("key");
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
    RecipeKey<Integer> TIER = NumberComponent.INT.inputKey("tier").optional(0);

    RecipeSchema SCHEMA = new RecipeSchema(PATTERN, KEY, RESULT, TIER)
            .constructor(PATTERN, KEY, RESULT, TIER);
}
