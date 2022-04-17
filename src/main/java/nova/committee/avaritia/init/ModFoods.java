package nova.committee.avaritia.init;

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
    public static final FoodProperties ultimate_stew = (new FoodProperties.Builder()).nutrition(20).saturationMod(20F)
            .effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F).build();

    public static final FoodProperties cosmic_meatballs = (new FoodProperties.Builder()).nutrition(20).saturationMod(20F)
            .effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F).build();

}
