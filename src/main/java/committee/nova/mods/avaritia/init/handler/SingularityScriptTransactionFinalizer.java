package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Commits script operations after every resource reload listener has completed. */
public final class SingularityScriptTransactionFinalizer {
    private static Map<ResourceLocation, Recipe<?>> generatedRecipes = Map.of();

    private SingularityScriptTransactionFinalizer() {}

    public static void finalizeReload(RecipeManager recipeManager) {
        CopyOnWriteArrayList<Recipe<?>> recipes = new CopyOnWriteArrayList<>();
        if (!SingularityReloadListener.INSTANCE.finalizeScriptTransaction(() -> {
            RegisterRecipesEvent event = new RegisterRecipesEvent(recipeManager, recipes);
            InternalRecipeHandler.onRegisterRecipes(event);
            RegisterRecipesEvent.post(event);
            replaceGeneratedRecipes(recipeManager, recipes);
        })) {
            return;
        }
        Const.LOGGER.info("Singularity: Finalized script transaction and regenerated {} internal recipes",
                recipes.size());
    }

    private static void replaceGeneratedRecipes(RecipeManager recipeManager, Iterable<Recipe<?>> replacements) {
        Map<ResourceLocation, Recipe<?>> byName = new LinkedHashMap<>();
        recipeManager.getRecipes().forEach(recipe -> byName.put(recipe.getId(), recipe));

        generatedRecipes.forEach((id, generatedRecipe) -> {
            if (byName.get(id) == generatedRecipe) {
                byName.remove(id);
            }
        });

        Map<ResourceLocation, Recipe<?>> nextGeneratedRecipes = new LinkedHashMap<>();
        for (Recipe<?> replacement : replacements) {
            ResourceLocation id = replacement.getId();
            byName.put(id, replacement);
            nextGeneratedRecipes.put(id, replacement);
        }

        recipeManager.replaceRecipes(byName.values());
        generatedRecipes = Map.copyOf(nextGeneratedRecipes);
    }
}
