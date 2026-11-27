package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.api.iface.SpearMarkEntityData;
import committee.nova.mods.avaritia.common.component.SpearMark;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * 长矛标记的每刻维护（对应 Neo 侧 EntityTickEvent.Post 处理器）。
 * 由 LivingEntitySpearMarkMixin 在 LivingEntity.tick 尾部调用。
 */
public final class SpearMarkHandler {
    private static final int PARTICLE_INTERVAL_TICKS = 4;

    private SpearMarkHandler() {
    }

    public static void tick(LivingEntity target) {
        if (!(target instanceof SpearMarkEntityData data)) {
            return;
        }
        SpearMark mark = data.avaritia$getSpearMark();
        if (mark == null) {
            return;
        }

        long gameTime = target.level().getGameTime();
        if (!mark.isActive(gameTime)) {
            if (!target.level().isClientSide()) {
                data.avaritia$setSpearMark(null);
            }
            return;
        }

        if (!target.level().isClientSide() || target.tickCount % PARTICLE_INTERVAL_TICKS != 0) {
            return;
        }

        for (int index = 0; index < 2; index++) {
            double x = target.getX() + (target.getRandom().nextDouble() - 0.5D) * target.getBbWidth();
            double y = target.getY() + target.getRandom().nextDouble() * target.getBbHeight();
            double z = target.getZ() + (target.getRandom().nextDouble() - 0.5D) * target.getBbWidth();
            target.level().addParticle(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 0.0D, 0.03D, 0.0D);
        }
    }
}
