package committee.nova.mods.avaritia.common.effects;

import net.minecraft.server.level.ServerLevel;
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
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        livingEntity.hurtServer(level, livingEntity.damageSources().inFire(), livingEntity.getMaxHealth() * 0.05f);
        livingEntity.setInvulnerableTime(10);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int i = 20 >> amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }
}
