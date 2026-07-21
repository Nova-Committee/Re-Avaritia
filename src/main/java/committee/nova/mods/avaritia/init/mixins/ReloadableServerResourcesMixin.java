package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/5/19 00:22
 * @Description:
 */
@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    public ReloadableServerResourcesMixin() {
    }

    @Inject(
            at = {@At("RETURN")},
            method = {"<init>"}
    )
    public void avaritia$constructor(RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, int functionCompilationLevel, CallbackInfo ci) {
        ReloadableServerResources resources = (ReloadableServerResources) (Object) this;
        SingularityReloadListener.INSTANCE.setContext(resources.getConditionContext());
    }

    @Inject(
            at = {@At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z")},
            method = {"loadResources"},
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void avaritia$loadResources(ResourceManager resourceManager, RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, int functionCompilationLevel, Executor backgroundExecutor, Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir, ReloadableServerResources reloadableserverresources,
                                           List<PreparableReloadListener> listeners) {
        listeners.add(2, SingularityReloadListener.INSTANCE);
    }//确保SingularityReloadListener在RecipeManager之前执行
}
