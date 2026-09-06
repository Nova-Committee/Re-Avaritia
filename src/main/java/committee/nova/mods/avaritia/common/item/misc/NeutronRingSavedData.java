package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Server-side Neutron Ring structure library. */
public final class NeutronRingSavedData extends SavedData {
    public static final int MAX_SPACES = 32;
    private static final String NAME = "avaritia_neutron_ring";
    private final Map<UUID, List<Space>> libraries = new HashMap<>();

    public record Space(String id, String name, CompoundTag template, NeutronSpacePreview preview) {
        public Space withName(String newName) {
            return new Space(id, newName, template, preview);
        }
    }

    public record SpaceInfo(String id, String name, NeutronSpacePreview preview) {
    }

    public static NeutronRingSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(NeutronRingSavedData::new, NeutronRingSavedData::load), NAME);
    }

    public List<SpaceInfo> list(UUID storageId) {
        List<Space> spaces = libraries.get(storageId);
        if (spaces == null) {
            return List.of();
        }
        return spaces.stream().map(space -> new SpaceInfo(space.id, space.name, space.preview)).toList();
    }

    public Optional<Space> get(UUID storageId, String id) {
        List<Space> spaces = libraries.get(storageId);
        if (spaces == null) {
            return Optional.empty();
        }
        return spaces.stream().filter(space -> space.id.equals(id)).findFirst();
    }

    public boolean add(UUID storageId, String name, CompoundTag template, NeutronSpacePreview preview) {
        List<Space> spaces = libraries.computeIfAbsent(storageId, ignored -> new ArrayList<>());
        if (spaces.size() >= MAX_SPACES) {
            return false;
        }
        spaces.add(new Space(UUID.randomUUID().toString(), name, template, preview));
        setDirty();
        return true;
    }

    public boolean rename(UUID storageId, String id, String name) {
        List<Space> spaces = libraries.get(storageId);
        if (spaces == null) {
            return false;
        }
        for (int i = 0; i < spaces.size(); i++) {
            if (spaces.get(i).id.equals(id)) {
                spaces.set(i, spaces.get(i).withName(name));
                setDirty();
                return true;
            }
        }
        return false;
    }

    public boolean remove(UUID storageId, String id) {
        List<Space> spaces = libraries.get(storageId);
        if (spaces == null) {
            return false;
        }
        boolean removed = spaces.removeIf(space -> space.id.equals(id));
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public boolean adopt(UUID from, UUID to) {
        if (from.equals(to)) {
            return true;
        }
        List<Space> source = libraries.get(from);
        if (source == null || source.isEmpty()) {
            return true;
        }
        List<Space> dest = libraries.get(to);
        int destSize = dest == null ? 0 : dest.size();
        if (destSize + source.size() > MAX_SPACES) {
            return false;
        }
        if (dest == null) {
            dest = new ArrayList<>();
            libraries.put(to, dest);
        }
        dest.addAll(source);
        libraries.remove(from);
        setDirty();
        return true;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag librariesTag = new ListTag();
        libraries.forEach((storageId, spaces) -> {
            CompoundTag library = new CompoundTag();
            library.put("Id", NbtUtils.createUUID(storageId));
            ListTag spacesTag = new ListTag();
            for (Space space : spaces) {
                CompoundTag entry = new CompoundTag();
                entry.putString("Id", space.id);
                entry.putString("Name", space.name);
                entry.put("Template", space.template);
                entry.put("Preview", space.preview.save());
                spacesTag.add(entry);
            }
            library.put("Spaces", spacesTag);
            librariesTag.add(library);
        });
        tag.put("Libraries", librariesTag);
        return tag;
    }

    private static NeutronRingSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        NeutronRingSavedData data = new NeutronRingSavedData();
        ListTag librariesTag = tag.getList("Libraries", Tag.TAG_COMPOUND);
        for (int i = 0; i < librariesTag.size(); i++) {
            CompoundTag library = librariesTag.getCompound(i);
            UUID storageId = NbtUtils.loadUUID(library.get("Id"));
            List<Space> spaces = new ArrayList<>();
            ListTag spacesTag = library.getList("Spaces", Tag.TAG_COMPOUND);
            for (int j = 0; j < spacesTag.size(); j++) {
                CompoundTag entry = spacesTag.getCompound(j);
                spaces.add(new Space(entry.getString("Id"), entry.getString("Name"),
                        entry.getCompound("Template"), NeutronSpacePreview.load(entry.getCompound("Preview"))));
            }
            data.libraries.put(storageId, spaces);
        }
        return data;
    }
}
