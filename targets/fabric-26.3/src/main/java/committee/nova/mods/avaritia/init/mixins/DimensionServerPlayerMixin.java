package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.InfinityRingHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class DimensionServerPlayerMixin {
    // 26.3 unified cross-dimension travel: changeDimension and teleportTo both route through teleport(TeleportTransition).
    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("RETURN"))
    private void avaritia$travel(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (cir.getReturnValue() != null) {
            InfinityRingHandler.onJoin(cir.getReturnValue());
        }
    }
}
