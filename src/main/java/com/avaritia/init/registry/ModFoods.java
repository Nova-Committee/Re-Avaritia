package com.avaritia.init.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;

/**
 * 注册模组中的所有食物属性（FoodProperties）。
 *
 * <p>包含终极炖菜与宇宙肉丸两种特殊食物。</p>
 */
public class ModFoods {

    public static double ratio = 1;
    public static final FoodProperties ultimate_stew = new FoodProperties.Builder().nutrition(20).saturationModifier(20F)
            .alwaysEdible()
            .build();
    public static final Consumable ultimate_stew_consumable = Consumables.defaultFood()
            .consumeSeconds(0.8F)
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffects.STRENGTH, (int) Math.ceil(5 * 60 * 20 * ratio), 4),
                    new MobEffectInstance(MobEffects.HASTE, (int) Math.ceil(3 * 60 * 20 * ratio), 2),
                    new MobEffectInstance(MobEffects.SPEED, (int) Math.ceil(3 * 60 * 20 * ratio), 2),
                    new MobEffectInstance(MobEffects.JUMP_BOOST, (int) Math.ceil(3 * 60 * 20 * ratio), 2)
            )))
            .build();

    public static final FoodProperties cosmic_meatballs = new FoodProperties.Builder().nutrition(20).saturationModifier(20F)
            .alwaysEdible()
            .build();
    public static final Consumable cosmic_meatballs_consumable = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) Math.ceil(5 * 60 * 20 * ratio), 0),
                    new MobEffectInstance(MobEffects.RESISTANCE, (int) Math.ceil(1 * 60 * 20 * ratio), 1),
                    new MobEffectInstance(MobEffects.ABSORPTION, (int) Math.ceil(3 * 60 * 20 * ratio), 2),
                    new MobEffectInstance(MobEffects.NIGHT_VISION, (int) Math.ceil(3 * 60 * 20 * ratio), 0),
                    new MobEffectInstance(MobEffects.WATER_BREATHING, (int) Math.ceil(2 * 60 * 20 * ratio), 2),
                    new MobEffectInstance(MobEffects.REGENERATION, (int) Math.ceil(5 * 60 * 20 * ratio), 4)
            )))
            .build();

}
