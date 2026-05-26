package com.avaritia.mixin;

import com.avaritia.common.item.tools.infinity.InfinityShieldItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "blockUsingShield", at = @At("HEAD"), cancellable = true)
    private void onBlockUsingShield(LivingEntity entity, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack useItem = player.getUseItem();

        if (useItem.getItem() instanceof InfinityShieldItem) {

            if (entity.getMainHandItem().canDisableShield(useItem, player, entity)) {
                ci.cancel();
            }
        }
    }
}
