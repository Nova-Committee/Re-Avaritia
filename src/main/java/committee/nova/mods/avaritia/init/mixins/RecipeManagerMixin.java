package committee.nova.mods.avaritia.init.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import committee.nova.mods.avaritia.api.utils.RecipeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
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
@Mixin({RecipeManager.class})
public abstract class RecipeManagerMixin extends SimpleJsonResourceReloadListener {

    public RecipeManagerMixin(Gson gson, String directory) {
        super(gson, directory);
    }

    @Definition(id = "builder", method = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;")
    @Expression("? = builder()")
    @Inject(
            at = {@At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER)},
            method = {"apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"}
    )
    public void avaritia$apply(Map<ResourceLocation, JsonElement> p_44037_, ResourceManager p_44038_, ProfilerFiller p_44039_, CallbackInfo ci, @Local ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType, @Local ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName) {
        RecipeUtils.fireRecipeManagerLoadingEvent((RecipeManager)(Object)this, byType, byName);
    }
}
