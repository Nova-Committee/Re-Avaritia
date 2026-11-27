package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.core.io.StorageLifecycle;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class StorageServerLifecycleMixin {
    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void avaritia$loadStorage(CallbackInfo ci) { StorageLifecycle.start((MinecraftServer) (Object) this); }
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void avaritia$tickStorage(BooleanSupplier hasTimeLeft, CallbackInfo ci) { StorageLifecycle.tick((MinecraftServer) (Object) this); }
    @Inject(method = "saveEverything", at = @At("RETURN"))
    private void avaritia$saveStorage(boolean suppressLogs, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) { StorageLifecycle.save((MinecraftServer) (Object) this); }
    @Inject(method = "stopServer", at = @At("HEAD"))
    private void avaritia$stopStorage(CallbackInfo ci) { StorageLifecycle.stop((MinecraftServer) (Object) this); }
}
