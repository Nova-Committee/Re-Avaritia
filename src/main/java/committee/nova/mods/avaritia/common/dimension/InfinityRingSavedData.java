package committee.nova.mods.avaritia.common.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Overworld saved data: owner settings plus per-actor travel returns. */
public final class InfinityRingSavedData extends SavedData {
    public static final String NAME = "avaritia_infinity_ring";

    private static final Codec<OwnerEntry> OWNER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(OwnerEntry::owner),
            InfinityRingSettings.CODEC.fieldOf("settings").forGetter(OwnerEntry::settings)
    ).apply(instance, OwnerEntry::new));

    private static final Codec<TravelEntry> TRAVEL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("actor").forGetter(TravelEntry::actor),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dim").forGetter(entry -> entry.point().dimension()),
            Codec.DOUBLE.fieldOf("x").forGetter(entry -> entry.point().x()),
            Codec.DOUBLE.fieldOf("y").forGetter(entry -> entry.point().y()),
            Codec.DOUBLE.fieldOf("z").forGetter(entry -> entry.point().z()),
            Codec.FLOAT.fieldOf("y_rot").forGetter(entry -> entry.point().yRot()),
            Codec.FLOAT.fieldOf("x_rot").forGetter(entry -> entry.point().xRot())
    ).apply(instance, (actor, dim, x, y, z, yRot, xRot) ->
            new TravelEntry(actor, new TravelPoint(dim, x, y, z, yRot, xRot))));

    public static final Codec<InfinityRingSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OWNER_CODEC.listOf().optionalFieldOf("owners", List.of()).forGetter(InfinityRingSavedData::entries),
            TRAVEL_CODEC.listOf().optionalFieldOf("returns", List.of()).forGetter(InfinityRingSavedData::travelEntries)
    ).apply(instance, InfinityRingSavedData::fromEntries));

    public static final SavedDataType<InfinityRingSavedData> TYPE = new SavedDataType<>(
            Const.rl(NAME), InfinityRingSavedData::new, CODEC);

    private final Map<UUID, InfinityRingSettings> owners = new HashMap<>();
    private final Map<UUID, TravelPoint> returns = new HashMap<>();

    public record TravelPoint(ResourceKey<Level> dimension, double x, double y, double z, float yRot, float xRot) {
    }

    public InfinityRingSavedData() {
    }

    private static InfinityRingSavedData fromEntries(List<OwnerEntry> entries, List<TravelEntry> travelEntries) {
        InfinityRingSavedData data = new InfinityRingSavedData();
        for (OwnerEntry entry : entries) {
            data.owners.put(entry.owner(), entry.settings());
        }
        for (TravelEntry entry : travelEntries) {
            data.returns.put(entry.actor(), entry.point());
        }
        return data;
    }

    private List<OwnerEntry> entries() {
        List<OwnerEntry> list = new ArrayList<>();
        owners.forEach((owner, settings) -> list.add(new OwnerEntry(owner, settings)));
        return list;
    }

    private List<TravelEntry> travelEntries() {
        List<TravelEntry> list = new ArrayList<>();
        returns.forEach((actor, point) -> list.add(new TravelEntry(actor, point)));
        return list;
    }

    public static InfinityRingSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean hasDimension(UUID owner) {
        return owners.containsKey(owner);
    }

    public InfinityRingSettings settings(UUID owner) {
        return owners.get(owner);
    }

    public InfinityRingSettings create(UUID owner, InfinityRingSettings settings) {
        owners.put(owner, settings);
        setDirty();
        return settings;
    }

    public void removeOwner(UUID owner) {
        owners.remove(owner);
        setDirty();
    }

    public void rememberReturn(UUID actor, TravelPoint point) {
        returns.put(actor, point);
        setDirty();
    }

    public TravelPoint takeReturn(UUID actor) {
        TravelPoint point = returns.remove(actor);
        if (point != null) {
            setDirty();
        }
        return point;
    }

    public TravelPoint peekReturn(UUID actor) {
        return returns.get(actor);
    }

    public void touch() {
        setDirty();
    }

    private record OwnerEntry(UUID owner, InfinityRingSettings settings) {
    }

    private record TravelEntry(UUID actor, TravelPoint point) {
    }
}
