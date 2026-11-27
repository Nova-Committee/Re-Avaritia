package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.InfinityShieldHandler;
import committee.nova.mods.avaritia.init.handler.ProjectileReflectionTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public abstract class GameplayProjectileMixin implements ProjectileReflectionTracker {
    @Unique private int avaritia$reflectedEntity = -1;
    @Unique private int avaritia$reflectionTick;
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void avaritia$reflect(HitResult result, CallbackInfo ci) {
        if (avaritia$reflectImpact(result)) ci.cancel();
    }
    @Override
    public boolean avaritia$reflectImpact(HitResult result) {
        Projectile projectile = (Projectile) (Object) this;
        if (!InfinityShieldHandler.reflectProjectile(projectile, result)) return false;
        avaritia$reflectedEntity = ((EntityHitResult) result).getEntity().getId();
        avaritia$reflectionTick = projectile.tickCount;
        return true;
    }
    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private void avaritia$skipReflected(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Projectile projectile = (Projectile) (Object) this;
        if (entity.getId() == avaritia$reflectedEntity && projectile.tickCount - avaritia$reflectionTick < 5) cir.setReturnValue(false);
    }
}
