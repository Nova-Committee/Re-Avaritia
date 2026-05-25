package com.avaritia.init.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

/**
 * 注册模组中的所有食物属性（FoodProperties）。
 *
 * <p>包含终极炖菜与宇宙肉丸两种特殊食物。</p>
 */
public class ModFoods {

    public static double ratio = 1;
    public static final FoodProperties ultimate_stew = new FoodProperties.Builder().nutrition(20).saturationModifier(20F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) Math.ceil(5 * 60 * 20 * ratio), 4), 1).effect(
                    () -> new MobEffectInstance(MobEffects.DIG_SPEED, (int) Math.ceil(3 * 60 * 20 * ratio), 2), 1).effect(
                    () -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int) Math.ceil(3 * 60 * 20 * ratio), 2), 1).effect(
                    () -> new MobEffectInstance(MobEffects.JUMP, (int) Math.ceil(3 * 60 * 20 * ratio), 2), 1).alwaysEdible().fast().build();

    public static final FoodProperties cosmic_meatballs = new FoodProperties.Builder().nutrition(20).saturationModifier(20F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) Math.ceil(5 * 60 * 20 * ratio), 0), 1).effect(
                    () -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int) Math.ceil(1 * 60 * 20 * ratio), 1), 1).effect(
                    () -> new MobEffectInstance(MobEffects.ABSORPTION, (int) Math.ceil(3 * 60 * 20 * ratio), 2), 1).effect(
                    () -> new MobEffectInstance(MobEffects.NIGHT_VISION, (int) Math.ceil(3 * 60 * 20 * ratio), 0), 1).effect(
                    () -> new MobEffectInstance(MobEffects.WATER_BREATHING, (int) Math.ceil(2 * 60 * 20 * ratio), 2), 1).effect(
                    () -> new MobEffectInstance(MobEffects.REGENERATION, (int) Math.ceil(5 * 60 * 20 * ratio), 4), 1).alwaysEdible().build();

}
