package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.channel.ChannelAction;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelActionPack;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelListPack;
import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-instance-bound facade over the Tesseract SavedData. */
public final class ServerChannelManager {
    private static ServerChannelManager instance;
    private final MinecraftServer server;
    private final TesseractSavedData savedData;
    private final Map<UUID, UUID> selectors = new HashMap<>();

    private ServerChannelManager(MinecraftServer server) {
        this.server = server;
        this.savedData = server.overworld().getDataStorage().computeIfAbsent(TesseractSavedData.TYPE);
        importLegacyChannels();
    }

    public static @Nullable ServerChannelManager getInstance() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        if (instance == null || instance.server != server) {
            if (instance != null) instance.releaseResources();
            instance = new ServerChannelManager(server);
        }
        return instance;
    }

    public static void release(MinecraftServer server) {
        if (instance != null && instance.server == server) {
            instance.releaseResources();
            instance = null;
        }
    }

    public ServerChannel getChannel(ChannelInfo reference) {
        return savedData.get(reference);
    }

    public ServerChannel getChannel(UUID owner, int id) {
        if (owner == null || id < 0 || id > ChannelInfo.MAX_ID) {
            return NullChannel.INSTANCE;
        }
        return getChannel(new ChannelInfo(owner, id));
    }

    public void openSelector(ServerPlayer player, UUID terminalOwner) {
        selectors.put(player.getUUID(), terminalOwner);
        PacketDistributor.sendToPlayer(player, new S2CChannelListPack(
                names(player.getUUID()),
                player.getUUID().equals(terminalOwner) ? Map.of() : names(terminalOwner),
                names(Const.AVARITIA_FAKE_PLAYER.id())));
    }

    public void closeSelector(ServerPlayer player) {
        selectors.remove(player.getUUID());
    }

    public boolean isSelecting(ServerPlayer player, UUID terminalOwner) {
        return terminalOwner.equals(selectors.get(player.getUUID()));
    }

    public @Nullable ChannelInfo resolveSelection(ServerPlayer player, UUID terminalOwner, byte type, int id) {
        UUID owner = switch (type) {
            case 0 -> player.getUUID();
            case 1 -> terminalOwner;
            case 2 -> Const.AVARITIA_FAKE_PLAYER.id();
            default -> null;
        };
        if (owner == null || id < 0 || id > ChannelInfo.MAX_ID) {
            return null;
        }
        ChannelInfo reference = new ChannelInfo(owner, id);
        return getChannel(reference).isRemoved() ? null : reference;
    }

    public @Nullable ChannelInfo create(ServerPlayer player, String requestedName, boolean publicChannel) {
        if (publicChannel && !isGameMaster(player)) {
            return null;
        }
        UUID owner = publicChannel ? Const.AVARITIA_FAKE_PLAYER.id() : player.getUUID();
        Map<Integer, ServerChannel> channels = savedData.channels(owner);
        int maximum = publicChannel ? ModConfig.MAX_PUBLIC_CHANNELS.get() : ModConfig.MAX_CHANNELS_PRE_PLAYER.get();
        if (channels.size() >= maximum) {
            return null;
        }
        for (int id = 0; id <= ChannelInfo.MAX_ID; id++) {
            if (!channels.containsKey(id)) {
                ChannelInfo reference = new ChannelInfo(owner, id);
                String name = Channel.normalizeName(requestedName);
                savedData.create(reference, name);
                sendAction(owner, ChannelAction.ADD, id, name);
                return reference;
            }
        }
        return null;
    }

    public boolean rename(ServerPlayer player, ChannelInfo reference, String requestedName) {
        if (!canManage(player, reference.owner())) {
            return false;
        }
        ServerChannel channel = getChannel(reference);
        if (channel.isRemoved()) {
            return false;
        }
        channel.setName(requestedName);
        sendAction(reference.owner(), ChannelAction.ADD, reference.id(), channel.getName());
        return true;
    }

    public boolean remove(ServerPlayer player, ChannelInfo reference) {
        if (!canManage(player, reference.owner())) {
            return false;
        }
        ServerChannel channel = getChannel(reference);
        if (channel.isRemoved() || !channel.isEmpty() || !savedData.remove(reference)) {
            return false;
        }
        sendAction(reference.owner(), ChannelAction.REMOVE, reference.id(), "");
        return true;
    }

    public void sendSelected(ServerPlayer player, UUID terminalOwner, @Nullable ChannelInfo reference) {
        if (reference == null) {
            PacketDistributor.sendToPlayer(player, S2CChannelActionPack.clearSelection());
            return;
        }
        byte type = listType(player, terminalOwner, reference.owner());
        PacketDistributor.sendToPlayer(player, new S2CChannelActionPack(
                ChannelAction.SET, type, reference.id(), getChannel(reference).getName()));
    }

    private Map<Integer, String> names(UUID owner) {
        Map<Integer, String> names = new HashMap<>();
        savedData.channels(owner).forEach((id, channel) -> names.put(id, channel.getName()));
        return names;
    }

    private void sendAction(UUID owner, ChannelAction action, int id, String name) {
        selectors.forEach((playerId, terminalOwner) -> {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                byte type = listType(player, terminalOwner, owner);
                if (type >= 0) {
                    PacketDistributor.sendToPlayer(player, new S2CChannelActionPack(action, type, id, name));
                }
            }
        });
    }

    private static byte listType(ServerPlayer player, UUID terminalOwner, UUID channelOwner) {
        if (player.getUUID().equals(channelOwner)) return 0;
        if (terminalOwner.equals(channelOwner)) return 1;
        if (Const.AVARITIA_FAKE_PLAYER.id().equals(channelOwner)) return 2;
        return -1;
    }

    private static boolean canManage(ServerPlayer player, UUID owner) {
        return player.getUUID().equals(owner)
                || Const.AVARITIA_FAKE_PLAYER.id().equals(owner) && isGameMaster(player);
    }

    private static boolean isGameMaster(ServerPlayer player) {
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private void releaseResources() {
        savedData.release();
        selectors.clear();
    }

    private void importLegacyChannels() {
        if (savedData.legacyImported()) return;
        Path root = server.getWorldPath(LevelResource.ROOT).resolve("data/avaritia/tesseract");
        if (!Files.isDirectory(root)) {
            savedData.markLegacyImported();
            return;
        }

        try {
            List<LegacyChannel> staged = new ArrayList<>();
            try (var owners = Files.list(root)) {
                for (Path ownerPath : owners.filter(Files::isDirectory).toList()) {
                    UUID owner;
                    try {
                        owner = UUID.fromString(ownerPath.getFileName().toString());
                    } catch (IllegalArgumentException ignored) {
                        continue;
                    }
                    try (var files = Files.list(ownerPath)) {
                        for (Path channelFile : files.filter(Files::isRegularFile).toList()) {
                            String fileName = channelFile.getFileName().toString();
                            if (!fileName.matches("^(0|[1-9][0-9]{0,3})\\.dat$")) continue;
                            int id = Integer.parseInt(fileName.substring(0, fileName.length() - 4));
                            staged.add(new LegacyChannel(new ChannelInfo(owner, id), readLegacy(channelFile)));
                        }
                    }
                }
            }

            // Apply only after every legacy file decoded successfully. Existing
            // SavedData wins, which also makes a retry after an interrupted save idempotent.
            for (LegacyChannel legacy : staged) {
                if (savedData.get(legacy.reference()).isRemoved()) {
                    savedData.create(legacy.reference(), legacy.data());
                }
            }
            savedData.markLegacyImported();
            Const.LOGGER.info("Imported {} legacy Tesseract channels into SavedData", staged.size());
        } catch (Exception exception) {
            Const.LOGGER.error("Legacy Tesseract import failed; it will be retried and the source files were left untouched", exception);
        }
    }

    private static Channel.Data readLegacy(Path file) throws IOException {
        CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        List<Channel.ItemEntry> items = new ArrayList<>();
        for (var entry : root.getCompoundOrEmpty("items").entrySet()) {
            long amount = root.getCompoundOrEmpty("items").getLongOr(entry.getKey(), 0);
            if (amount <= 0) continue;
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                throw new IOException("Unknown legacy item " + entry.getKey() + " in " + file);
            }
            ItemResource resource = ItemResource.of(BuiltInRegistries.ITEM.getValue(id));
            if (!resource.isEmpty()) items.add(new Channel.ItemEntry(resource, amount));
        }

        List<Channel.FluidEntry> fluids = new ArrayList<>();
        for (var entry : root.getCompoundOrEmpty("fluids").entrySet()) {
            long amount = root.getCompoundOrEmpty("fluids").getLongOr(entry.getKey(), 0);
            if (amount <= 0) continue;
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null || !BuiltInRegistries.FLUID.containsKey(id)) {
                throw new IOException("Unknown legacy fluid " + entry.getKey() + " in " + file);
            }
            FluidResource resource = FluidResource.of(BuiltInRegistries.FLUID.getValue(id));
            if (!resource.isEmpty()) fluids.add(new Channel.FluidEntry(resource, amount));
        }
        long energy = root.getCompoundOrEmpty("energies").getLongOr("avaritia:forge_energy", 0);
        if ((long) items.size() + fluids.size() + (energy > 0 ? 1 : 0) > Channel.MAX_VARIANTS) {
            throw new IOException("Legacy channel exceeds supported variant count: " + file);
        }
        return new Channel.Data(root.getStringOr("name", Channel.DEFAULT_NAME), items, fluids, energy);
    }

    private record LegacyChannel(ChannelInfo reference, Channel.Data data) {
    }
}
