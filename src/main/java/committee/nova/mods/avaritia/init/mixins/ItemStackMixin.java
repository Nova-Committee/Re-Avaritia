package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityPickaxeItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * @author cu6
 * @description 工具不消耗耐久Mixin
 * @date 2025/9/15 23:40
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {

    /**
     * 拦截ItemStack的hurtAndBreak方法
     *
     * @param amount 伤害值
     * @param ci     回调信息
     */
    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private void onHurtAndBreak(int amount, LivingEntity entity, Consumer<LivingEntity> onBroken, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IUndamageable) {
            ci.cancel();
        }
    }
}
