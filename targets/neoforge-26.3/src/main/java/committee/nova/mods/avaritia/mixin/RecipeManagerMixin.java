package committee.nova.mods.avaritia.mixin;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import com.google.common.base.Stopwatch;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionContext;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Fires Avaritia's runtime recipe extension event before the recipe map is finalized.
 *
 * 26.3 起 RecipeManager 不再是重载监听器（apply 方法与 RecipeMap 入参已移除），配方随数据包注册表加载；
 * 因此改为在 {@link RecipeManager#finalizeRecipeLoading} 头部注入，此时所有重载监听器
 * （含奇点数据快照）均已应用完成，原有"数据快照先于配方重建"的时序天然成立。
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow
    @Final
    @Mutable
    private RecipeMap recipes;

    @Shadow
    @Final
    @Mutable
    private Collection<RecipeHolder<?>> learnableRecipes;

    @Unique
    private HolderLookup.Provider avaritia$registryLookup;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void avaritia$captureRegistryLookup(HolderLookup.Provider registries, CallbackInfo ci) {
        this.avaritia$registryLookup = registries;
    }

    @Inject(
            method = "finalizeRecipeLoading",
            at = @At("HEAD")
    )
    private void avaritia$finalizeRecipes(FeatureFlagSet enabledFlags, CallbackInfo ci) {
        RecipeManager manager = (RecipeManager) (Object) this;
        Const.LOGGER.info("Avaritia: Loading recipes...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<RecipeHolder<?>> recipes = new ArrayList<>(this.recipes.values());
        List<RecipeHolder<?>> originalRecipes = List.copyOf(recipes);
        int vanillaRecipeCount = recipes.size();

        ICondition.IContext context = new ConditionContext(List.<Registry.PendingTags<?>>of(), this.avaritia$registryLookup, enabledFlags);
        try {
            SingularityReloadListener.INSTANCE.finalizeScriptTransaction(() ->
                    NeoForge.EVENT_BUS.post(new RegisterRecipesEvent(
                            manager, recipes, this.avaritia$registryLookup, context)));
        } catch (RuntimeException | Error e) {
            recipes.clear();
            recipes.addAll(originalRecipes);
            Const.LOGGER.error("Avaritia: An error occurred while firing RegisterRecipesEvent", e);
        }

        int generatedRecipeCount = recipes.size() - vanillaRecipeCount;
        if (!recipes.equals(originalRecipes)) {
            this.recipes = RecipeMap.createClient(recipes);
            this.learnableRecipes = this.recipes.values().stream().filter(r -> !r.value().isSpecial()).collect(Collectors.toUnmodifiableList());
        }

        Const.LOGGER.info(
                "Avaritia: Registered {} recipes in {} ms",
                generatedRecipeCount,
                stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)
        );
    }
}
