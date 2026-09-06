package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Overworld saved data: owner settings plus per-actor travel returns. */
public final class InfinityRingSavedData extends SavedData {
    private static final String NAME = "avaritia_infinity_ring";
    private final Map<UUID, InfinityRingSettings> owners = new HashMap<>();
    private final Map<UUID, TravelPoint> returns = new HashMap<>();

    public record TravelPoint(ResourceKey<Level> dimension, double x, double y, double z, float yRot, float xRot) {
    }

    public static InfinityRingSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(InfinityRingSavedData::new, InfinityRingSavedData::load), NAME);
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

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag ownerList = new ListTag();
        owners.forEach((owner, settings) -> {
            CompoundTag entry = settings.save();
            entry.put("Owner", NbtUtils.createUUID(owner));
            ownerList.add(entry);
        });
        tag.put("Owners", ownerList);
        ListTag returnList = new ListTag();
        returns.forEach((actor, point) -> {
            CompoundTag entry = new CompoundTag();
            entry.put("Actor", NbtUtils.createUUID(actor));
            entry.putString("Dim", point.dimension.location().toString());
            entry.putDouble("X", point.x);
            entry.putDouble("Y", point.y);
            entry.putDouble("Z", point.z);
            entry.putFloat("YRot", point.yRot);
            entry.putFloat("XRot", point.xRot);
            returnList.add(entry);
        });
        tag.put("Returns", returnList);
        return tag;
    }

    private static InfinityRingSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        InfinityRingSavedData data = new InfinityRingSavedData();
        ListTag ownerList = tag.getList("Owners", Tag.TAG_COMPOUND);
        for (int i = 0; i < ownerList.size(); i++) {
            CompoundTag entry = ownerList.getCompound(i);
            UUID owner = NbtUtils.loadUUID(entry.get("Owner"));
            data.owners.put(owner, InfinityRingSettings.load(entry));
        }
        ListTag returnList = tag.getList("Returns", Tag.TAG_COMPOUND);
        for (int i = 0; i < returnList.size(); i++) {
            CompoundTag entry = returnList.getCompound(i);
            UUID actor = NbtUtils.loadUUID(entry.get("Actor"));
            ResourceLocation dimId = ResourceLocation.tryParse(entry.getString("Dim"));
            ResourceKey<Level> dimension = dimId == null
                    ? Level.OVERWORLD
                    : ResourceKey.create(Registries.DIMENSION, dimId);
            data.returns.put(actor, new TravelPoint(
                    dimension,
                    entry.getDouble("X"),
                    entry.getDouble("Y"),
                    entry.getDouble("Z"),
                    entry.getFloat("YRot"),
                    entry.getFloat("XRot")));
        }
        return data;
    }
}
