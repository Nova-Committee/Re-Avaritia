package com.avaritia.init.registry;

import com.avaritia.Avaritia;
import com.avaritia.Const;
import com.avaritia.common.effects.BurningEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 注册模组中的所有状态效果。
 */
public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Const.MOD_ID);
    public static final DeferredHolder<MobEffect, MobEffect> BURNING = MOB_EFFECTS.register("burning", () -> new BurningEffect(MobEffectCategory.HARMFUL, 0xd0f9ff));
}
