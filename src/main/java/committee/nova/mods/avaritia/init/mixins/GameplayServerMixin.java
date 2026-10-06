package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class GameplayServerMixin {
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void avaritia$restoreMining(BooleanSupplier hasTime, CallbackInfo ci) {
        InfinityHandler.restoreAbandonedTemporaryFakeBlocks((MinecraftServer) (Object) this);
    }
}
