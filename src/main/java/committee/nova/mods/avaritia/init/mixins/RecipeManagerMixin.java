package committee.nova.mods.avaritia.init.mixins;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import committee.nova.mods.avaritia.api.Lib;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/5/18 13:47
 * @Description:
 */
@Mixin(value = {RecipeManager.class}, priority = 1101)
public abstract class RecipeManagerMixin extends SimpleJsonResourceReloadListener {

    @Shadow
    public Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    @Shadow
    public Map<ResourceLocation, Recipe<?>> byName;

    public RecipeManagerMixin(Gson gson, String directory) {
        super(gson, directory);
    }

    @Inject(
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 1),
            method = {"apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"}
    )
    public void avaritia$apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager,
                                ProfilerFiller profiler, CallbackInfo ci
    ) {
        RecipeManager manager = (RecipeManager) (Object) this;
        Lib.LOGGER.info("Avaritia: Loading recipes...");
        var stopwatch = Stopwatch.createStarted();
        CopyOnWriteArrayList<Recipe<?>> addRecipes = new CopyOnWriteArrayList<>();

        try {
            MinecraftForge.EVENT_BUS.post(new RegisterRecipesEvent(manager, addRecipes));
        } catch (Exception e) {
            Lib.LOGGER.error("An error occurred while firing RegisterRecipesEvent", e);
        }

        if (recipes instanceof ImmutableMap) {
            recipes = new ConcurrentHashMap<>(recipes);
            recipes.replaceAll((t, v) -> new ConcurrentHashMap<>(recipes.get(t)));
        }

        if (byName instanceof ImmutableMap) {
            byName = new ConcurrentHashMap<>(byName);
        }

        addRecipes.forEach((recipe1) -> {
            recipes.computeIfAbsent(recipe1.getType(), t -> new ConcurrentHashMap<>()).put(recipe1.getId(), recipe1);
            byName.put(recipe1.getId(), recipe1);
        });


        Lib.LOGGER.info("Avaritia: Registered {} recipes in {} ms", addRecipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
}
