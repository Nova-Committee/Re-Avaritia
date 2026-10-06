package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.ResourceReloadHandler;
import committee.nova.mods.avaritia.init.handler.SingularityScriptTransactionFinalizer;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ReloadableServerResources.class, priority = 900)
public abstract class SingularityServerResourcesMixin {
    @Shadow @Final private TagManager tagManager;
    @Shadow @Final private RecipeManager recipes;

    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void avaritia$reloadListeners(CallbackInfoReturnable<List<PreparableReloadListener>> callback) {
        callback.setReturnValue(ResourceReloadHandler.listeners(callback.getReturnValue(), this.tagManager, this.recipes));
    }

    @Inject(method = "loadResources", at = @At("RETURN"), cancellable = true)
    private static void avaritia$commitScriptTransaction(ResourceManager resourceManager,
                                                        RegistryAccess.Frozen registryAccess,
                                                        FeatureFlagSet enabledFeatures,
                                                        Commands.CommandSelection commandSelection,
                                                        int functionCompilationLevel,
                                                        Executor preparationExecutor, Executor applyExecutor,
                                                        CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> callback) {
        // CraftTweaker appends its listeners at SimpleReloadInstance.create, after listeners() returns.
        // Waiting on the complete resource future covers those listeners on both startup and /reload.
        callback.setReturnValue(callback.getReturnValue().thenApplyAsync(resources -> {
            SingularityScriptTransactionFinalizer.finalizeReload(resources.getRecipeManager());
            return resources;
        }, applyExecutor));
    }
}
