package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import org.apache.logging.log4j.LogManager;  
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author cnlimiter
 */
@Mod.EventBusSubscriber(modid = Const.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerChestManager {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static volatile ServerChestManager instance;

    @Nullable
    public static ServerChestManager getInstance() {
        if (instance == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                LOGGER.warn("[ServerChestManager] getInstance() called but no server is running (client-side?). Returning null.");
                return null;
            }
            synchronized (ServerChestManager.class) {
                if (instance == null) {
                    instance = new ServerChestManager(server);
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

    private File saveDataPath;
    private boolean loadSuccess = true;
    private final MinecraftServer server;
    private final HashMap<UUID, HashMap<UUID, ServerChestHandler>> chestList = new HashMap<>();

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;
        int tickCount = server.getTickCount();
        if (tickCount % ModConfig.CHANNEL_FULL_UPDATE_RATE.get() == 0)
            chestList.forEach((uuid, map) -> map.forEach((id, chestHandler) -> chestHandler.sendFullUpdate()));
        else if (tickCount % ModConfig.CHANNEL_FAST_UPDATE_RATE.get() == 0)
            chestList.forEach((uuid, map) -> map.forEach((id, channel) -> channel.sendUpdate()));
    }

    @SubscribeEvent
    public void onLevelSave(LevelEvent.Save event) {
        if (!event.getLevel().isClientSide()) save(event.getLevel().getServer());
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
                        Const.LOGGER.debug(Component.translatable("info.avaritia.infinity_chest.load_success", dir.getName(), channelID, "").getString());
                    }
                    chestList.put(player, playerChannels);
                }
            }
            Const.LOGGER.debug(Component.translatable("info.avaritia.infinity_chest.load_finish").getString());

        } catch (Exception e) {
            loadSuccess = false;
            throw new RuntimeException("在加载数据的时候出错了！ 本次游戏将不会保存数据！", e);
        }
    }

    private void save(MinecraftServer server) {
        if (!loadSuccess) return;
        try {
            chestList.forEach((uuid, channels) -> {
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
                    Const.LOGGER.debug(Component.translatable("info.avaritia.infinity_chest.save_success", uuid, id, "").getString());
                });
            });

        } catch (Exception e) {
            throw new RuntimeException("在保存数据的时候出错了！ 什么情况呢？", e);
        }
    }

    public ServerChestHandler getChest(UUID ownerUUID, UUID chestId) {
        if (ownerUUID == null || chestId == null) {
            LOGGER.warn("[ServerChestManager] getChest() called with invalid owner or chest id. owner={}, chestId={}", ownerUUID, chestId);
            return new ServerChestHandler();
        }
        HashMap<UUID, ServerChestHandler> list = chestList.computeIfAbsent(ownerUUID, ignored -> new HashMap<>());
        return list.computeIfAbsent(chestId, ignored -> new ServerChestHandler());
    }

    public void tryAddChest(ServerPlayer player, UUID chestId) {
        UUID uuid = player.getUUID();
        HashMap<UUID, ServerChestHandler> playerChannels;
        if (chestList.containsKey(uuid)) playerChannels = chestList.get(uuid);
        else {
            playerChannels = new HashMap<>();
            chestList.put(uuid, playerChannels);
        }
        playerChannels.putIfAbsent(chestId, new ServerChestHandler());
        Const.LOGGER.debug(Component.translatable("info.avaritia.infinity_chest.add_success", uuid, chestId, "").getString());
    }



    public boolean tryRemoveChest(ServerPlayer player, UUID chestId) {
        UUID channelOwner = player.getUUID();
        if (!chestList.containsKey(channelOwner)) return false;
        HashMap<UUID, ServerChestHandler> list = chestList.get(channelOwner);
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
