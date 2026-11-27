package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.common.item.misc.InfinityElytraItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * @author cu6
 * @description 工具不消耗耐久Mixin
 * @date 2025/9/15 23:40
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    private void avaritia$unbreakableItems(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IUndamageable || stack.getItem() instanceof InfinityElytraItem) {
            cir.setReturnValue(false);
        }
    }

    /**
     * 拦截ItemStack的hurtAndBreak方法
     */
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void onHurtAndBreak(int amount, ServerLevel level, ServerPlayer player, Consumer<ItemStack> onBroken, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IUndamageable) {
            ci.cancel();
        }
    }
}
