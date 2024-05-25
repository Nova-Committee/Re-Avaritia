package committee.nova.mods.avaritia.init.mixin;

import committee.nova.mods.avaritia.api.init.event.ModEventFactory;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * ReloadableResourcesMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午5:06
 */
@Mixin(ReloadableServerResources.class)
public abstract class ReloadableResourcesMixin {

    @Shadow @Final private static CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK;

    @Shadow @Final private static Logger LOGGER;

    @Inject(
            method = "loadResources",
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void loadResources1(ResourceManager pResourceManager, RegistryAccess.Frozen pRegistryAccess, FeatureFlagSet pEnabledFeatures, Commands.CommandSelection pCommandSelection, int pFunctionCompilationLevel, Executor pBackgroundExecutor, Executor pGameExecutor, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir){
        ReloadableServerResources reloadableserverresources = new ReloadableServerResources(pRegistryAccess, pEnabledFeatures, pCommandSelection, pFunctionCompilationLevel);
        List<PreparableReloadListener> listeners = new java.util.ArrayList<>(reloadableserverresources.listeners());
        listeners.addAll(ModEventFactory.onResourceReload(reloadableserverresources, pRegistryAccess));
        cir.setReturnValue(SimpleReloadInstance.create(pResourceManager, listeners, pBackgroundExecutor, pGameExecutor, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()).done().whenComplete((p_214309_, p_214310_) -> {
            reloadableserverresources.commandBuildContext.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.FAIL);
        }).thenApply((p_214306_) -> {
            return reloadableserverresources;
        }));
        cir.cancel();
    }
}
