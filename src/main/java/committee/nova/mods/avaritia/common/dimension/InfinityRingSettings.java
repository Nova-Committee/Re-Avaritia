package committee.nova.mods.avaritia.common.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

    private record PlayerRole(UUID id, int role) {
    }

    private static final Codec<PlayerRole> PLAYER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(PlayerRole::id),
            Codec.INT.optionalFieldOf("role", Role.VISITOR.ordinal()).forGetter(PlayerRole::role)
    ).apply(instance, PlayerRole::new));

    public static final Codec<InfinityRingSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("terrain", Terrain.VOID.ordinal()).forGetter(settings -> settings.terrain.ordinal()),
            Codec.INT.optionalFieldOf("time", TimeMode.DAY.ordinal()).forGetter(settings -> settings.time.ordinal()),
            Codec.INT.optionalFieldOf("weather", WeatherMode.CLEAR.ordinal()).forGetter(settings -> settings.weather.ordinal()),
            Codec.INT.optionalFieldOf("access", Access.PRIVATE.ordinal()).forGetter(settings -> settings.access.ordinal()),
            PLAYER_CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(InfinityRingSettings::playerEntries),
            UUIDUtil.CODEC.listOf().optionalFieldOf("banned", List.of()).forGetter(settings -> List.copyOf(settings.banned)),
            UUIDUtil.CODEC.listOf().optionalFieldOf("friends", List.of()).forGetter(settings -> List.of())
    ).apply(instance, InfinityRingSettings::fromCodec));

    public Terrain terrain = Terrain.VOID;
    public TimeMode time = TimeMode.DAY;
    public WeatherMode weather = WeatherMode.CLEAR;
    public Access access = Access.PRIVATE;
    public final Map<UUID, Role> players = new LinkedHashMap<>();
    public final Set<UUID> banned = new LinkedHashSet<>();

    private static InfinityRingSettings fromCodec(int terrain, int time, int weather, int access,
                                                 List<PlayerRole> players, List<UUID> banned, List<UUID> friends) {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.terrain = Terrain.byId(terrain);
        settings.time = TimeMode.byId(time);
        settings.weather = WeatherMode.byId(weather);
        settings.access = Access.byId(access);
        for (PlayerRole player : players) {
            settings.players.put(player.id(), Role.byId(player.role()));
        }
        for (UUID friend : friends) {
            settings.players.putIfAbsent(friend, Role.MEMBER);
        }
        settings.banned.addAll(banned);
        return settings;
    }

    private List<PlayerRole> playerEntries() {
        List<PlayerRole> entries = new ArrayList<>();
        players.forEach((id, role) -> entries.add(new PlayerRole(id, role.ordinal())));
        return entries;
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
