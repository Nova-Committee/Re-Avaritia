package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Owner-only personal-dimension settings. Travel returns are stored per actor. */
public final class InfinityRingSettings {
    public enum Terrain {
        VOID, SKY_ISLAND, FLAT;

        public static Terrain byId(int id) {
            Terrain[] values = values();
            return id >= 0 && id < values.length ? values[id] : VOID;
        }
    }

    public enum TimeMode {
        FOLLOW, DAY, NIGHT, CYCLE;

        public static TimeMode byId(int id) {
            TimeMode[] values = values();
            return id >= 0 && id < values.length ? values[id] : FOLLOW;
        }
    }

    public enum WeatherMode {
        FOLLOW, CLEAR, RAIN, THUNDER;

        public static WeatherMode byId(int id) {
            WeatherMode[] values = values();
            return id >= 0 && id < values.length ? values[id] : FOLLOW;
        }
    }

    public enum Access {
        PRIVATE, FRIENDS, PUBLIC;

        public static Access byId(int id) {
            Access[] values = values();
            return id >= 0 && id < values.length ? values[id] : PRIVATE;
        }
    }

    public enum Role {
        VISITOR, MEMBER, ADMIN;

        public static Role byId(int id) {
            Role[] values = values();
            return id >= 0 && id < values.length ? values[id] : VISITOR;
        }
    }

    public Terrain terrain = Terrain.VOID;
    public TimeMode time = TimeMode.DAY;
    public WeatherMode weather = WeatherMode.CLEAR;
    public Access access = Access.PRIVATE;
    public final Map<UUID, Role> players = new LinkedHashMap<>();
    public final Set<UUID> banned = new LinkedHashSet<>();

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Terrain", terrain.ordinal());
        tag.putInt("Time", time.ordinal());
        tag.putInt("Weather", weather.ordinal());
        tag.putInt("Access", access.ordinal());
        ListTag playerList = new ListTag();
        players.forEach((id, role) -> {
            CompoundTag entry = new CompoundTag();
            entry.put("Id", NbtUtils.createUUID(id));
            entry.putInt("Role", role.ordinal());
            playerList.add(entry);
        });
        tag.put("Players", playerList);
        ListTag bannedList = new ListTag();
        for (UUID id : banned) {
            bannedList.add(NbtUtils.createUUID(id));
        }
        tag.put("Banned", bannedList);
        return tag;
    }

    public static InfinityRingSettings load(CompoundTag tag) {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.terrain = Terrain.byId(tag.getInt("Terrain"));
        settings.time = TimeMode.byId(tag.getInt("Time"));
        settings.weather = WeatherMode.byId(tag.getInt("Weather"));
        settings.access = Access.byId(tag.getInt("Access"));
        ListTag playerList = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag entry = playerList.getCompound(i);
            settings.players.put(NbtUtils.loadUUID(entry.get("Id")), Role.byId(entry.getInt("Role")));
        }
        ListTag legacy = tag.getList("Friends", Tag.TAG_INT_ARRAY);
        for (Tag friendTag : legacy) {
            settings.players.putIfAbsent(NbtUtils.loadUUID(friendTag), Role.MEMBER);
        }
        ListTag bannedList = tag.getList("Banned", Tag.TAG_INT_ARRAY);
        for (Tag bannedTag : bannedList) {
            settings.banned.add(NbtUtils.loadUUID(bannedTag));
        }
        return settings;
    }

    public boolean canEnter(UUID actor, UUID owner) {
        if (actor.equals(owner)) {
            return true;
        }
        if (banned.contains(actor)) {
            return false;
        }
        return switch (access) {
            case PUBLIC -> true;
            case FRIENDS -> players.containsKey(actor);
            case PRIVATE -> false;
        };
    }

    public boolean canManage(UUID actor, UUID owner) {
        if (actor.equals(owner)) {
            return true;
        }
        return players.get(actor) == Role.ADMIN;
    }
}
