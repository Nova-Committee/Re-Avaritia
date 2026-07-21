package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Final reload barrier that commits script operations and regenerates internal recipes. */
final class SingularityScriptTransactionFinalizer extends SimplePreparableReloadListener<Void> {
    private static Map<ResourceLocation, RecipeHolder<?>> generatedRecipes = Map.of();

    private final RecipeManager recipeManager;
    private final HolderLookup.Provider registries;
    private final ICondition.IContext conditionContext;

    SingularityScriptTransactionFinalizer(RecipeManager recipeManager, HolderLookup.Provider registries,
                                          ICondition.IContext conditionContext) {
        this.recipeManager = recipeManager;
        this.registries = registries;
        this.conditionContext = conditionContext;
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        if (!SingularityReloadListener.INSTANCE.finalizeScriptTransaction(() -> {
            NeoForge.EVENT_BUS.post(new RegisterRecipesEvent(
                    this.recipeManager, recipes, this.registries, this.conditionContext));
            replaceGeneratedRecipes(recipes);
        })) {
            return;
        }
        Const.LOGGER.info("Singularity: Finalized script transaction and regenerated {} internal recipes",
                recipes.size());
    }

    private void replaceGeneratedRecipes(List<RecipeHolder<?>> replacements) {
        Map<ResourceLocation, RecipeHolder<?>> byName = new LinkedHashMap<>();
        this.recipeManager.getRecipes().forEach(recipe -> byName.put(recipe.id(), recipe));

        generatedRecipes.forEach((id, generatedRecipe) -> {
            if (byName.get(id) == generatedRecipe) {
                byName.remove(id);
            }
        });

        Map<ResourceLocation, RecipeHolder<?>> nextGeneratedRecipes = new LinkedHashMap<>();
        for (RecipeHolder<?> replacement : replacements) {
            byName.put(replacement.id(), replacement);
            nextGeneratedRecipes.put(replacement.id(), replacement);
        }

        this.recipeManager.replaceRecipes(byName.values());
        generatedRecipes = Map.copyOf(nextGeneratedRecipes);
    }
}
