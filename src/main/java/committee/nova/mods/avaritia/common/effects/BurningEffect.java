package committee.nova.mods.avaritia.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * @author cnlimiter
 */
public class BurningEffect extends MobEffect {
    public BurningEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        List<Entity> list = livingEntity.level().getEntities(livingEntity, livingEntity.getBoundingBox().inflate(.25, .5, .25));
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (entity instanceof LivingEntity livingEntity1) {
                    entity.hurt(livingEntity.level().damageSources().inFire(), livingEntity1.getMaxHealth() * 0.1f);
                    entity.invulnerableTime = 0;
                }
            }
        } else if (livingEntity.horizontalCollision) {
            livingEntity.removeEffect(this);
        }
        livingEntity.fallDistance = 0;
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        int i = 40 >> pAmplifier;
        if (i > 0) {
            return pDuration % i == 0;
        } else {
            return true;
        }
    }

}
