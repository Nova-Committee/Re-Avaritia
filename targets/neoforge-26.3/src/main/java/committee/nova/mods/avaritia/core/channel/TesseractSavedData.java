package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Overworld SavedData for every persistent Tesseract channel. */
public final class TesseractSavedData extends SavedData {
    public static final String NAME = "tesseract_channels";
    private static final Codec<StoredChannel> STORED_CHANNEL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ChannelInfo.CODEC.fieldOf("reference").forGetter(StoredChannel::reference),
            Channel.DATA_CODEC.fieldOf("data").forGetter(StoredChannel::data)
    ).apply(instance, StoredChannel::new));
    public static final Codec<TesseractSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            STORED_CHANNEL_CODEC.listOf().optionalFieldOf("channels", List.of()).forGetter(TesseractSavedData::storedChannels),
            Codec.BOOL.optionalFieldOf("legacy_imported", false).forGetter(TesseractSavedData::legacyImported)
    ).apply(instance, TesseractSavedData::new));
    public static final SavedDataType<TesseractSavedData> TYPE = new SavedDataType<>(
            Const.rl(NAME), TesseractSavedData::new, CODEC);

    private final Map<UUID, Map<Integer, ServerChannel>> channels = new HashMap<>();
    private boolean legacyImported;

    public TesseractSavedData() {
    }

    private TesseractSavedData(List<StoredChannel> stored, boolean legacyImported) {
        this.legacyImported = legacyImported;
        for (StoredChannel entry : stored) {
            channels.computeIfAbsent(entry.reference().owner(), ignored -> new HashMap<>())
                    .put(entry.reference().id(), attach(new ServerChannel(entry.data())));
        }
    }

    public ServerChannel get(ChannelInfo reference) {
        Map<Integer, ServerChannel> owned = channels.get(reference.owner());
        return owned == null ? NullChannel.INSTANCE : owned.getOrDefault(reference.id(), NullChannel.INSTANCE);
    }

    public Map<Integer, ServerChannel> channels(UUID owner) {
        return channels.getOrDefault(owner, Map.of());
    }

    public ServerChannel create(ChannelInfo reference, String name) {
        return create(reference, new Channel.Data(name, List.of(), List.of(), 0));
    }

    public ServerChannel create(ChannelInfo reference, Channel.Data data) {
        Map<Integer, ServerChannel> owned = channels.computeIfAbsent(reference.owner(), ignored -> new HashMap<>());
        if (owned.containsKey(reference.id())) {
            throw new IllegalArgumentException("Channel already exists: " + reference);
        }
        ServerChannel channel = attach(new ServerChannel(data));
        owned.put(reference.id(), channel);
        setDirty();
        return channel;
    }

    public boolean remove(ChannelInfo reference) {
        Map<Integer, ServerChannel> owned = channels.get(reference.owner());
        if (owned == null) {
            return false;
        }
        ServerChannel removed = owned.remove(reference.id());
        if (removed == null) {
            return false;
        }
        removed.setRemoved();
        if (owned.isEmpty()) {
            channels.remove(reference.owner());
        }
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
        channels.values().forEach(owned -> owned.values().forEach(ServerChannel::setRemoved));
    }

    private ServerChannel attach(ServerChannel channel) {
        channel.attachDirtyCallback(this::setDirty);
        return channel;
    }

    private List<StoredChannel> storedChannels() {
        List<StoredChannel> stored = new ArrayList<>();
        channels.forEach((owner, owned) -> owned.forEach((id, channel) ->
                stored.add(new StoredChannel(new ChannelInfo(owner, id), channel.data()))));
        return stored;
    }

    private record StoredChannel(ChannelInfo reference, Channel.Data data) {
    }
}
