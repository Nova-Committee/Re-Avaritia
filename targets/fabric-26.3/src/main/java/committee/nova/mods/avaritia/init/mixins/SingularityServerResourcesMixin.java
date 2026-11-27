package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.ResourceReloadHandler;
import committee.nova.mods.avaritia.init.handler.SingularityScriptTransactionFinalizer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ReloadableServerResources.class, priority = 900)
public abstract class SingularityServerResourcesMixin {
    @Shadow @Final private RecipeManager recipes;

    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void avaritia$reloadListeners(CallbackInfoReturnable<List<PreparableReloadListener>> callback) {
        callback.setReturnValue(ResourceReloadHandler.listeners(callback.getReturnValue(), this.recipes));
    }

    // 26.3: registry holder data components are NOT bound when loadResources() completes;
    // binding happens in updateComponentsAndStaticRegistryTags(). Creating ItemStacks (our
    // generated singularity compressor recipe outputs) before that throws
    // "Components not bound yet" and aborts world load / world creation.
    // This point is also still before RecipeManager#finalizeRecipeLoading on both the
    // world-open path (WorldLoader -> MinecraftServer ctor) and the /reload path,
    // so our rebuilt RecipeMap participates in display/property-set finalization.
    @Inject(method = "updateComponentsAndStaticRegistryTags", at = @At("TAIL"))
    private void avaritia$commitScriptTransaction(CallbackInfo ci) {
        SingularityScriptTransactionFinalizer.finalizeReload(this.recipes);
    }
}
