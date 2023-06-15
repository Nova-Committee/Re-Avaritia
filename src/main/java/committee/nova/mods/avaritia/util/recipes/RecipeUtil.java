package committee.nova.mods.avaritia.util.recipes;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
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
            recipeManager.recipes = new HashMap(recipeManager.recipes);
            recipeManager.recipes.replaceAll((t, v) -> {
                return new HashMap((Map) recipeManager.recipes.get(t));
            });
        }

        if (recipeManager.byName instanceof ImmutableMap) {
            recipeManager.byName = new HashMap(recipeManager.byName);
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
        ((Map) getRecipeManager().recipes.computeIfAbsent(recipe.getType(), (t) -> new HashMap())).put(recipe.getId(), recipe);
        getRecipeManager().byName.put(recipe.getId(), recipe);
    }
}
