package committee.nova.mods.avaritia.client.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.C2SElytraSpeedUpPacket;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class InfinityElytraClientHandler {
    private static final int LAUNCH_PACKET_INTERVAL = 4;
    private static final int BOOST_PACKET_INTERVAL = 1;
    private static boolean lastFlyingIntent = false;
    private static boolean lastBoosting = false;
    private static int packetCooldown = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.screen != null) {
            reset();
            return;
        }
        if (!InfinityElytraUtils.hasInfinityElytraEquipped(player)) {
            reset();
            return;
        }

        boolean canRequestGlide = !player.isPassenger()
                && !player.isInWater()
                && !player.onClimbable()
                && !player.getAbilities().flying;
        boolean wantsLaunch = canRequestGlide
                && !player.isFallFlying()
                && minecraft.options.keyJump.isDown();
        boolean wantsBoost = (player.isFallFlying() || wantsLaunch)
                && minecraft.options.keySprint.isDown()
                && !minecraft.options.keyShift.isDown();

        sync(wantsLaunch || wantsBoost, wantsBoost);

        if (player.isFallFlying()) {
            spawnTrailParticles(player);
        }
    }

    private static void sync(boolean flyingIntent, boolean boosting) {
        if (!flyingIntent) {
            reset();
            return;
        }

        boolean changed = flyingIntent != lastFlyingIntent || boosting != lastBoosting;
        if (changed || packetCooldown <= 0) {
            NetworkHandler.sendToServer(new C2SElytraSpeedUpPacket(flyingIntent, boosting));
            lastFlyingIntent = flyingIntent;
            lastBoosting = boosting;
            packetCooldown = boosting ? BOOST_PACKET_INTERVAL : LAUNCH_PACKET_INTERVAL;
        } else {
            packetCooldown--;
        }
    }

    private static void reset() {
        lastFlyingIntent = false;
        lastBoosting = false;
        packetCooldown = 0;
    }

    private static void spawnTrailParticles(LivingEntity entity) {
        RandomSource random = entity.level().getRandom();
        for (int i = 0; i < 3; i++) {
            double offsetX = random.nextGaussian() * 0.1;
            double offsetY = random.nextGaussian() * 0.1;
            double offsetZ = random.nextGaussian() * 0.1;
            double x = entity.getX() - entity.getLookAngle().x * 1.5 + offsetX;
            double y = entity.getY() + entity.getBbHeight() / 2 + offsetY;
            double z = entity.getZ() - entity.getLookAngle().z * 1.5 + offsetZ;
            entity.level().addParticle(
                    ParticleTypes.CLOUD, x, y, z,
                    -entity.getDeltaMovement().x * 0.5 + offsetX * 0.5,
                    -entity.getDeltaMovement().y * 0.5 + offsetY * 0.5,
                    -entity.getDeltaMovement().z * 0.5 + offsetZ * 0.5
            );
        }
    }
}