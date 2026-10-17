package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.AbilityHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class GameplayServerPlayerMixin {
    // 26.3: changeDimension was removed; cross-dimension travel routes through teleport(TeleportTransition).
    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("RETURN"))
    private void avaritia$travelFlight(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (cir.getReturnValue() != null) AbilityHandler.reapplyFly(cir.getReturnValue());
    }
    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void avaritia$respawnAbilities(ServerPlayer previous, boolean alive, CallbackInfo ci) {
        AbilityHandler.stripAbilities((ServerPlayer) (Object) this);
        AbilityHandler.updateAbilities((ServerPlayer) (Object) this);
    }
}
