package committee.nova.mods.avaritia.init.mixins;

import com.google.gson.JsonElement;
import committee.nova.mods.avaritia.api.init.data.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RegistryRecipeConditionsMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void avaritia$conditions(Map<ResourceLocation, JsonElement> recipes, ResourceManager manager,
                                     ProfilerFiller profiler, CallbackInfo ci) {
        recipes.entrySet().removeIf(entry -> entry.getValue().isJsonObject()
                && !ResourceConditions.processConditions(entry.getValue().getAsJsonObject()));
    }
}
