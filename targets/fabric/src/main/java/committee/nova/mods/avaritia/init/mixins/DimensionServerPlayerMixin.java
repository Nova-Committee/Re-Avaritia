package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.InfinityRingHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class DimensionServerPlayerMixin {
    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void avaritia$portalTravel(ServerLevel target, CallbackInfoReturnable<Entity> cir) {
        InfinityRingHandler.onJoin((ServerPlayer) (Object) this);
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("RETURN"))
    private void avaritia$directTravel(ServerLevel target, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        InfinityRingHandler.onJoin((ServerPlayer) (Object) this);
    }
}
