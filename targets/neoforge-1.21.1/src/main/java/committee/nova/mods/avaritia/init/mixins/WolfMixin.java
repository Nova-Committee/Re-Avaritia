package committee.nova.mods.avaritia.init.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 让 1.21.1 的中子狼铠只保留模组自己的机制，不带原版狼铠的「挡伤害 + 掉耐久」。
 * <p>
 * 1.21.1 的原版链路是三条硬编码：
 * <ol>
 *   <li>{@code hasArmor()} → 只认 {@code Items.WOLF_ARMOR}，为 false 时连渲染都不会有；</li>
 *   <li>{@code canArmorAbsorb(source)} → true 时伤害全部由铠甲吃掉，狼不掉血；</li>
 *   <li>{@code hurtArmor(source, amount)} → 只要 BODY 槽有东西就扣耐久，不管有没有吸收。</li>
 * </ol>
 * 因此这里：放行 hasArmor（拿到渲染与剪刀取下），拦住 canArmorAbsorb 与 hurtArmor
 * （伤害与耐久都交给 {@code NeutronWolfArmorHandler} 处理）。
 * <p>
 * 全部用 MixinExtras 的写法（{@code @ModifyReturnValue} / {@code @WrapMethod}），
 * 只改写返回值或包裹调用，不抢占整个方法，和其他模组对同一方法的注入可以共存。
 */
@Mixin(Wolf.class)
public abstract class WolfMixin {
    @ModifyReturnValue(method = "hasArmor", at = @At("RETURN"))
    private boolean avaritia$hasNeutronArmor(boolean original) {
        Wolf wolf = (Wolf) (Object) this;
        return original || wolf.getBodyArmorItem().is(ModItems.neutron_wolf_armor.get());
    }

    @ModifyReturnValue(method = "canArmorAbsorb", at = @At("RETURN"))
    private boolean avaritia$neutronArmorDoesNotAbsorb(boolean original, DamageSource source) {
        Wolf wolf = (Wolf) (Object) this;
        return original && !wolf.getBodyArmorItem().is(ModItems.neutron_wolf_armor.get());
    }

    @WrapMethod(method = "hurtArmor")
    private void avaritia$neutronArmorKeepsDurability(DamageSource source, float amount, Operation<Void> original) {
        Wolf wolf = (Wolf) (Object) this;
        if (!wolf.getBodyArmorItem().is(ModItems.neutron_wolf_armor.get())) {
            original.call(source, amount);
        }
    }
}
