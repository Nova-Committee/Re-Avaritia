package committee.nova.mods.avaritia.common.dimension;

import committee.nova.mods.avaritia.Const;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.Optional;
import java.util.UUID;

/** Resource keys for per-player Infinity Ring dimensions. */
public final class InfinityRingKeys {
    public static final String PATH_PREFIX = "personal_";
    public static final ResourceKey<WorldClock> CLOCK = ResourceKey.create(Registries.WORLD_CLOCK, Const.rl("personal"));
    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, Const.rl("personal"));

    private InfinityRingKeys() {
    }

    public static ResourceKey<Level> levelKey(UUID owner) {
        return ResourceKey.create(Registries.DIMENSION,
                Const.rl(PATH_PREFIX + owner.toString().replace("-", "")));
    }

    public static boolean isPersonal(ResourceKey<Level> key) {
        if (key == null) {
            return false;
        }
        Identifier id = key.identifier();
        return Const.MOD_ID.equals(id.getNamespace()) && id.getPath().startsWith(PATH_PREFIX);
    }

    public static Optional<UUID> ownerOf(ResourceKey<Level> key) {
        if (!isPersonal(key)) {
            return Optional.empty();
        }
        String hex = key.identifier().getPath().substring(PATH_PREFIX.length());
        if (hex.length() != 32) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-"
                    + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
