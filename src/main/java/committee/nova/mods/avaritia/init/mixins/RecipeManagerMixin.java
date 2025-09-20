package committee.nova.mods.avaritia.init.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import committee.nova.mods.avaritia.api.util.RecipeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 13:47
 * @Description:
 */
@SuppressWarnings("unchecked")
@Mixin({RecipeManager.class})
public abstract class RecipeManagerMixin extends SimpleJsonResourceReloadListener {

    public RecipeManagerMixin(Gson gson, String directory) {
        super(gson, directory);
    }

    @Inject(
            at = {@At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;",
                    ordinal = 0
            )},
            method = {"apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"}

    )
    public void avaritia$apply(
            Map<ResourceLocation, JsonElement> p_44037_,
            ResourceManager p_44038_,
            ProfilerFiller p_44039_,
            CallbackInfo ci,
            @Local(ordinal = 1) Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> map, // recipes
            @Local(name = "builder") ImmutableMap.Builder<ResourceLocation, Recipe<?>> builder // byName
    ) {
        RecipeUtils.fireRecipeManagerLoadedEvent((RecipeManager) (Object) this, (Map<RecipeType<?>, Object>) (Object) map, builder);
    }
}
