package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Const.MOD_ID)
public final class ServerChestManager {
    private static volatile ServerChestManager instance;

    private final MinecraftServer server;
    private final Path saveDataPath;
    private final Map<UUID, Map<UUID, ServerChestHandler>> chestList = new HashMap<>();
    private boolean loadSuccess = true;

    private ServerChestManager(MinecraftServer server) {
        this.server = server;
        this.saveDataPath = server.getWorldPath(LevelResource.ROOT).resolve("data/avaritia/infinity_chest");
        load();
    }

    @Nullable
    public static ServerChestManager getInstance() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            Const.LOGGER.warn("Infinity chest manager requested without a running server");
            return null;
        }
        ServerChestManager current = instance;
        if (current == null || current.server != server) {
            synchronized (ServerChestManager.class) {
                current = instance;
                if (current == null || current.server != server) {
                    if (current != null) {
                        current.release();
                    }
                    current = new ServerChestManager(server);
                    instance = current;
                }
            }
        }
        return current;
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        synchronized (ServerChestManager.class) {
            if (instance != null) {
                instance.release();
            }
            instance = new ServerChestManager(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        ServerChestManager current = instance;
        if (current != null && event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            current.save();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerChestManager current = instance;
        if (current != null && current.server == event.getServer()) {
            current.save();
            current.release();
            instance = null;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerChestManager current = instance;
        if (current != null && event.getEntity() instanceof ServerPlayer player) {
            current.chestList.values().forEach(channels -> channels.values()
                    .forEach(channel -> channel.removeListener(player)));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerChestManager current = instance;
        if (current == null || current.server != event.getServer()) {
            return;
        }
        int tickCount = event.getServer().getTickCount();
        if (tickCount % ModConfig.CHANNEL_FULL_UPDATE_RATE.get() == 0) {
            current.chestList.values().forEach(channels -> channels.values().forEach(ServerChestHandler::sendFullUpdate));
        } else if (tickCount % ModConfig.CHANNEL_FAST_UPDATE_RATE.get() == 0) {
            current.chestList.values().forEach(channels -> channels.values().forEach(ServerChestHandler::sendUpdate));
        }
    }

    private void load() {
        try {
            Files.createDirectories(saveDataPath);
            try (var owners = Files.list(saveDataPath)) {
                owners.filter(Files::isDirectory)
                        .filter(path -> path.getFileName().toString().matches(StorageUtils.UUID_REGEX))
                        .forEach(this::loadOwner);
            }
            Const.LOGGER.debug("Loaded {} infinity chest owner directories", chestList.size());
        } catch (Exception exception) {
            loadSuccess = false;
            Const.LOGGER.error("Failed to load infinity chest data; saving is disabled for this run", exception);
        }
    }

    private void loadOwner(Path ownerPath) {
        UUID owner = UUID.fromString(ownerPath.getFileName().toString());
        Map<UUID, ServerChestHandler> channels = new HashMap<>();
        try (var files = Files.list(ownerPath)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches(
                            "^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\\.dat$"))
                    .forEach(path -> loadChannel(channels, path));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to enumerate infinity chest owner directory " + ownerPath, exception);
        }
        chestList.put(owner, channels);
    }

    private void loadChannel(Map<UUID, ServerChestHandler> channels, Path channelPath) {
        String fileName = channelPath.getFileName().toString();
        UUID channelId = UUID.fromString(fileName.substring(0, fileName.length() - 4));
        try {
            CompoundTag data = NbtIo.readCompressed(channelPath, NbtAccounter.unlimitedHeap());
            ServerChestHandler channel = new ServerChestHandler(server, data);
            channels.put(channelId, channel);
            if (!channel.isLoadComplete()) {
                loadSuccess = false;
                Const.LOGGER.error("Infinity chest channel {} contains unavailable items; saving is disabled for this run", channelPath);
            }
            Const.LOGGER.debug("Loaded infinity chest channel {}", channelPath);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load infinity chest channel " + channelPath, exception);
        }
    }

    private void save() {
        if (!loadSuccess) {
            return;
        }
        try {
            Files.createDirectories(saveDataPath);
            for (var ownerEntry : chestList.entrySet()) {
                Path ownerPath = saveDataPath.resolve(ownerEntry.getKey().toString());
                Files.createDirectories(ownerPath);
                for (var channelEntry : ownerEntry.getValue().entrySet()) {
                    Path channelPath = ownerPath.resolve(channelEntry.getKey() + ".dat");
                    NbtIo.writeCompressed(channelEntry.getValue().buildData(), channelPath);
                    Const.LOGGER.debug("Saved infinity chest channel {}", channelPath);
                }
            }
        } catch (Exception exception) {
            Const.LOGGER.error("Failed to save infinity chest data", exception);
        }
    }

    public ServerChestHandler getOrCreateChest(UUID owner, UUID channelId) {
        if (owner == null || channelId == null) {
            throw new IllegalArgumentException("Infinity chest owner and channel id must be non-null");
        }
        return chestList.computeIfAbsent(owner, ignored -> new HashMap<>())
                .computeIfAbsent(channelId, ignored -> new ServerChestHandler(server));
    }

    public ServerChestHandler getChest(UUID owner, UUID channelId) {
        return getOrCreateChest(owner, channelId);
    }

    public void tryAddChest(ServerPlayer player, UUID channelId) {
        getOrCreateChest(player.getUUID(), channelId);
    }

    public boolean tryRemoveChest(ServerPlayer player, UUID channelId) {
        if (!loadSuccess) {
            return false;
        }
        Map<UUID, ServerChestHandler> channels = chestList.get(player.getUUID());
        if (channels == null) {
            return false;
        }
        ServerChestHandler channel = channels.get(channelId);
        if (channel == null || !channel.isEmpty()) {
            return false;
        }
        channels.remove(channelId);
        channel.setRemoved();
        try {
            Files.deleteIfExists(saveDataPath.resolve(player.getUUID().toString()).resolve(channelId + ".dat"));
        } catch (IOException exception) {
            Const.LOGGER.error("Failed to delete empty infinity chest channel {}", channelId, exception);
        }
        return true;
    }

    private void release() {
        chestList.values().forEach(channels -> channels.values().forEach(ServerChestHandler::setRemoved));
        chestList.clear();
    }
}
