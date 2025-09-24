package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.util.StorageUtils;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author: cnlimiter
 */
@Mod.EventBusSubscriber(modid = Const.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerChestManager {
    private static volatile ServerChestManager instance;

    public static ServerChestManager getInstance() {
        if (instance == null) {
            synchronized (ServerChestManager.class) {
                if (instance == null) {
                    instance = new ServerChestManager(ServerLifecycleHooks.getCurrentServer());
                }
            }
        }
        return instance;
    }

    private static void newInstance(MinecraftServer server) {
        if (instance == null) {
            synchronized (ServerChestManager.class) {
                if (instance == null) instance = new ServerChestManager(server);
            }
        }
    }

    @SubscribeEvent
    public static void onServerLoad(ServerAboutToStartEvent event) {
        newInstance(event.getServer());
    }


    @Getter
    private CompoundTag userCache;
    private File saveDataPath;
    private boolean loadSuccess = true;
    private final MinecraftServer server;
    private final HashMap<UUID, HashMap<UUID, ServerChestHandler>> channelList = new HashMap<>();

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        this.userCache.getCompound("nameCache").putString(event.getEntity().getUUID().toString(), event.getEntity().getGameProfile().getName());
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CInfinityChestStatePack(ChannelState.NAME, userCache));
        if (!loadSuccess)
            event.getEntity().sendSystemMessage(Component.translatable("info.avaritia.channel.load_error"));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;
        int tickCount = server.getTickCount();
        if (tickCount % ModConfig.CHANNEL_FULL_UPDATE_RATE.get() == 0)
            channelList.forEach((uuid, map) -> map.forEach((id, channel) -> channel.sendFullUpdate()));
        else if (tickCount % ModConfig.CHANNEL_FAST_UPDATE_RATE.get() == 0)
            channelList.forEach((uuid, map) -> map.forEach((id, channel) -> channel.sendUpdate()));
    }

    @SubscribeEvent
    public void onLevelSave(LevelEvent.Save event) {
        if (isOverworld(event.getLevel())) save(event.getLevel().getServer());
    }

    @SubscribeEvent
    public void onServerDown(ServerStoppingEvent event) {
        this.save(event.getServer());
        MinecraftForge.EVENT_BUS.unregister(this);
        instance = null;
    }

    private ServerChestManager(MinecraftServer server) {
        this.server = server;
        MinecraftForge.EVENT_BUS.register(this);
        this.load();
    }

    private void load() {
        this.saveDataPath = new File(server.getWorldPath(LevelResource.ROOT).toFile(), "data/avaritia/infinity_chest");
        try {
            if (!saveDataPath.exists()) saveDataPath.mkdirs();

            File userCacheFile = new File(saveDataPath, "UserCache.dat");
            if (userCacheFile.exists() && userCacheFile.isFile()) {
                this.userCache = NbtIo.readCompressed(userCacheFile);
                if (!this.userCache.contains("nameCache")) this.initializeNameCache();
            } else {
                this.initializeNameCache();
            }
            Const.LOGGER.info(Component.translatable("info.avaritia.channel.load_success").getString());

            File[] channelDirs = saveDataPath.listFiles(pathname -> pathname.isDirectory() && pathname.getName()
                    .matches(StorageUtils.UUID_REGEX));
            if (channelDirs != null) {
                for (File dir : channelDirs) {
                    UUID player = UUID.fromString(dir.getName());
                    File[] channels = dir.listFiles(pathname -> pathname.isFile() && pathname.getName().matches("^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\\.dat$"));
                    if (channels == null) continue;
                    HashMap<UUID, ServerChestHandler> playerChannels = new HashMap<>();
                    for (File channelFile : channels) {
                        UUID channelID = UUID.fromString(channelFile.getName().substring(0, channelFile.getName().lastIndexOf(".")));
                        CompoundTag channelDat = NbtIo.readCompressed(channelFile);
                        ServerChestHandler channel = new ServerChestHandler(channelDat);
                        playerChannels.put(channelID, channel);
                        Const.LOGGER.info(Component.translatable("info.avaritia.channel.load_success", dir.getName(), channelID, "").getString());
                    }
                    channelList.put(player, playerChannels);
                }
            }
            Const.LOGGER.info(Component.translatable("info.avaritia.channel.load_finish").getString());

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

            channelList.forEach((uuid, channels) -> {
                File user = new File(saveDataPath, uuid.toString());
                if (!user.exists()) user.mkdir();
                channels.forEach((id, channel) -> {
                    File channelDat = new File(user, id.toString() + ".dat");
                    try {
                        if (!channelDat.exists()) channelDat.createNewFile();
                        NbtIo.writeCompressed(channel.buildData(), channelDat);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Const.LOGGER.info(Component.translatable("info.avaritia.channel.save_success", uuid, id, "").getString());
                });
            });

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

    private boolean isOverworld(LevelAccessor level) {
        return !level.isClientSide();
    }

    public String getUserName(UUID uuid) {
        String userName = userCache.getCompound("nameCache").getString(uuid.toString());
        if (userName.isEmpty()) {
            userCache.getCompound("nameCache").putString(uuid.toString(), "unknownUser");
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CInfinityChestStatePack(ChannelState.NAME, userCache));
            userName = "unknownUser";
        }
        return userName;
    }

    public ServerChestHandler getChannel(UUID ownerUUID, UUID channelId) {
        if (channelList.containsKey(ownerUUID)) {
            HashMap<UUID, ServerChestHandler> list = channelList.get(ownerUUID);
            if (list.containsKey(channelId)) return list.get(channelId);
        }
        return new ServerChestHandler();
    }

    public void tryAddChannel(ServerPlayer player, UUID chestId) {
        UUID uuid = player.getUUID();
        HashMap<UUID, ServerChestHandler> playerChannels;
        if (channelList.containsKey(uuid)) playerChannels = channelList.get(uuid);
        else {
            playerChannels = new HashMap<>();
            channelList.put(uuid, playerChannels);
        }
        playerChannels.put(chestId, new ServerChestHandler());
        Const.LOGGER.info(Component.translatable("info.avaritia.channel.add_success", uuid, chestId, "").getString());
    }



    public boolean tryRemoveChannel(ServerPlayer player, UUID chestId) {
        UUID channelOwner = player.getUUID();
        if (!channelList.containsKey(channelOwner)) return false;
        HashMap<UUID, ServerChestHandler> list = channelList.get(channelOwner);
        if (!list.containsKey(chestId)) return false;
        ServerChestHandler channel = list.get(chestId);
        if (!channel.isEmpty()) return false;
        if (list.remove(chestId) != null) {
            channel.setRemoved();
            File file = new File(saveDataPath, channelOwner + "/" + chestId + ".dat");
            file.delete();
            return true;
        }
        return false;
    }
}
