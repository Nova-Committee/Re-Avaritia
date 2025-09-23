package committee.nova.mods.avaritia.api.util.recipe;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import committee.nova.mods.avaritia.api.Lib;
import committee.nova.mods.avaritia.api.init.event.RecipeManagerLoadingEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 13:45
 * @Description:
 */
public class RecipeUtils {
    @Nullable
    private static WeakReference<RecipeManager> recipeManager;

    public static RecipeManager getRecipeManager() throws IllegalStateException {
        if (recipeManager == null || recipeManager.get() == null) {
            throw new IllegalStateException("Recipe Manager is not available");
        }

        return recipeManager.get();
    }

    @ApiStatus.Internal
    public static void setRecipeManager(RecipeManager manager) {
        recipeManager = new WeakReference<>(manager);
    }

    public static <I extends Container, T extends Recipe<I>> Map<ResourceLocation, T> byType(RecipeType<T> type) {
        return byType(getRecipeManager(), type);
    }

    public static <I extends Container, T extends Recipe<I>> Map<ResourceLocation, T> byType(RecipeManager manager, RecipeType<T> type) {
        return manager.byType(type);
    }

    public static Collection<Recipe<?>> getAllRecipes() {
        return getRecipeManager().getRecipes();
    }

    // map parameter uses Object because custom servers replace the ImmutableMap.Builder with a different map type
    public static void fireRecipeManagerLoadedEvent(RecipeManager manager, ICondition.IContext context, Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> map,
                                                    Map<ResourceLocation, Recipe<?>> builder) {
        var stopwatch = Stopwatch.createStarted();
        var recipes = new ArrayList<Recipe<?>>();

        try {
            MinecraftForge.EVENT_BUS.post(new RecipeManagerLoadingEvent(manager, context, recipes));
        } catch (Exception e) {
            Lib.LOGGER.error("An error occurred while firing RecipeManagerLoadingEvent", e);
        }

        for (var recipe : recipes) {
            RecipeType<?> recipeType = recipe.getType();
            var recipeMap = map.get(recipeType);

            if (!map.containsKey(recipeType)) {
                recipeMap = Maps.newConcurrentMap();
            }

            recipeMap.put(recipe.getId(), recipe);
            map.put(recipe.getType(), recipeMap);
            builder.put(recipe.getId(), recipe);
        }

        Lib.LOGGER.info("Registered {} recipes in {} ms", recipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

//    public static void fireRecipeManagerLoadedEventKubeJSEdition(RecipeManager manager, Map<ResourceLocation, Recipe<?>> recipesByName) {
//        var stopwatch = Stopwatch.createStarted();
//        var recipes = new ArrayList<Recipe<?>>();
//
//        try {
//            MinecraftForge.EVENT_BUS.post(new RecipeManagerLoadingEvent(manager, recipes));
//        } catch (Exception e) {
//            Lib.LOGGER.error("An error occurred while firing RecipeManagerLoadingEvent", e);
//        }
//
//        for (var recipe : recipes) {
//            recipesByName.put(recipe.getId(), recipe);
//        }
//
//        Lib.LOGGER.info("Registered {} recipes in {} ms (KubeJS mode)", recipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
//    }
}
