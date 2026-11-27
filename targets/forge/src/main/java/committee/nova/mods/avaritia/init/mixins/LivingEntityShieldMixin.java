package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldMixin {
    // Forge fires ShieldBlockEvent only after this test succeeds. Backport instant/all-angle blocking here.
    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    private void avaritia$infinityShieldBlocksImmediately(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof InfinityShieldItem) {
            boolean piercing = source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0;
            cir.setReturnValue(!source.is(DamageTypeTags.BYPASSES_SHIELD) && !piercing);
        }
    }

    // 26.1.2 UseEffects.interactVibrations=false; do not cancel item-use state or other game events.
    @Redirect(method = {"startUsingItem", "stopUsingItem"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;)V"))
    private void avaritia$shieldUseIsSilent(LivingEntity entity, GameEvent event) {
        if (!(entity.getUseItem().getItem() instanceof InfinityShieldItem)) {
            entity.gameEvent(event);
        }
    }
}
