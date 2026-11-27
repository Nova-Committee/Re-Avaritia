package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.misc.InfinityElytraItem;
import committee.nova.mods.avaritia.init.handler.AbilityHandler;
import committee.nova.mods.avaritia.init.handler.NeutronWolfArmorHandler;
import committee.nova.mods.avaritia.init.handler.NeutronNautilusArmorHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.init.handler.InfinityShieldHandler;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GameplayLivingEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void avaritia$abilities(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AbilityHandler.updateAbilities(entity);
        NeutronWolfArmorHandler.tick(entity);
        NeutronNautilusArmorHandler.tick(entity);
        if (entity instanceof Player player) InfinityShieldHandler.floatTick(player);
        ItemStack chest = InfinityElytraUtils.getInfinityElytraStack(entity);
        if (chest.getItem() instanceof InfinityElytraItem elytra && entity.isFallFlying())
            elytra.elytraFlightTick(chest, entity, entity.tickCount);
    }
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void avaritia$jump(CallbackInfo ci) { AbilityHandler.jumpBoost((LivingEntity) (Object) this); }
    // 26.3: server-side damage entry point is hurtServer (hurt() was removed).
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void avaritia$immunity(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (NeutronWolfArmorHandler.preventDamage(entity, source) || InfinityShieldHandler.preventDamage(entity, source) || InfinityHandler.preventDamage(entity, source, amount)) cir.setReturnValue(false);
    }
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float avaritia$nautilusIncoming(float amount) {
        return NeutronNautilusArmorHandler.incomingDamage((LivingEntity) (Object) this, amount);
    }

    @ModifyExpressionValue(method = "actuallyHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float avaritia$wolfDamage(float amount, ServerLevel level, DamageSource source, float originalAmount) {
        return NeutronWolfArmorHandler.reduceDamage((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void avaritia$totem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (InfinityHandler.preventDeath((LivingEntity) (Object) this)) cir.setReturnValue(true);
    }
    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void avaritia$death(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (InfinityHandler.preventDeath(entity)) { ci.cancel(); return; }
        if (entity instanceof Player player) AbilityHandler.stripAbilities(player);
    }
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void avaritia$fall(double distance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && (InfinityElytraUtils.hasInfinityElytraEquipped(player) || InfinityShieldHandler.preventFall(player))) {
            player.resetFallDistance();
            cir.setReturnValue(false);
        }
    }
    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void avaritia$surface(FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && !player.isSwimming() && InfinityShieldHandler.preventFall(player)) cir.setReturnValue(true);
    }
    // 26.3: gliding is component-driven; updateFallFlying and Player.tryToStartFallFlying both
    // scan equipment slots through this static check, so one hook covers every entry point.
    @Inject(method = "canGlideUsing", at = @At("HEAD"), cancellable = true)
    private static void avaritia$infinityGlider(ItemStack stack, EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof InfinityElytraItem) cir.setReturnValue(InfinityElytraUtils.isFlyEnabled(stack));
    }
}
