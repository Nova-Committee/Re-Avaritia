package com.avaritia.init.registry;

import com.avaritia.Const;

import com.avaritia.Avaritia;
import com.avaritia.api.common.crafting.ICompressorRecipe;
import com.avaritia.api.common.crafting.ITierCraftingRecipe;
import com.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 注册所有配方类型。
 */
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_TYPE, Const.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ITierCraftingRecipe>> CRAFTING_TABLE_RECIPE =
            recipe("crafting_table_recipe", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(Const.MOD_ID, "crafting_table_recipe")));
    public static final DeferredHolder<RecipeType<?>, RecipeType<ICompressorRecipe>> COMPRESSOR_RECIPE =
            recipe("compressor_recipe", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(Const.MOD_ID, "compressor_recipe")));
    public static final DeferredHolder<RecipeType<?>, RecipeType<ExtremeSmithingRecipe>> EXTREME_SMITHING_RECIPE =
            recipe("extreme_smithing_recipe", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(Const.MOD_ID, "extreme_smithing_recipe")));

    public static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
        return RECIPES.register(name, type);
    }
}
