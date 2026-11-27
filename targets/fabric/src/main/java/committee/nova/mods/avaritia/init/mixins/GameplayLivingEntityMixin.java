package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.misc.InfinityElytraItem;
import committee.nova.mods.avaritia.init.handler.AbilityHandler;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.init.handler.InfinityShieldHandler;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GameplayLivingEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void avaritia$abilities(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AbilityHandler.updateAbilities(entity);
        if (entity instanceof Player player) InfinityShieldHandler.floatTick(player);
        ItemStack chest = InfinityElytraUtils.getInfinityElytraStack(entity);
        if (chest.getItem() instanceof InfinityElytraItem elytra && entity.isFallFlying())
            elytra.elytraFlightTick(chest, entity, entity.tickCount);
    }
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void avaritia$jump(CallbackInfo ci) { AbilityHandler.jumpBoost((LivingEntity) (Object) this); }
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void avaritia$immunity(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (InfinityShieldHandler.preventDamage(entity, source) || InfinityHandler.preventDamage(entity, source, amount)) cir.setReturnValue(false);
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
    private void avaritia$fall(float distance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && (InfinityElytraUtils.hasInfinityElytraEquipped(player) || InfinityShieldHandler.preventFall(player))) {
            player.resetFallDistance();
            cir.setReturnValue(false);
        }
    }
    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void avaritia$surface(FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && !player.isSwimming() && InfinityShieldHandler.preventFall(player)) cir.setReturnValue(true);
    }
    @Redirect(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean avaritia$elytra(ItemStack stack, net.minecraft.world.item.Item item) {
        return stack.is(item) || item == Items.ELYTRA && stack.getItem() instanceof InfinityElytraItem;
    }
    @Redirect(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ElytraItem;isFlyEnabled(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean avaritia$elytraFlightEnabled(ItemStack stack) {
        return InfinityElytraUtils.isFlyEnabled(stack);
    }
    @Redirect(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack avaritia$equippedElytra(LivingEntity entity, EquipmentSlot slot) {
        ItemStack elytra = InfinityElytraUtils.getInfinityElytraStack(entity);
        return slot == EquipmentSlot.CHEST && !elytra.isEmpty() ? elytra : entity.getItemBySlot(slot);
    }
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"))
    private void avaritia$reflectBlocked(LivingEntity entity, float blocked, DamageSource source, float amount) {
        InfinityShieldHandler.onShieldBlock(entity, source, blocked);
        ((GameplayShieldDamageInvoker) entity).avaritia$hurtShield(blocked);
    }
}
