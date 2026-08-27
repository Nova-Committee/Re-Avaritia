package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.SpearMark;
import committee.nova.mods.avaritia.init.registry.ModDataAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Const.MOD_ID)
public final class SpearMarkHandler {
    private static final int PARTICLE_INTERVAL_TICKS = 4;

    private SpearMarkHandler() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        SpearMark mark = target.getExistingDataOrNull(ModDataAttachments.SPEAR_MARK);
        if (mark == null) {
            return;
        }

        long gameTime = target.level().getGameTime();
        if (!mark.isActive(gameTime)) {
            if (!target.level().isClientSide()) {
                target.removeData(ModDataAttachments.SPEAR_MARK);
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
