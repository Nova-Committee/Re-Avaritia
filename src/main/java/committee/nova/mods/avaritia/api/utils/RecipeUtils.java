package committee.nova.mods.avaritia.api.utils;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import committee.nova.mods.avaritia.api.Lib;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 13:45
 * @Description:
 */
public class RecipeUtils {
    private static @Nullable WeakReference<RecipeManager> recipeManager;

    public RecipeUtils() {
    }

    public static RecipeManager getRecipeManager() throws IllegalStateException {
        if (recipeManager != null && recipeManager.get() != null) {
            return recipeManager.get();
        } else {
            throw new IllegalStateException("Recipe Manager is not available");
        }
    }

    @ApiStatus.Internal
    public static void setRecipeManager(RecipeManager manager) {
        recipeManager = new WeakReference<>(manager);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type) {
        return byType(getRecipeManager(), type);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeManager manager, RecipeType<T> type) {
        return manager.byType(type);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> List<T> byTypeValues(RecipeType<T> type) {
        return byTypeValues(getRecipeManager(), type);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> List<T> byTypeValues(RecipeManager manager, RecipeType<T> type) {
        return byType(manager, type).stream().map(RecipeHolder::value).toList();
    }

    public static Collection<RecipeHolder<?>> getAllRecipes() {
        return getRecipeManager().getRecipes();
    }

    @ApiStatus.Internal
    public static void fireRecipeManagerLoadingEvent(RecipeManager manager, ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> map, ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ArrayList<RecipeHolder<?>> recipes = new ArrayList<>();

        try {
            NeoForge.EVENT_BUS.post(new RegisterRecipesEvent(manager, recipes));
        } catch (Exception e) {
            Lib.LOGGER.error("An error occurred while firing RecipeManagerLoadingEvent", e);
        }

        for(RecipeHolder<?> recipe : recipes) {
            map.put(recipe.value().getType(), recipe);
            builder.put(recipe.id(), recipe);
        }

        Lib.LOGGER.info("Registered {} recipes in {} ms", recipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
}
