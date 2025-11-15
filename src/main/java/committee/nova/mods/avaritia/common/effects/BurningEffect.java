package committee.nova.mods.avaritia.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author cnlimiter
 */
public class BurningEffect extends MobEffect {
    public BurningEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.hurt(livingEntity.damageSources().inFire(), livingEntity.getMaxHealth() * 0.05f);
        livingEntity.invulnerableTime = 10;
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int p_295629_, int p_295734_) {
        int i = 20 >> p_295734_;
        return i == 0 || p_295629_ % i == 0;
    }
}
