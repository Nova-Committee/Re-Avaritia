package committee.nova.mods.avaritia.mixin;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import com.google.common.base.Stopwatch;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fires Avaritia's runtime recipe extension event before the recipe map is finalized.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin extends ContextAwareReloadListener {
    @Shadow
    public RecipeMap recipes;

    @Inject(
            method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void avaritia$apply(
            RecipeMap preparedRecipes,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller,
            CallbackInfo ci,
            @Local(argsOnly = true) RecipeMap recipeMap
    ) {
        RecipeManager manager = (RecipeManager) (Object) this;
        Const.LOGGER.info("Avaritia: Loading recipes...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<RecipeHolder<?>> recipes = new ArrayList<>(recipeMap.values());
        int vanillaRecipeCount = recipes.size();

        try {
            NeoForge.EVENT_BUS.post(new RegisterRecipesEvent(manager, recipes, this.getRegistryLookup(), this.getContext()));
        } catch (Exception e) {
            Const.LOGGER.error("Avaritia: An error occurred while firing RegisterRecipesEvent", e);
        }

        int generatedRecipeCount = recipes.size() - vanillaRecipeCount;
        if (generatedRecipeCount > 0) {
            this.recipes = RecipeMap.create(recipes);
            ci.cancel();
        }

        Const.LOGGER.info(
                "Avaritia: Registered {} recipes in {} ms",
                generatedRecipeCount,
                stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)
        );
    }
}
