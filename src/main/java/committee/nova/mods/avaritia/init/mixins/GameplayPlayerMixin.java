package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.blaze.BlazeSwordItem;
import committee.nova.mods.avaritia.common.item.tools.crystal.*;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityAxeItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
    // 26.3: elytra flight checks route through LivingEntity.canGlideUsing (see GameplayLivingEntityMixin),
    // and shield durability loss is handled by the BlocksAttacks component + ItemStackMixin.
}
