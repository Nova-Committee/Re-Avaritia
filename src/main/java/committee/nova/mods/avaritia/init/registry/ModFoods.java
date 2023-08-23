package committee.nova.mods.avaritia.init.registry;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 0:11
 * Version: 1.0
 */
public class ModFoods {

    public static double ratio = 1;
    public static final FoodProperties ultimate_stew = (new FoodProperties.Builder()).nutrition(20).saturationMod(20F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) Math.ceil(5 * 60 * 20 * ratio), 4), 1).effect(
                    () -> new MobEffectInstance(MobEffects.DIG_SPEED, (int) Math.ceil(3 * 60 * 20 * ratio), 2), 1).effect(
                    () -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int) Math.ceil(3 * 60 * 20 * ratio), 2), 1).effect(
                    () -> new MobEffectInstance(MobEffects.JUMP, (int) Math.ceil(3 * 60 * 20 * ratio), 2), 1).alwaysEat().meat().build();

    public static final FoodProperties cosmic_meatballs = (new FoodProperties.Builder()).nutrition(20).saturationMod(20F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) Math.ceil(5 * 60 * 20 * ratio), 0), 1).effect(
                    () -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int) Math.ceil(1 * 60 * 20 * ratio), 1), 1).effect(
                    () -> new MobEffectInstance(MobEffects.ABSORPTION, (int) Math.ceil(3 * 60 * 20 * ratio), 2), 1).effect(
                    () -> new MobEffectInstance(MobEffects.NIGHT_VISION, (int) Math.ceil(3 * 60 * 20 * ratio), 0), 1).effect(
                    () -> new MobEffectInstance(MobEffects.WATER_BREATHING, (int) Math.ceil(2 * 60 * 20 * ratio), 2), 1).effect(
                    () -> new MobEffectInstance(MobEffects.REGENERATION, (int) Math.ceil(5 * 60 * 20 * ratio), 4), 1).alwaysEat().build();

}
