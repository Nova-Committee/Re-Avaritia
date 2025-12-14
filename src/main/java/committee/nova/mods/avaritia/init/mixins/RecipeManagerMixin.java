package committee.nova.mods.avaritia.init.mixins;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import committee.nova.mods.avaritia.api.Lib;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;makeConditionalOps()Lnet/neoforged/neoforge/common/conditions/ConditionalOps;"),
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"
    )
    public void avaritia$apply(
            Map<ResourceLocation, JsonElement> jsonElementMap,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller,
            CallbackInfo ci,
            @Local ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType,
            @Local ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName
    ) {
        RecipeManager manager = (RecipeManager) (Object) this;
        Lib.LOGGER.info("Avaritia: Loading recipes...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        ArrayList<RecipeHolder<?>> recipes = new ArrayList<>();

        try {
            NeoForge.EVENT_BUS.post(new RegisterRecipesEvent(manager, recipes, this.getRegistryLookup(), this.getContext()));
        } catch (Exception e) {
            Lib.LOGGER.error("Avaritia: An error occurred while firing RecipeManagerLoadingEvent", e);
        }

        for(RecipeHolder<?> recipe : recipes) {
            byType.put(recipe.value().getType(), recipe);
            byName.put(recipe.id(), recipe);
        }

        Lib.LOGGER.info("Avaritia: Registered {} recipes in {} ms", recipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
}
