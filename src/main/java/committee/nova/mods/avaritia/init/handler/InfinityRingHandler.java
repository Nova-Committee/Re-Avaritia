package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingKeys;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingLegacyRecovery;
import committee.nova.mods.avaritia.common.net.S2CUpdateDimensionsPack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Native server lifecycle and access enforcement for Infinity Ring dimensions. */
public final class InfinityRingHandler {
    private InfinityRingHandler() {
    }

    public static void onServerStarted(MinecraftServer server) {
        InfinityRingDimensions.restoreMissingLevels(server);
    }

    public static void onJoin(ServerPlayer player) {
        InfinityRingDimensions.evictIfUnauthorized(player);
    }

    public static void onLogin(ServerPlayer player) {
        player.server.getAllLevels().forEach(level -> {
            if (InfinityRingKeys.isPersonal(level.dimension())) {
                S2CUpdateDimensionsPack.addTo(player, level.dimension());
            }
        });
        NeutronRingLegacyRecovery.recoverAll(player);
        InfinityRingDimensions.evictIfUnauthorized(player);
    }
}
