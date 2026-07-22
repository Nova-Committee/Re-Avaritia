package committee.nova.mods.avaritia.core.chest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 主世界 {@link SavedData} 中的全部无限箱通道。 */
public final class InfinityChestSavedData extends SavedData {
    public static final String NAME = "infinity_chests";

    private static final Codec<StoredChannel> STORED_CHANNEL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(StoredChannel::owner),
            UUIDUtil.CODEC.fieldOf("channel").forGetter(StoredChannel::channel),
            ChestHandler.STORED_ITEM_CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(StoredChannel::items)
    ).apply(instance, StoredChannel::new));

    public static final Codec<InfinityChestSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            STORED_CHANNEL_CODEC.listOf().optionalFieldOf("channels", List.of())
                    .forGetter(InfinityChestSavedData::storedChannels),
            Codec.BOOL.optionalFieldOf("legacy_imported", false)
                    .forGetter(InfinityChestSavedData::legacyImported)
    ).apply(instance, InfinityChestSavedData::new));

    public static final SavedDataType<InfinityChestSavedData> TYPE = new SavedDataType<>(
            Const.rl(NAME), InfinityChestSavedData::new, CODEC);

    private final Map<UUID, Map<UUID, ServerChestHandler>> channels = new HashMap<>();
    private boolean legacyImported;

    public InfinityChestSavedData() {
    }

    private InfinityChestSavedData(List<StoredChannel> storedChannels, boolean legacyImported) {
        this.legacyImported = legacyImported;
        for (StoredChannel stored : storedChannels) {
            channels.computeIfAbsent(stored.owner(), ignored -> new HashMap<>())
                    .putIfAbsent(stored.channel(), attach(new ServerChestHandler(stored.items())));
        }
    }

    public ServerChestHandler getOrCreate(UUID owner, UUID channel) {
        Map<UUID, ServerChestHandler> owned = channels.computeIfAbsent(owner, ignored -> new HashMap<>());
        ServerChestHandler existing = owned.get(channel);
        if (existing != null) {
            return existing;
        }
        ServerChestHandler created = attach(new ServerChestHandler());
        owned.put(channel, created);
        setDirty();
        return created;
    }

    public boolean contains(UUID owner, UUID channel) {
        Map<UUID, ServerChestHandler> owned = channels.get(owner);
        return owned != null && owned.containsKey(channel);
    }

    /** 旧文件只导入到尚不存在的同名通道，避免重启后叠加重复。 */
    public boolean importIfAbsent(UUID owner, UUID channel, Collection<ChestHandler.StoredItem> items) {
        Map<UUID, ServerChestHandler> owned = channels.computeIfAbsent(owner, ignored -> new HashMap<>());
        if (owned.containsKey(channel)) {
            return false;
        }
        owned.put(channel, attach(new ServerChestHandler(items)));
        setDirty();
        return true;
    }

    public boolean legacyImported() {
        return legacyImported;
    }

    public void markLegacyImported() {
        if (!legacyImported) {
            legacyImported = true;
            setDirty();
        }
    }

    public void release() {
        channels.values().forEach(owned -> owned.values().forEach(ServerChestHandler::setRemoved));
    }

    private ServerChestHandler attach(ServerChestHandler handler) {
        handler.attachDirtyCallback(this::setDirty);
        return handler;
    }

    private List<StoredChannel> storedChannels() {
        List<StoredChannel> stored = new ArrayList<>();
        channels.forEach((owner, owned) -> owned.forEach((channel, handler) ->
                stored.add(new StoredChannel(owner, channel, handler.entries()))));
        return stored;
    }

    private record StoredChannel(UUID owner, UUID channel, List<ChestHandler.StoredItem> items) {
        private StoredChannel {
            items = List.copyOf(items == null ? List.of() : items);
        }
    }
}
