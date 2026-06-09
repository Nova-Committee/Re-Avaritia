package committee.nova.mods.avaritia.common.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.NonNull;

/**
 * 燃烧效果——在效果持续期间对目标造成基于最大生命值百分比的火焰伤害。
 */
public class BurningEffect extends MobEffect {
    public BurningEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(@NonNull ServerLevel serverLevel, LivingEntity livingEntity, int amplifier) {
        livingEntity.hurt(livingEntity.damageSources().inFire(), livingEntity.getMaxHealth() * 0.05f);
        livingEntity.invulnerableTime = 10;
        return true;
    }


    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int i = 20 >> amplifier;
        return i == 0 || duration % i == 0;
    }
}
