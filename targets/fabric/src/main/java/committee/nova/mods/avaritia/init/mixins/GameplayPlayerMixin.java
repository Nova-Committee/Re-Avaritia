package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.blaze.BlazeSwordItem;
import committee.nova.mods.avaritia.common.item.tools.crystal.*;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityAxeItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import net.minecraft.world.entity.EquipmentSlot;
import committee.nova.mods.avaritia.common.item.misc.InfinityElytraItem;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class GameplayPlayerMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void avaritia$weaponAttack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack stack = player.getMainHandItem();
        Item item = stack.getItem();
        boolean handled = false;
        if (item instanceof InfinitySwordItem sword) handled = sword.onLeftClickEntity(stack, player, target);
        else if (item instanceof InfinityAxeItem axe) handled = axe.onLeftClickEntity(stack, player, target);
        else if (item instanceof CrystalAxeItem axe) handled = axe.onLeftClickEntity(stack, player, target);
        else if (item instanceof CrystalHoeItem hoe) handled = hoe.onLeftClickEntity(stack, player, target);
        else if (item instanceof CrystalSwordItem sword) handled = sword.onLeftClickEntity(stack, player, target);
        else if (item instanceof BlazeSwordItem sword) handled = sword.onLeftClickEntity(stack, player, target);
        if (handled) ci.cancel();
    }
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void avaritia$miningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(InfinityHandler.digging((Player) (Object) this, cir.getReturnValue()));
    }
    @Redirect(method = "tryToStartFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean avaritia$nativeElytra(ItemStack stack, Item item) {
        return stack.is(item) || item == Items.ELYTRA && stack.getItem() instanceof InfinityElytraItem;
    }
    @Redirect(method = "tryToStartFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ElytraItem;isFlyEnabled(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean avaritia$elytraFlightEnabled(ItemStack stack) {
        return InfinityElytraUtils.isFlyEnabled(stack);
    }
    @Redirect(method = "tryToStartFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack avaritia$equippedElytra(Player player, EquipmentSlot slot) {
        ItemStack elytra = InfinityElytraUtils.getInfinityElytraStack(player);
        return slot == EquipmentSlot.CHEST && !elytra.isEmpty() ? elytra : player.getItemBySlot(slot);
    }
    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"), cancellable = true)
    private void avaritia$unbreakableShield(float amount, CallbackInfo ci) {
        if (((Player) (Object) this).getUseItem().is(ModItems.infinity_shield.get())) ci.cancel();
    }
}
