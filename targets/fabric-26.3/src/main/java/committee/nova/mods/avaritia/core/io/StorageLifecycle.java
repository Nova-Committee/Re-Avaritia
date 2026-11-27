package committee.nova.mods.avaritia.core.io;

import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import committee.nova.mods.avaritia.core.chest.ServerChestManager;
import committee.nova.mods.avaritia.core.name.NameCacheManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class StorageLifecycle {
    private static MinecraftServer server;
    private StorageLifecycle() {}
    public static MinecraftServer getServer() { return server; }
    public static void start(MinecraftServer value) {
        server = value;
        NameCacheManager.onServerLoad(value);
        ServerChannelManager.onServerLoad(value);
        ServerChestManager.onServerLoad(value);
    }
    public static void tick(MinecraftServer value) {
        if (server != value) return;
        ServerChannelManager.getInstance().onTick(value);
        ServerChestManager.getInstance().onTick(value);
    }
    public static void save(MinecraftServer value) {
        if (server != value) return;
        NameCacheManager.getInstance().onLevelSave(value);
        ServerChannelManager.getInstance().onLevelSave(value);
        ServerChestManager.getInstance().onLevelSave(value);
    }
    public static void stop(MinecraftServer value) {
        if (server != value) return;
        try {
            NameCacheManager.getInstance().onServerDown(value);
            ServerChannelManager.getInstance().onServerDown(value);
            ServerChestManager.getInstance().onServerDown(value);
        } finally { server = null; }
    }
    public static void join(ServerPlayer player) { if (server != null) NameCacheManager.getInstance().onPlayerJoin(player); }
}
