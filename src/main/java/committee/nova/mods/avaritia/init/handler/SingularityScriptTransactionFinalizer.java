package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.MinecraftForge;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Final reload barrier that commits script operations and regenerates internal recipes. */
final class SingularityScriptTransactionFinalizer extends SimplePreparableReloadListener<Void> {
    private static Map<ResourceLocation, Recipe<?>> generatedRecipes = Map.of();

    private final RecipeManager recipeManager;

    SingularityScriptTransactionFinalizer(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        CopyOnWriteArrayList<Recipe<?>> recipes = new CopyOnWriteArrayList<>();
        if (!SingularityReloadListener.INSTANCE.finalizeScriptTransaction(() -> {
            MinecraftForge.EVENT_BUS.post(new RegisterRecipesEvent(this.recipeManager, recipes));
            replaceGeneratedRecipes(recipes);
        })) {
            return;
        }
        Const.LOGGER.info("Singularity: Finalized script transaction and regenerated {} internal recipes",
                recipes.size());
    }

    private void replaceGeneratedRecipes(Iterable<Recipe<?>> replacements) {
        Map<ResourceLocation, Recipe<?>> byName = new LinkedHashMap<>();
        this.recipeManager.getRecipes().forEach(recipe -> byName.put(recipe.getId(), recipe));

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

        this.recipeManager.replaceRecipes(byName.values());
        generatedRecipes = Map.copyOf(nextGeneratedRecipes);
    }
}
