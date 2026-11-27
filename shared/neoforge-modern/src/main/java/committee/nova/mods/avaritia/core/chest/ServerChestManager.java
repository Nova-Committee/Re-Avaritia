package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.Const;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

/** 绑定当前服务端实例的无限箱 {@link InfinityChestSavedData} 门面。 */
public final class ServerChestManager {
    private static final Pattern CHANNEL_FILE = Pattern.compile(
            "^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\\.dat$");

    private static ServerChestManager instance;

    private final MinecraftServer server;
    private final InfinityChestSavedData savedData;

    private ServerChestManager(MinecraftServer server) {
        this.server = server;
        this.savedData = server.overworld().getDataStorage().computeIfAbsent(InfinityChestSavedData.TYPE);
        importLegacyFiles();
    }

    public static ServerChestManager get(MinecraftServer server) {
        if (instance == null || instance.server != server) {
            if (instance != null) {
                instance.releaseResources();
            }
            instance = new ServerChestManager(server);
        }
        return instance;
    }

    public static void release(MinecraftServer server) {
        if (instance != null && instance.server == server) {
            instance.releaseResources();
            instance = null;
        }
    }

    public static @Nullable ServerChestManager getInstance() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : get(server);
    }

    public ServerChestHandler getOrCreateChest(UUID owner, UUID channelId) {
        if (owner == null || channelId == null) {
            throw new IllegalArgumentException("Infinity chest owner and channel id must be non-null");
        }
        return savedData.getOrCreate(owner, channelId);
    }

    private void importLegacyFiles() {
        if (savedData.legacyImported()) {
            return;
        }
        Path root = server.getWorldPath(LevelResource.ROOT).resolve("data/avaritia/infinity_chest");
        if (!Files.isDirectory(root)) {
            savedData.markLegacyImported();
            return;
        }

        boolean complete = true;
        try (var owners = Files.list(root)) {
            for (Path ownerPath : owners.filter(Files::isDirectory).toList()) {
                UUID owner;
                try {
                    owner = UUID.fromString(ownerPath.getFileName().toString());
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                complete &= importOwner(owner, ownerPath);
            }
        } catch (IOException exception) {
            Const.LOGGER.error("Failed to enumerate legacy infinity chest data at {}", root, exception);
            complete = false;
        }

        if (complete) {
            savedData.markLegacyImported();
        } else {
            Const.LOGGER.warn("Legacy infinity chest import remains pending; source files were left untouched");
        }
    }

    private boolean importOwner(UUID owner, Path ownerPath) {
        boolean complete = true;
        try (var files = Files.list(ownerPath)) {
            for (Path channelPath : files.filter(Files::isRegularFile).toList()) {
                var matcher = CHANNEL_FILE.matcher(channelPath.getFileName().toString());
                if (!matcher.matches()) {
                    continue;
                }
                UUID channel = UUID.fromString(matcher.group(1));
                if (savedData.contains(owner, channel)) {
                    continue;
                }
                try {
                    CompoundTag tag = NbtIo.readCompressed(channelPath, NbtAccounter.unlimitedHeap());
                    var decoded = ServerChestHandler.readLegacyData(server, tag);
                    if (decoded.isEmpty()) {
                        Const.LOGGER.error("Legacy infinity chest channel {} cannot be imported losslessly", channelPath);
                        complete = false;
                        continue;
                    }
                    savedData.importIfAbsent(owner, channel, decoded.get());
                    Const.LOGGER.info("Imported legacy infinity chest channel {}", channelPath);
                } catch (Exception exception) {
                    Const.LOGGER.error("Failed to import legacy infinity chest channel {}", channelPath, exception);
                    complete = false;
                }
            }
        } catch (IOException exception) {
            Const.LOGGER.error("Failed to enumerate legacy infinity chest owner directory {}", ownerPath, exception);
            return false;
        }
        return complete;
    }

    private void releaseResources() {
        savedData.release();
    }
}
