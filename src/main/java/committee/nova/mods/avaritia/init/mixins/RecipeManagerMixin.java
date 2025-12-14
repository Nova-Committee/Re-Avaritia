package committee.nova.mods.avaritia.init.mixins;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            at = @At(value = "TAIL"),
            method = {"apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"}
    )
    public void avaritia$apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager,
                                ProfilerFiller profiler, CallbackInfo ci
    ) {
        RecipeManager manager = (RecipeManager) (Object) this;
        Lib.LOGGER.info("Avaritia: Loading recipes...");

        // 创建新的recipes映射
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = Maps.newConcurrentMap();
        this.recipes.forEach((type, map) ->
                newRecipes.put(type, new ConcurrentHashMap<>(map)));

        // 创建新的byName映射
        Map<ResourceLocation, Recipe<?>> newByName = new ConcurrentHashMap<>(this.byName);

        var stopwatch = Stopwatch.createStarted();
        var recipes = new CopyOnWriteArrayList<Recipe<?>>();

        try {
            MinecraftForge.EVENT_BUS.post(new RegisterRecipesEvent(manager, recipes));
        } catch (Exception e) {
            Lib.LOGGER.error("An error occurred while firing RegisterRecipesEvent", e);
        }

        for (var recipe : recipes) {
            RecipeType<?> recipeType = recipe.getType();
            var recipeMap = newRecipes.get(recipeType);

            if (!newRecipes.containsKey(recipeType)) {
                recipeMap = Maps.newConcurrentMap();
            }

            recipeMap.put(recipe.getId(), recipe);
            newRecipes.put(recipe.getType(), recipeMap);
            newByName.put(recipe.getId(), recipe);
        }

        this.recipes = ImmutableMap.copyOf(newRecipes);
        this.byName = ImmutableMap.copyOf(newByName);

        Lib.LOGGER.info("Registered {} recipes in {} ms", recipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
}
