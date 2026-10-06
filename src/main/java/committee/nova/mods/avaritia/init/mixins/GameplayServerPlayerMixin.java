package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.AbilityHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class GameplayServerPlayerMixin {
    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void avaritia$travelFlight(ServerLevel level, CallbackInfoReturnable<Entity> cir) {
        AbilityHandler.reapplyFly((ServerPlayer) (Object) this);
    }
    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void avaritia$respawnAbilities(ServerPlayer previous, boolean alive, CallbackInfo ci) {
        AbilityHandler.stripAbilities((ServerPlayer) (Object) this);
        AbilityHandler.updateAbilities((ServerPlayer) (Object) this);
    }
}
