package committee.nova.mods.avaritia.common.item.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Server-side Neutron Ring structure library. */
public final class NeutronRingSavedData extends SavedData {
    public static final int MAX_SPACES = 32;
    public static final String NAME = "avaritia_neutron_ring";

    private static final Codec<Space> SPACE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Space::id),
            Codec.STRING.fieldOf("name").forGetter(Space::name),
            CompoundTag.CODEC.fieldOf("template").forGetter(Space::template)
    ).apply(instance, (id, name, template) -> new Space(id, name, template, NeutronSpacePreview.meta(template))));

    private static final Codec<Library> LIBRARY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(Library::storageId),
            SPACE_CODEC.listOf().optionalFieldOf("spaces", List.of()).forGetter(Library::spaces)
    ).apply(instance, Library::new));

    public static final Codec<NeutronRingSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LIBRARY_CODEC.listOf().optionalFieldOf("libraries", List.of()).forGetter(NeutronRingSavedData::entries)
    ).apply(instance, NeutronRingSavedData::fromEntries));

    public static final SavedDataType<NeutronRingSavedData> TYPE = new SavedDataType<>(
            Const.rl(NAME), NeutronRingSavedData::new, CODEC);

    private final Map<UUID, List<Space>> libraries = new HashMap<>();

    public record Space(String id, String name, CompoundTag template, NeutronSpacePreview.Meta meta) {
        public Space withName(String newName) {
            return new Space(id, newName, template, meta);
        }
    }

    public record SpaceInfo(String id, String name, int sizeX, int sizeY, int sizeZ, int blocks) {
    }

    public NeutronRingSavedData() {
    }

    private static NeutronRingSavedData fromEntries(List<Library> entries) {
        NeutronRingSavedData data = new NeutronRingSavedData();
        for (Library library : entries) {
            data.libraries.put(library.storageId(), new ArrayList<>(library.spaces()));
        }
        return data;
    }

    private List<Library> entries() {
        List<Library> entries = new ArrayList<>(libraries.size());
        libraries.forEach((id, spaces) -> entries.add(new Library(id, List.copyOf(spaces))));
        return entries;
    }

    public static NeutronRingSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public List<SpaceInfo> list(UUID storageId) {
        List<Space> spaces = libraries.get(storageId);
        if (spaces == null) {
            return List.of();
        }
        return spaces.stream().map(space -> {
            NeutronSpacePreview.Meta meta = space.meta;
            return new SpaceInfo(space.id, space.name, meta.sizeX(), meta.sizeY(), meta.sizeZ(), meta.blocks());
        }).toList();
    }

    public Optional<Space> get(UUID storageId, String id) {
        List<Space> spaces = libraries.get(storageId);
        if (spaces == null) {
            return Optional.empty();
        }
        return spaces.stream().filter(space -> space.id.equals(id)).findFirst();
    }

    public boolean add(UUID storageId, String name, CompoundTag template) {
        List<Space> spaces = libraries.computeIfAbsent(storageId, ignored -> new ArrayList<>());
        if (spaces.size() >= MAX_SPACES) {
            return false;
        }
        spaces.add(new Space(UUID.randomUUID().toString(), name, template, NeutronSpacePreview.meta(template)));
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

    private record Library(UUID storageId, List<Space> spaces) {
    }
}
