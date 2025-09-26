package committee.nova.mods.avaritia.init.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import committee.nova.mods.avaritia.api.util.recipe.ConfigRecipeManager;
import committee.nova.mods.avaritia.api.util.recipe.RecipeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 13:47
 * @Description:
 */
@Mixin({RecipeManager.class})
public abstract class RecipeManagerMixin extends SimpleJsonResourceReloadListener {

    @Shadow
    public Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    @Shadow
    public Map<ResourceLocation, Recipe<?>> byName;

    @Shadow(remap = false)
    @Final
    private ICondition.IContext context;

    public RecipeManagerMixin(Gson gson, String directory) {
        super(gson, directory);
    }

    @Inject(
            method = {"apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"},
            at = {@At(value = "HEAD")}
    )
    public void avaritia$apply(
            Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        // 加载配置文件中的配方
        Map<ResourceLocation, ConfigRecipeManager.RecipeConfig> configRecipes = ConfigRecipeManager.loadConfigRecipes();

        // 处理配置配方
        for (Map.Entry<ResourceLocation, ConfigRecipeManager.RecipeConfig> entry : configRecipes.entrySet()) {
            ResourceLocation recipeId = entry.getKey();
            ConfigRecipeManager.RecipeConfig config = entry.getValue();

            switch (config.action) {
                case ADD:
                    // 添加新配方
                    object.put(recipeId, config.recipeData);
                    break;
                case REMOVE:
                    // 删除配方
                    object.remove(recipeId);
                    break;
                case REPLACE:
                    // 替换配方
                    object.put(recipeId, config.recipeData);
                    break;
            }
        }
    }


    @Inject(
            at = @At("TAIL"),
            method = {"apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"}
    )
    public void avaritia$apply2(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager,
                                ProfilerFiller profiler, CallbackInfo ci
    ) {

        // 创建新的recipes映射
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = Maps.newConcurrentMap();
        this.recipes.forEach((type, map) ->
                newRecipes.put(type, new ConcurrentHashMap<>(map)));

        // 创建新的byName映射
        Map<ResourceLocation, Recipe<?>> newByName = new ConcurrentHashMap<>(this.byName);

        RecipeUtils.fireRecipeManagerLoadedEvent((RecipeManager) (Object) this, this.context, newRecipes, newByName);

        this.recipes = ImmutableMap.copyOf(newRecipes);
        this.byName = ImmutableMap.copyOf(newByName);

    }
}
