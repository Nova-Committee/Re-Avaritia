package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.init.data.LootResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(LootDataManager.class)
public abstract class RegistryLootManagerConditionsMixin {
    @Inject(method = "reload", at = @At("HEAD"))
    private void avaritia$begin(PreparableReloadListener.PreparationBarrier barrier, ResourceManager resources,
                                ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                Executor preparationExecutor, Executor applyExecutor,
                                CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        LootResourceConditions.beginReload();
    }

    @Inject(method = "apply", at = @At("HEAD"))
    private void avaritia$tags(Map<LootDataType<?>, Map<ResourceLocation, ?>> elements, CallbackInfo ci) {
        LootResourceConditions.apply(elements);
    }
}
