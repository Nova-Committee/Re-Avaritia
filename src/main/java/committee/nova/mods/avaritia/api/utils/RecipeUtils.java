package committee.nova.mods.avaritia.api.utils;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.Lib;
import committee.nova.mods.avaritia.api.init.event.RecipeManagerLoadingEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 13:45
 * @Description:
 */
@Mod.EventBusSubscriber(modid = Const.MOD_ID)
public class RecipeUtils {
    private static @Nullable RecipeManager recipeManager;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        setRecipeManager(event.getServerResources().getRecipeManager());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        setRecipeManager(event.getRecipeManager());
    }

    @ApiStatus.Internal
    public static void setRecipeManager(RecipeManager manager) {
        recipeManager = manager;
    }

    public static RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public static void addRecipe(Recipe<?> recipe) {
        if (recipeManager.recipes instanceof ImmutableMap) {
            recipeManager.recipes = new HashMap<>(recipeManager.recipes);
            recipeManager.recipes.replaceAll((t, v) -> new HashMap<>(recipeManager.recipes.get(t)));
        }

        if (recipeManager.byName instanceof ImmutableMap) {
            recipeManager.byName = new HashMap<>(recipeManager.byName);
        }

        getRecipeManager().recipes.computeIfAbsent(recipe.getType(), t -> new HashMap<>()).put(recipe.getId(), recipe);
        getRecipeManager().byName.put(recipe.getId(), recipe);
    }

    public static Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> getRecipes() {
        return getRecipeManager().recipes;
    }

    public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> getRecipes(RecipeType<T> type) {
        return getRecipeManager().byType(type);
    }

    // map parameter uses Object because custom servers replace the ImmutableMap.Builder with a different map type
    public static void fireRecipeManagerLoadedEvent(RecipeManager manager, Map<RecipeType<?>, Object> map, ImmutableMap.Builder<ResourceLocation, Recipe<?>> builder) {
        var stopwatch = Stopwatch.createStarted();
        var recipes = new ArrayList<Recipe<?>>();

        try {
            MinecraftForge.EVENT_BUS.post(new RecipeManagerLoadingEvent(manager, recipes));
        } catch (Exception e) {
            Lib.LOGGER.error("An error occurred while firing RecipeManagerLoadingEvent", e);
        }

        for (var recipe : recipes) {
            var recipeType = recipe.getType();
            var recipeId = recipe.getId();
            var recipeMap = map.get(recipeType);

            // Mohist (and I think another custom server) change it to this because annoying hacky hybrid server reasons
            if (recipeMap instanceof Object2ObjectLinkedOpenHashMap<?, ?>) {
                var o2oRecipeMap = (Object2ObjectLinkedOpenHashMap<Object, Object>) recipeMap;
                o2oRecipeMap.put(recipeId, recipe);
            } else if (recipeMap instanceof ImmutableMap.Builder<?, ?>) {
                var recipeMapBuilder = (ImmutableMap.Builder<Object, Object>) recipeMap;
                recipeMapBuilder.put(recipeId, recipe);
            } else if (recipeMap == null) {
                var recipeMapBuilder = ImmutableMap.builder();
                recipeMapBuilder.put(recipeId, recipe);
                map.put(recipeType, recipeMapBuilder);
            } else {
                Lib.LOGGER.error("Failed to register recipe {} to map of type {}", recipeId, recipeMap.getClass());
            }

            builder.put(recipeId, recipe);
        }

        Lib.LOGGER.info("Registered {} recipes in {} ms", recipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    public static void fireRecipeManagerLoadedEventKubeJSEdition(RecipeManager manager, Map<ResourceLocation, Recipe<?>> recipesByName) {
        var stopwatch = Stopwatch.createStarted();
        var recipes = new ArrayList<Recipe<?>>();

        try {
            MinecraftForge.EVENT_BUS.post(new RecipeManagerLoadingEvent(manager, recipes));
        } catch (Exception e) {
            Lib.LOGGER.error("An error occurred while firing RecipeManagerLoadingEvent", e);
        }

        for (var recipe : recipes) {
            recipesByName.put(recipe.getId(), recipe);
        }

        Lib.LOGGER.info("Registered {} recipes in {} ms (KubeJS mode)", recipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
}
