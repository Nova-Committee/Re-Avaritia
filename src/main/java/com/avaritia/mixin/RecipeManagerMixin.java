package com.avaritia.mixin;

import com.avaritia.Avaritia;
import com.google.common.base.Stopwatch;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 在配方重载完成前注入 Avaritia 运行时生成配方。
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin extends ContextAwareReloadListener {
    @Unique
    private static final String AVARITIA_REGISTER_RECIPES_EVENT = "com.avaritia.api.init.event.RegisterRecipesEvent";
    @Unique
    private static boolean avaritia$warnedMissingRegisterRecipesEvent;

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
            this.avaritia$postRegisterRecipesEvent(manager, recipes);
        } catch (Exception e) {
            Const.LOGGER.error("Avaritia: An error occurred while firing RecipeManagerLoadingEvent", e);
        }

        int generatedRecipeCount = recipes.size() - vanillaRecipeCount;
        if (generatedRecipeCount > 0) {
            // 26.1.2 的 RecipeMap 取代旧版 byType/byName builder 局部变量；新增配方后必须重建整个 RecipeMap。
            this.recipes = RecipeMap.create(recipes);
            ci.cancel();
        }

        Const.LOGGER.info(
                "Avaritia: Registered {} recipes in {} ms",
                generatedRecipeCount,
                stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)
        );
    }

    @Unique
    private void avaritia$postRegisterRecipesEvent(RecipeManager manager, List<RecipeHolder<?>> recipes) throws ReflectiveOperationException {
        Class<?> eventClass;
        try {
            eventClass = Class.forName(AVARITIA_REGISTER_RECIPES_EVENT);
        } catch (ClassNotFoundException e) {
            // TODO: 迁移 com.avaritia.api.init.event.RegisterRecipesEvent 后，运行时生成配方事件会自动恢复派发。
            if (!avaritia$warnedMissingRegisterRecipesEvent) {
                Const.LOGGER.warn("Avaritia: RegisterRecipesEvent has not been migrated yet; runtime generated recipes are skipped.");
                avaritia$warnedMissingRegisterRecipesEvent = true;
            }
            return;
        }

        if (!Event.class.isAssignableFrom(eventClass)) {
            Const.LOGGER.warn("Avaritia: {} is not a NeoForge event; runtime generated recipes are skipped.", AVARITIA_REGISTER_RECIPES_EVENT);
            return;
        }

        Constructor<?> constructor = eventClass.getConstructor(
                RecipeManager.class,
                List.class,
                HolderLookup.Provider.class,
                ICondition.IContext.class
        );
        Event event = (Event) constructor.newInstance(manager, recipes, this.getRegistryLookup(), this.getContext());
        NeoForge.EVENT_BUS.post(event);
    }
}
