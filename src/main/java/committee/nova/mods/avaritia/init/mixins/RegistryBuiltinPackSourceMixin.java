package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.PackResourceHandler;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(BuiltInPackSource.class)
public abstract class RegistryBuiltinPackSourceMixin {
    @Shadow @Final private PackType packType;

    @Inject(method = "loadPacks", at = @At("TAIL"))
    private void avaritia$loadRequiredResources(Consumer<Pack> consumer, CallbackInfo ci) {
        PackResourceHandler.addResources(consumer, this.packType);
    }
}
