package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Loader's main entrypoint runs after freeze when Fabric API is absent. */
@Mixin(BuiltInRegistries.class)
public abstract class RegistryBootstrapMixin {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;freeze()V"))
    private static void avaritia$registerBeforeFreeze(CallbackInfo ci) {
        ModConfig.register();
        ModRegistries.initialize();
    }
}
