package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.channel.ChannelAction;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelActionPack;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelListPack;
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Dedicated-server-safe owner of persistent Tesseract channels. */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class ServerChannelManager {
    private static volatile ServerChannelManager instance;

    private final MinecraftServer server;
    private final Path saveDataPath;
    private final Map<UUID, Map<Integer, ServerChannel>> channelList = new HashMap<>();
    private final Map<UUID, UUID> channelSelectors = new HashMap<>();
    private boolean loadSuccess = true;

    private ServerChannelManager(MinecraftServer server) {
        this.server = server;
        this.saveDataPath = server.getWorldPath(LevelResource.ROOT).resolve("data/avaritia/tesseract");
        load();
    }

    @Nullable
    public static ServerChannelManager getInstance() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        ServerChannelManager current = instance;
        if (current == null || current.server != server) {
            synchronized (ServerChannelManager.class) {
                current = instance;
                if (current == null || current.server != server) {
                    if (current != null) {
                        current.release();
                    }
                    current = new ServerChannelManager(server);
                    instance = current;
                }
            }
        }
        return current;
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        synchronized (ServerChannelManager.class) {
            if (instance != null) {
                instance.release();
            }
            instance = new ServerChannelManager(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        ServerChannelManager current = instance;
        if (current != null && event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            current.save();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerChannelManager current = instance;
        if (current != null && current.server == event.getServer()) {
            current.save();
            current.release();
            instance = null;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerChannelManager current = instance;
        if (current != null && event.getEntity() instanceof ServerPlayer player) {
            current.channelSelectors.remove(player.getUUID());
            current.channelList.values().forEach(channels -> channels.values()
                    .forEach(channel -> channel.removeListener(player)));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerChannelManager current = instance;
        if (current == null || current.server != event.getServer()) {
            return;
        }
        int tickCount = event.getServer().getTickCount();
        if (tickCount % ModConfig.CHANNEL_FULL_UPDATE_RATE.get() == 0) {
            current.channelList.values().forEach(channels -> channels.values().forEach(ServerChannel::sendFullUpdate));
        } else if (tickCount % ModConfig.CHANNEL_FAST_UPDATE_RATE.get() == 0) {
            current.channelList.values().forEach(channels -> channels.values().forEach(ServerChannel::sendUpdate));
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
        } catch (Exception exception) {
            loadSuccess = false;
            Const.LOGGER.error("Failed to load Tesseract channels; saving is disabled for this run", exception);
        }
    }

    private void loadOwner(Path ownerPath) {
        UUID owner = UUID.fromString(ownerPath.getFileName().toString());
        Map<Integer, ServerChannel> channels = new HashMap<>();
        try (var files = Files.list(ownerPath)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("^(0|[1-9][0-9]{0,3})\\.dat$"))
                    .forEach(path -> loadChannel(channels, path));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to enumerate Tesseract channels in " + ownerPath, exception);
        }
        channelList.put(owner, channels);
    }

    private void loadChannel(Map<Integer, ServerChannel> channels, Path channelPath) {
        String fileName = channelPath.getFileName().toString();
        int channelId = Integer.parseInt(fileName.substring(0, fileName.length() - 4));
        try {
            CompoundTag data = NbtIo.readCompressed(channelPath, NbtAccounter.unlimitedHeap());
            ServerChannel channel = new ServerChannel(data);
            channels.put(channelId, channel);
            if (!channel.isLoadComplete()) {
                loadSuccess = false;
                Const.LOGGER.error("Tesseract channel {} contains unavailable registry entries; saving is disabled for this run", channelPath);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load Tesseract channel " + channelPath, exception);
        }
    }

    private void save() {
        if (!loadSuccess) {
            return;
        }
        try {
            Files.createDirectories(saveDataPath);
            for (var ownerEntry : channelList.entrySet()) {
                Path ownerPath = saveDataPath.resolve(ownerEntry.getKey().toString());
                Files.createDirectories(ownerPath);
                for (var channelEntry : ownerEntry.getValue().entrySet()) {
                    NbtIo.writeCompressed(channelEntry.getValue().buildData(), ownerPath.resolve(channelEntry.getKey() + ".dat"));
                }
            }
        } catch (Exception exception) {
            Const.LOGGER.error("Failed to save Tesseract channels", exception);
        }
    }

    public ServerChannel getChannel(UUID owner, int channelId) {
        Map<Integer, ServerChannel> channels = channelList.get(owner);
        return channels == null ? NullChannel.INSTANCE : channels.getOrDefault(channelId, NullChannel.INSTANCE);
    }

    public String getChannelName(UUID owner, int channelId) {
        return getChannel(owner, channelId).getName();
    }

    public void addChannelSelector(ServerPlayer player, UUID terminalOwner) {
        channelSelectors.put(player.getUUID(), terminalOwner);
        CompoundTag mine = channelNames(player.getUUID());
        CompoundTag other = player.getUUID().equals(terminalOwner) ? new CompoundTag() : channelNames(terminalOwner);
        CompoundTag shared = channelNames(Const.AVARITIA_FAKE_PLAYER.getId());
        PacketDistributor.sendToPlayer(player, new S2CChannelListPack(mine, other, shared));
    }

    private CompoundTag channelNames(UUID owner) {
        CompoundTag result = new CompoundTag();
        Map<Integer, ServerChannel> channels = channelList.get(owner);
        if (channels != null) {
            channels.forEach((id, channel) -> result.putString(Integer.toString(id), channel.getName()));
        }
        return result;
    }

    public boolean isSelecting(ServerPlayer player, UUID terminalOwner) {
        return terminalOwner.equals(channelSelectors.get(player.getUUID()));
    }

    public void removeChannelSelector(ServerPlayer player) {
        channelSelectors.remove(player.getUUID());
    }

    public void tryAddChannel(ServerPlayer player, String requestedName, boolean isPublic) {
        UUID owner = isPublic ? Const.AVARITIA_FAKE_PLAYER.getId() : player.getUUID();
        int max = isPublic ? ModConfig.MAX_PUBLIC_CHANNELS.get() : ModConfig.MAX_CHANNELS_PRE_PLAYER.get();
        Map<Integer, ServerChannel> channels = channelList.computeIfAbsent(owner, ignored -> new HashMap<>());
        if (channels.size() >= max) {
            return;
        }
        String name = normalizeName(requestedName);
        for (int id = 0; id < 10_000; id++) {
            if (!channels.containsKey(id)) {
                channels.put(id, new ServerChannel(name));
                sendChannelAction(owner, ChannelAction.ADD, name, id);
                return;
            }
        }
    }

    public boolean tryRemoveChannel(UUID owner, int channelId) {
        if (!loadSuccess) {
            return false;
        }
        Map<Integer, ServerChannel> channels = channelList.get(owner);
        if (channels == null) {
            return false;
        }
        ServerChannel channel = channels.get(channelId);
        if (channel == null || !channel.isEmpty()) {
            return false;
        }
        channels.remove(channelId);
        channel.setRemoved();
        sendChannelAction(owner, ChannelAction.REMOVE, "", channelId);
        try {
            Files.deleteIfExists(saveDataPath.resolve(owner.toString()).resolve(channelId + ".dat"));
        } catch (IOException exception) {
            Const.LOGGER.error("Failed to delete empty Tesseract channel {}:{}", owner, channelId, exception);
        }
        return true;
    }

    public void renameChannel(ChannelInfo info, String requestedName) {
        ServerChannel channel = getChannel(info.owner(), info.id());
        if (channel.isRemoved()) {
            return;
        }
        String name = normalizeName(requestedName);
        channel.setName(name);
        sendChannelAction(info.owner(), ChannelAction.ADD, name, info.id());
    }

    private static String normalizeName(String name) {
        String stripped = name == null ? "" : name.strip();
        return stripped.isEmpty() ? "Channel" : stripped.substring(0, Math.min(64, stripped.length()));
    }

    private void sendChannelAction(UUID channelOwner, ChannelAction action, String name, int channelId) {
        for (var selector : channelSelectors.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(selector.getKey());
            if (player == null) {
                continue;
            }
            byte list = listFor(player, selector.getValue(), channelOwner);
            if (list >= 0) {
                PacketDistributor.sendToPlayer(player, new S2CChannelActionPack(action, list, name, channelId));
            }
        }
    }

    private static byte listFor(ServerPlayer player, UUID terminalOwner, UUID channelOwner) {
        if (player.getUUID().equals(channelOwner)) {
            return 0;
        }
        if (terminalOwner.equals(channelOwner)) {
            return 1;
        }
        if (Const.AVARITIA_FAKE_PLAYER.getId().equals(channelOwner)) {
            return 2;
        }
        return -1;
    }

    public static void sendChannelSet(ServerPlayer player, UUID terminalOwner, UUID channelOwner, int channelId) {
        ServerChannelManager manager = getInstance();
        if (manager == null) {
            return;
        }
        String name = manager.getChannelName(channelOwner, channelId);
        byte list = listFor(player, terminalOwner, channelOwner);
        if (list >= 0 || !manager.getChannel(channelOwner, channelId).isRemoved()) {
            PacketDistributor.sendToPlayer(player, new S2CChannelActionPack(ChannelAction.SET, list, name, channelId));
        }
    }

    private void release() {
        channelList.values().forEach(channels -> channels.values().forEach(ServerChannel::setRemoved));
        channelList.clear();
        channelSelectors.clear();
    }
}
