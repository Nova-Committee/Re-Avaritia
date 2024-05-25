package committee.nova.mods.avaritia.util;

import com.google.common.collect.ImmutableMap;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.init.event.AddReloadListenerEvent;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import io.github.fabricators_of_create.porting_lib.entity.events.OnDatapackSyncCallback;
import io.github.fabricators_of_create.porting_lib.event.common.AddPackFindersCallback;
import io.github.fabricators_of_create.porting_lib.event.common.RecipesUpdatedCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author cnlimiter
 * CreateTime 2023/6/15 20:50
 * Name RecipeUtil
 * Description
 */

public class RecipeUtil {
    private static RecipeManager recipeManager;

    public static void setRecipeManager(RecipeManager recipeManager) {
        RecipeUtil.recipeManager = recipeManager;
    }

    public static void init() {
        onAddReloadListeners();
        onRecipesUpdated();
    }

    public static void onAddReloadListeners() {
        AddReloadListenerEvent.RELOAD.register((event) -> {
            recipeManager = event.getServerResources().getRecipeManager();
        });
    }

    public static void onRecipesUpdated() {
        RecipesUpdatedCallback.EVENT.register(manager -> recipeManager = manager);
    }

    public static RecipeManager getRecipeManager() {
        if (recipeManager.recipes instanceof ImmutableMap) {
            recipeManager.recipes = new HashMap<>(recipeManager.recipes);
            recipeManager.recipes.replaceAll((t, v) -> new HashMap<>(recipeManager.recipes.get(t)));
        }

        if (recipeManager.byName instanceof ImmutableMap) {
            recipeManager.byName = new HashMap<>(recipeManager.byName);
        }

        return recipeManager;
    }

    public static Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> getRecipes() {
        return getRecipeManager().recipes;
    }

    public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> getRecipes(RecipeType<T> type) {
        return getRecipeManager().byType(type);
    }

    public static void addRecipe(Recipe<?> recipe) {
        getRecipeManager().recipes.computeIfAbsent(recipe.getType(), (t) -> new HashMap<>()).put(recipe.getId(), recipe);
        getRecipeManager().byName.put(recipe.getId(), recipe);
    }

    public static void addRecipe(RecipeManager manager, Recipe<?> recipe) {
        manager.recipes.computeIfAbsent(recipe.getType(), (t) -> new HashMap<>()).put(recipe.getId(), recipe);
        manager.byName.put(recipe.getId(), recipe);
    }

    public static Recipe<?> getRecipe(ResourceLocation name) {
       return getRecipeManager().byName.get(name);
    }

    @ApiStatus.Experimental
    public static ShapelessExtremeCraftingRecipe addExtremeShapelessRecipe(ItemStack result, List<ItemStack> ingredients) {
        List<ItemStack> arraylist = new ArrayList<>();

        for (ItemStack stack : ingredients) {
            if (stack != null) {
                arraylist.add(stack.copy());
            } else {
                throw new RuntimeException("Invalid shapeless recipes!");
            }
        }

        return new ShapelessExtremeCraftingRecipe(BuiltInRegistries.ITEM.getKey(result.getItem()), getList(arraylist), result);
    }

    private static NonNullList<Ingredient> getList(List<ItemStack> arrayList) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack stack : arrayList) {
            ingredients.add(Ingredient.of(stack));
        }
        return ingredients;
    }
}
