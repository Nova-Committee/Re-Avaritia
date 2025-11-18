package committee.nova.mods.avaritia.core.name;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.chest.ChannelState;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;

/**
 * @author cnlimiter
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class NameCacheManager {
    private static volatile NameCacheManager instance;

    public static NameCacheManager getInstance() {
        if (instance == null) {
            synchronized (NameCacheManager.class) {
                if (instance == null) {
                    instance = new NameCacheManager(ServerLifecycleHooks.getCurrentServer());
                }
            }
        }
        return instance;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerLoad(ServerAboutToStartEvent event) {
        if (instance == null) {
            synchronized (NameCacheManager.class) {
                if (instance == null) instance = new NameCacheManager(event.getServer());
            }
        }
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (!event.getLevel().isClientSide()) getInstance().save(event.getLevel().getServer());
    }

    @SubscribeEvent
    public static void onServerDown(ServerStoppingEvent event) {
        getInstance().save(event.getServer());
        instance = null;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        getInstance().userCache.getCompound("nameCache").putString(event.getEntity().getUUID().toString(), event.getEntity().getGameProfile().getName());
        //NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CChannelStatePack(ChannelState.NAME, userCache));
        PacketDistributor.sendToAllPlayers(new S2CInfinityChestStatePack(ChannelState.NAME, getInstance().userCache));
        if (!getInstance().loadSuccess)
            event.getEntity().sendSystemMessage(Component.translatable("info.avaritia.channel.load_error"));
    }

    public CompoundTag userCache;
    private File saveDataPath;
    private boolean loadSuccess = true;
    private final MinecraftServer server;

    private NameCacheManager(MinecraftServer server) {
        this.server = server;
        this.load();
    }

    private void load() {
        this.saveDataPath = new File(server.getWorldPath(LevelResource.ROOT).toFile(), "data/avaritia");
        try {
            if (!saveDataPath.exists()) saveDataPath.mkdirs();

            File userCacheFile = new File(saveDataPath, "UserCache.dat");
            if (userCacheFile.exists() && userCacheFile.isFile()) {
                this.userCache = NbtIo.readCompressed(userCacheFile.toPath(), NbtAccounter.unlimitedHeap());
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
            NbtIo.writeCompressed(this.userCache, userCache.toPath());
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
