package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers after vanilla contents exist, before Loader entrypoints and registry freeze.
 * Fabric API redirects the initial bootstrap to createContents and delays bootStrap
 * until server setup, which is already too late for WorldLoader's registry freeze.
 */
@Mixin(BuiltInRegistries.class)
public abstract class RegistryBootstrapMixin {
    @Inject(method = "createContents", at = @At("RETURN"))
    private static void avaritia$registerAfterVanillaContents(CallbackInfo ci) {
        ModConfig.register();
        ModRegistries.initialize();
    }
}
