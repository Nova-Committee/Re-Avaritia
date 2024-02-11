package committee.nova.mods.avaritia.util;

import com.google.common.collect.ImmutableMap;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
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

@Mod.EventBusSubscriber
public class RecipeUtil {
    private static RecipeManager recipeManager;

    public RecipeUtil() {
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        recipeManager = event.getServerResources().getRecipeManager();
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        recipeManager = event.getRecipeManager();
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

    public static Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> getRecipes() {
        return getRecipeManager().recipes;
    }


    public static void addRecipe(RecipeHolder<?> recipe) {
        getRecipeManager().recipes.computeIfAbsent(recipe.value().getType(), (t) -> new HashMap<>()).put(recipe.id(), recipe);
        getRecipeManager().byName.put(recipe.id(), recipe);
    }

    public static Recipe<?> getRecipe(ResourceLocation name) {
       return getRecipeManager().byName.get(name).value();
    }

//    @ApiStatus.Experimental
//    public static ShapelessExtremeCraftingRecipe addExtremeShapelessRecipe(ItemStack result, List<ItemStack> ingredients) {
//        List<ItemStack> arraylist = new ArrayList<>();
//
//        for (ItemStack stack : ingredients) {
//            if (stack != null) {
//                arraylist.add(stack.copy());
//            } else {
//                throw new RuntimeException("Invalid shapeless recipes!");
//            }
//        }
//
//        return new ShapelessExtremeCraftingRecipe(BuiltInRegistries.ITEM.getKey(result.getItem()), getList(arraylist), result);
//    }

    private static NonNullList<Ingredient> getList(List<ItemStack> arrayList) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack stack : arrayList) {
            ingredients.add(Ingredient.of(stack));
        }
        return ingredients;
    }
}
