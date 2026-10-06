package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.common.net.S2CSingularitiesPack;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Shares exactly the server's committed definition snapshot with clients. */
public final class DataPackSyncHandler {
    private DataPackSyncHandler() {}

    public static void onPlayerLogin(ServerPlayer player) {
        NetworkHandler.CHANNEL.sendTo(player,
                new S2CSingularitiesPack(SingularityReloadListener.INSTANCE.getAllSingularities().values()));
    }

    public static void onDatapackSync(MinecraftServer server) {
        NetworkHandler.CHANNEL.sendToAll(server,
                new S2CSingularitiesPack(SingularityReloadListener.INSTANCE.getAllSingularities().values()));
    }
}
