package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.DynamicDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingKeys;
import committee.nova.mods.avaritia.common.net.S2CUpdateDimensionsPack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Environment rebind and access enforcement for Infinity Ring dimensions. */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class InfinityRingHandler {
    private InfinityRingHandler() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        InfinityRingDimensions.restoreMissingLevels(event.getServer());
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            InfinityRingDimensions.evictIfUnauthorized(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        player.level().getServer().getAllLevels().forEach(level -> {
            ResourceKey<Level> key = level.dimension();
            if (InfinityRingKeys.isPersonal(key)) {
                DynamicDimensions.rebind(level);
                S2CUpdateDimensionsPack.addTo(player, key);
            }
        });
        InfinityRingDimensions.evictIfUnauthorized(player);
    }
}
