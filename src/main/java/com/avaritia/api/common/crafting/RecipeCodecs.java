package com.avaritia.api.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public final class RecipeCodecs {
    public static final Codec<ItemStack> STRICT_ITEM_STACK = ItemStack.CODEC.validate(ItemStack::validateStrict);

    private RecipeCodecs() {
    }

    public static Codec<NonNullList<Ingredient>> ingredientList(int max, boolean allowEmpty, String recipeName) {
        return Ingredient.CODEC
                .listOf()
                .flatXmap(
                        ingredients -> validateIngredients(ingredients, max, allowEmpty, recipeName),
                        ingredients -> DataResult.success(List.copyOf(ingredients))
                );
    }

    private static DataResult<NonNullList<Ingredient>> validateIngredients(List<Ingredient> ingredients, int max, boolean allowEmpty, String recipeName) {
        if (!allowEmpty && ingredients.isEmpty()) {
            return DataResult.error(() -> "No ingredients for " + recipeName);
        }
        if (ingredients.size() > max) {
            return DataResult.error(() -> "Too many ingredients for %s. The maximum is: %s".formatted(recipeName, max));
        }
        return DataResult.success(NonNullList.copyOf(ingredients));
    }
}
