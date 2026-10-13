package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import committee.nova.mods.avaritia.init.handler.InfinityShieldHandler;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldMixin {
    // Instant, all-angle blocking for the infinity shield.
    // 26.3 replaced isDamageSourceBlocked with the BlocksAttacks-component driven applyItemBlocking.
    @Inject(method = "applyItemBlocking", at = @At("HEAD"), cancellable = true)
    private void avaritia$infinityShieldBlocksImmediately(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof InfinityShieldItem) {
            boolean piercing = source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0;
            cir.setReturnValue(source.is(DamageTypeTags.BYPASSES_SHIELD) || piercing ? 0.0F : damage);
        }
    }

    // Reflection callback for blocked hits (replaces the 1.20.1 hurtCurrentlyUsedShield redirect).
    @Inject(method = "applyItemBlocking", at = @At("RETURN"))
    private void avaritia$reflectBlocked(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (cir.getReturnValue() > 0.0F && entity.getUseItem().getItem() instanceof InfinityShieldItem) {
            InfinityShieldHandler.onShieldBlock(entity, source, cir.getReturnValue());
        }
    }

    // UseEffects.interactVibrations=false; do not cancel item-use state or other game events.
    @Redirect(method = {"startUsingItem", "stopUsingItem"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;causeUseVibration(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder$Reference;)V"))
    private void avaritia$shieldUseIsSilent(ItemStack useItem, Entity causer, Holder.Reference<GameEvent> event) {
        if (!(useItem.getItem() instanceof InfinityShieldItem)) {
            useItem.causeUseVibration(causer, event);
        }
    }
}
