package committee.nova.mods.avaritia.core.name;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelStatePack;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;

/**
 * @author cnlimiter
 */
public class NameCacheManager {
    private static volatile NameCacheManager instance;

    public static NameCacheManager getInstance() {
        if (instance == null) {
            synchronized (NameCacheManager.class) {
                if (instance == null) {
                    instance = new NameCacheManager(committee.nova.mods.avaritia.core.io.StorageLifecycle.getServer());
                }
            }
        }
        return instance;
    }

    private static void newInstance(MinecraftServer server) {
        if (instance == null) {
            synchronized (NameCacheManager.class) {
                if (instance == null) instance = new NameCacheManager(server);
            }
        }
    }

    public static void onServerLoad(MinecraftServer server) {
        newInstance(server);
    }



    public CompoundTag userCache;
    private File saveDataPath;
    private boolean loadSuccess = true;
    private final MinecraftServer server;

    private NameCacheManager(MinecraftServer server) {
        this.server = server;
        this.load();
    }

    public void onLevelSave(MinecraftServer server) {
        save(server);
    }

    public void onServerDown(MinecraftServer server) {
        this.save(server);
        instance = null;
    }

    public void onPlayerJoin(net.minecraft.server.level.ServerPlayer player) {
        this.userCache.getCompound("nameCache").putString(player.getUUID().toString(), player.getGameProfile().getName());
        NetworkHandler.CHANNEL.sendToAll(server, new S2CChannelStatePack(ChannelState.NAME, userCache));
        NetworkHandler.CHANNEL.sendToAll(server, new S2CInfinityChestStatePack(ChannelState.NAME, userCache));
        if (!loadSuccess)
            player.sendSystemMessage(Component.translatable("info.avaritia.channel.load_error"));
    }

    private void load() {
        this.saveDataPath = new File(server.getWorldPath(LevelResource.ROOT).toFile(), "data/avaritia");
        try {
            if (!saveDataPath.exists()) saveDataPath.mkdirs();

            File userCacheFile = new File(saveDataPath, "UserCache.dat");
            if (userCacheFile.exists() && userCacheFile.isFile()) {
                this.userCache = NbtIo.readCompressed(userCacheFile);
                if (!this.userCache.contains("nameCache")) this.initializeNameCache();
            } else {
                this.initializeNameCache();
            }
        } catch (Exception e) {
            loadSuccess = false;
            throw new RuntimeException("在加载数据的时候出错了！ 本次游戏将不会保存数据！", e);
        }
    }

    private void save(MinecraftServer server) {
        if (!loadSuccess) return;
        try {
            File userCache = new File(saveDataPath, "UserCache.dat");
            if (!userCache.exists()) userCache.createNewFile();
            NbtIo.writeCompressed(this.userCache, userCache);
        } catch (Exception e) {
            throw new RuntimeException("在保存数据的时候出错了！ 什么情况呢？", e);
        }
    }

    private void initializeUserCache() {
        this.userCache = new CompoundTag();
        this.userCache.putInt("dataVersion", 1);
    }

    private void initializeNameCache() {
        CompoundTag nameCache = new CompoundTag();
        nameCache.putString(Const.AVARITIA_FAKE_PLAYER.getId().toString(), Const.AVARITIA_FAKE_PLAYER.getName());
        if (userCache == null) this.initializeUserCache();
        this.userCache.put("nameCache", nameCache);
    }
}
