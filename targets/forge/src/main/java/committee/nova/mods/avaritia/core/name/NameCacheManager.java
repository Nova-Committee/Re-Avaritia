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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;

/**
 * @author cnlimiter
 */
@Mod.EventBusSubscriber(modid = Const.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    private static void newInstance(MinecraftServer server) {
        if (instance == null) {
            synchronized (NameCacheManager.class) {
                if (instance == null) instance = new NameCacheManager(server);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerLoad(ServerAboutToStartEvent event) {
        newInstance(event.getServer());
    }



    public CompoundTag userCache;
    private File saveDataPath;
    private boolean loadSuccess = true;
    private final MinecraftServer server;

    private NameCacheManager(MinecraftServer server) {
        this.server = server;
        MinecraftForge.EVENT_BUS.register(this);
        this.load();
    }

    @SubscribeEvent
    public void onLevelSave(LevelEvent.Save event) {
        if (!event.getLevel().isClientSide()) this.save(event.getLevel().getServer());
    }

    @SubscribeEvent
    public void onServerDown(ServerStoppingEvent event) {
        this.save(event.getServer());
        MinecraftForge.EVENT_BUS.unregister(this);
        instance = null;
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        this.userCache.getCompound("nameCache").putString(event.getEntity().getUUID().toString(), event.getEntity().getGameProfile().getName());
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CChannelStatePack(ChannelState.NAME, userCache));
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CInfinityChestStatePack(ChannelState.NAME, userCache));
        if (!loadSuccess)
            event.getEntity().sendSystemMessage(Component.translatable("info.avaritia.channel.load_error"));
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
