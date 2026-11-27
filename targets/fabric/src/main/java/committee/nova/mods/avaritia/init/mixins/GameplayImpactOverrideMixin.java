package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.entity.RainProEntity;
import committee.nova.mods.avaritia.common.entity.StormProEntity;
import committee.nova.mods.avaritia.common.entity.SunProEntity;
import committee.nova.mods.avaritia.common.entity.arrow.BurningArrowEntity;
import committee.nova.mods.avaritia.common.entity.ball.BurningBallEntity;
import committee.nova.mods.avaritia.common.entity.ball.FireBallEntity;
import committee.nova.mods.avaritia.init.handler.ProjectileReflectionTracker;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cancels entire override bodies, not just their super.onHit call. */
@Mixin({ThrownPotion.class, ThrownEgg.class, ThrownEnderpearl.class, ThrownExperienceBottle.class,
        SmallFireball.class, LargeFireball.class, DragonFireball.class, WitherSkull.class,
        RainProEntity.class, StormProEntity.class, SunProEntity.class,
        BurningArrowEntity.class, BurningBallEntity.class, FireBallEntity.class})
public abstract class GameplayImpactOverrideMixin {
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void avaritia$skipReflectedImpact(HitResult result, CallbackInfo ci) {
        if (((ProjectileReflectionTracker) this).avaritia$reflectImpact(result)) ci.cancel();
    }
}
