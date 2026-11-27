package committee.nova.mods.avaritia.common.dimension;

import com.mojang.serialization.Codec;
import committee.nova.mods.avaritia.Const;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

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

    // 26.3 的 SavedData 经 SavedDataType + Codec 持久化；旧 CompoundTag 格式整体
    // 作为一个 Compound 字段编解码，保持与 1.20.1 存档兼容。
    public static final Codec<InfinityRingSavedData> CODEC = CompoundTag.CODEC.xmap(
            InfinityRingSavedData::loadTag, data -> data.saveTag(new CompoundTag()));

    // 26.3 Fabric 的 SavedDataType 为 4 参构造；DataFixTypes 不能为 null，
    // 自定义数据用 SAVED_DATA_COMMAND_STORAGE 占位（同 NeutronRingSavedData）。
    public static final SavedDataType<InfinityRingSavedData> TYPE = new SavedDataType<>(
            Const.rl(NAME), InfinityRingSavedData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    public static InfinityRingSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
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

    public CompoundTag saveTag(CompoundTag tag) {
        ListTag ownerList = new ListTag();
        owners.forEach((owner, settings) -> {
            CompoundTag entry = settings.save();
            entry.put("Owner", new net.minecraft.nbt.IntArrayTag(UUIDUtil.uuidToIntArray(owner)));
            ownerList.add(entry);
        });
        tag.put("Owners", ownerList);
        ListTag returnList = new ListTag();
        returns.forEach((actor, point) -> {
            CompoundTag entry = new CompoundTag();
            entry.put("Actor", new net.minecraft.nbt.IntArrayTag(UUIDUtil.uuidToIntArray(actor)));
            entry.putString("Dim", point.dimension.identifier().toString());
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

    private static InfinityRingSavedData loadTag(CompoundTag tag) {
        InfinityRingSavedData data = new InfinityRingSavedData();
        ListTag ownerList = tag.getListOrEmpty("Owners");
        for (int i = 0; i < ownerList.size(); i++) {
            CompoundTag entry = ownerList.getCompoundOrEmpty(i);
            Tag ownerTag = entry.get("Owner");
            if (ownerTag == null) continue;
            UUID owner = UUIDUtil.readUUID(new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, ownerTag));
            data.owners.put(owner, InfinityRingSettings.load(entry));
        }
        ListTag returnList = tag.getListOrEmpty("Returns");
        for (int i = 0; i < returnList.size(); i++) {
            CompoundTag entry = returnList.getCompoundOrEmpty(i);
            Tag actorTag = entry.get("Actor");
            if (actorTag == null) continue;
            UUID actor = UUIDUtil.readUUID(new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, actorTag));
            String dimRaw = entry.getStringOr("Dim", "");
            Identifier dimId = Identifier.tryParse(dimRaw);
            ResourceKey<Level> dimension = dimId == null
                    ? Level.OVERWORLD
                    : ResourceKey.create(Registries.DIMENSION, dimId);
            data.returns.put(actor, new TravelPoint(
                    dimension,
                    entry.getDoubleOr("X", 0.0),
                    entry.getDoubleOr("Y", 0.0),
                    entry.getDoubleOr("Z", 0.0),
                    entry.getFloatOr("YRot", 0.0f),
                    entry.getFloatOr("XRot", 0.0f)));
        }
        return data;
    }
}
