package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Const.MOD_ID)
public class InfinityElytraHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        InfinityElytraUtils.updateCuriosFallbackFallFlying(player);
        damageNearbyWhileFlying(player);
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && InfinityElytraUtils.hasInfinityElytraEquipped(player)) {
            event.setDistance(0.0F);
            event.setDamageMultiplier(0.0F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        InfinityElytraUtils.clearCuriosFallbackFallFlying(event.getEntity());
    }

    private static void damageNearbyWhileFlying(ServerPlayer player) {
        if (!player.isFallFlying() || !InfinityElytraUtils.hasInfinityElytraEquipped(player)) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        double range = 3.0;
        AABB box = player.getBoundingBox().inflate(range);

        level.getEntitiesOfClass(LivingEntity.class, box, target -> canDamage(player, target))
                .forEach(target -> {
                    if (target.hurtServer(level, ModDamageTypes.source(player), ModConfig.infinityElytraFlyingRangeDamage.get().floatValue())) {
                        double dx = target.getX() - player.getX();
                        double dz = target.getZ() - player.getZ();
                        target.push(dx, 0.5, dz);
                    }
                });
    }

    private static boolean canDamage(Player user, LivingEntity target) {
        return target != user && target.isAlive() && !target.isSpectator()
                && !(target instanceof Player player && player.isCreative());
    }
}