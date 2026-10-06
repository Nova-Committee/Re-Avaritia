package committee.nova.mods.avaritia.init.config;

import com.google.gson.*;
import committee.nova.mods.avaritia.Const;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Validated, project-owned common configuration. Client screen changes persist to the same JSON file. */
public final class ModConfig {
    private static final Map<String, Value<?>> VALUES = new LinkedHashMap<>();
    public static final BooleanValue isMergeMatterCluster = bool("tools", "config.avaritia.is_merge_matter_cluster", true);
    public static final IntValue swordRangeDamage = integer("tools", "config.avaritia.sword_range_damage", 10000, 100, 100000);
    public static final IntValue swordAttackRange = integer("tools", "config.avaritia.sword_attack_range", 32, 8, 64);
    public static final BooleanValue isSwordAttackItemEntity = bool("tools", "config.avaritia.is_sword_attack_item_entity", false);
    public static final BooleanValue isSwordAttackProjectile = bool("tools", "config.avaritia.is_sword_attack_projectile", false);
    public static final BooleanValue isSwordAttackLightning = bool("tools", "config.avaritia.is_sword_attack_lightning", false);
    public static final BooleanValue isSwordAttackEndless = bool("tools", "config.avaritia.is_sword_attack_endless", true);
    public static final IntValue subArrowDamage = integer("tools", "config.avaritia.sub_arrow_damage", 10000, 100, 100000);
    public static final IntValue axeChainCount = integer("tools", "Axe Chain Count", 64, 16, 128);
    public static final IntValue pickAxeBreakRange = integer("tools", "config.avaritia.pickaxe_break_range", 8, 2, 32);
    public static final IntValue shovelBreakRange = integer("tools", "config.avaritia.shovel_break_range", 8, 2, 32);
    public static final IntValue singularityTimeRequired = integer("tools", "config.avaritia.singularity_time_required", 240, 1, Integer.MAX_VALUE);
    public static final DoubleValue growthSoulFarmland = decimal("tools", "config.avaritia.growth_soul_farmland", .8, 0, 1);
    public static final IntValue bladeSlashDamage = integer("tools", "config.avaritia.blade_slash_damage", 200, 0, Integer.MAX_VALUE);
    public static final IntValue bladeSlashRadius = integer("tools", "config.avaritia.blade_slash_radius", 10, 5, 100);
    public static final BooleanValue isSwordAttackExplode = bool("tools", "config.avaritia.is_sword_attack_explode", true);
    public static final BooleanValue enableProjectESingularityCountBoost = bool("emc", "config.avaritia.enable_projecte_singularity_count_boost", true);
    public static final IntValue neutronPileEmc = integer("emc", "config.avaritia.neutron_pile_emc", 512, 0, Integer.MAX_VALUE);
    public static final IntValue blazeCubeEmc = integer("emc", "config.avaritia.blaze_cube_emc", 30568, 0, Integer.MAX_VALUE);
    public static final IntValue vanillaTotemEmc = integer("emc", "config.avaritia.vanilla_totem_emc", 1000, 0, Integer.MAX_VALUE);
    public static final IntValue MAX_CHANNELS_PRE_PLAYER = integer("channel", "config.avaritia.max_channels_pre_player", 16, 4, 64);
    public static final IntValue MAX_PUBLIC_CHANNELS = integer("channel", "config.avaritia.max_public_channels", 128, 32, 1024);
    public static final IntValue CHANNEL_FAST_UPDATE_RATE = integer("channel", "config.avaritia.channel_fast_update_rate", 1, 1, 40);
    public static final IntValue CHANNEL_FULL_UPDATE_RATE = integer("channel", "config.avaritia.channel_full_update_rate", 40, 20, 1200);
    public static final DoubleValue immortalItemEntitySpeed = decimal("misc.immortal item entity", "config.avaritia.endless_item_entity_speed", 3, 1, 50);
    public static final DoubleValue immortalItemEntityRange = decimal("misc.immortal item entity", "config.avaritia.endless_item_entity_range", 1000, 1, 10000);
    public static final BooleanValue useAdvanceTooltips = bool("misc", "config.avaritia.use_advance_tooltips", false);
    public static final DoubleValue infinityElytraFlyingSpeed = decimal("misc", "config.avaritia.infinity_elytra_flying_speed", 1.5, 1, 10);
    public static final DoubleValue infinityElytraFlyingRangeDamage = decimal("misc", "Infinity Elytra Flying Range Damage", 100, 0, 10000);
    public static final BooleanValue InfinityArmorNightVision = bool("misc", "Infinity Open or Off Night Vision", true);
    public static final DoubleValue bootSpeedBase = decimal("misc", "config.avaritia.boot_speed_base", .1, .01, 1);
    public static final DoubleValue bootSpeedFlyingMultiplier = decimal("misc", "config.avaritia.boot_speed_flying_multiplier", 1.1, .1, 5);
    public static final DoubleValue bootSpeedSwimmingMultiplier = decimal("misc", "config.avaritia.boot_speed_swimming_multiplier", 1.2, .1, 5);
    public static final DoubleValue bootSpeedSneakingMultiplier = decimal("misc", "config.avaritia.boot_speed_sneaking_multiplier", .1, .01, 1);
    public static final DoubleValue bootSpeedBackwardMultiplier = decimal("misc", "config.avaritia.boot_speed_backward_multiplier", .25, .01, 1);
    public static final DoubleValue bootSpeedStrafingMultiplier = decimal("misc", "config.avaritia.boot_speed_strafing_multiplier", .45, .01, 1);
    public static final DoubleValue bootSpeedSprintingMultiplier = decimal("misc", "config.avaritia.boot_speed_sprinting_multiplier", .2, .01, 1);

    private ModConfig() {}
    private static <T extends Value<?>> T add(T value) { VALUES.put(value.key, value); return value; }
    private static BooleanValue bool(String group, String name, boolean value) { return add(new BooleanValue(group + "/" + name, value)); }
    private static IntValue integer(String group, String name, int value, int min, int max) { return add(new IntValue(group + "/" + name, value, min, max)); }
    private static DoubleValue decimal(String group, String name, double value, double min, double max) { return add(new DoubleValue(group + "/" + name, value, min, max)); }

    public static synchronized void register() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("avaritia-common.json");
        if (!Files.exists(file)) { save(); return; }
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Value<?> value : VALUES.values()) {
                JsonElement json = root.get(value.key);
                if (json == null) continue;
                try { value.load(json); }
                catch (IllegalArgumentException exception) { Const.LOGGER.warn("Invalid config value {}: retaining default", value.key, exception); }
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Cannot read Avaritia configuration " + file, exception);
        }
    }

    public static synchronized void save() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("avaritia-common.json");
        JsonObject root = new JsonObject();
        VALUES.forEach((key, value) -> root.add(key, Const.GSON.toJsonTree(value.get())));
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(temporary, Const.GSON.toJson(root), StandardCharsets.UTF_8);
            try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException exception) { throw new IllegalStateException("Cannot save Avaritia configuration " + file, exception); }
    }

    public abstract static class Value<T> {
        final String key;
        private volatile T value;
        private final T defaultValue;
        Value(String key, T value) { this.key = key; this.value = value; this.defaultValue = value; }
        public T get() { return value; }
        public T getDefault() { return defaultValue; }
        public void set(T value) { validate(Objects.requireNonNull(value)); this.value = value; }
        protected abstract void validate(T value);
        protected abstract void load(JsonElement json);
        public void save() { ModConfig.save(); }
    }
    public static final class BooleanValue extends Value<Boolean> {
        BooleanValue(String key, boolean value) { super(key, value); }
        protected void validate(Boolean value) {}
        protected void load(JsonElement json) {
            if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException("Expected boolean");
            set(json.getAsBoolean());
        }
    }
    public static final class IntValue extends Value<Integer> {
        private final int min, max;
        IntValue(String key, int value, int min, int max) { super(key, value); this.min = min; this.max = max; }
        protected void validate(Integer value) { if (value < min || value > max) throw new IllegalArgumentException("Out of range " + key); }
        protected void load(JsonElement json) { try { set(new BigDecimal(json.getAsString()).intValueExact()); } catch (RuntimeException ex) { throw new IllegalArgumentException("Expected integer", ex); } }
    }
    public static final class DoubleValue extends Value<Double> {
        private final double min, max;
        DoubleValue(String key, double value, double min, double max) { super(key, value); this.min = min; this.max = max; }
        protected void validate(Double value) { if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException("Out of range " + key); }
        protected void load(JsonElement json) { try { set(json.getAsDouble()); } catch (RuntimeException ex) { throw new IllegalArgumentException("Expected decimal", ex); } }
    }
    public static final class LongValue extends Value<Long> {
        private final long min, max;
        public LongValue(String key, long value, long min, long max) { super(key, value); this.min = min; this.max = max; }
        protected void validate(Long value) { if (value < min || value > max) throw new IllegalArgumentException("Out of range " + key); }
        protected void load(JsonElement json) { try { set(new BigDecimal(json.getAsString()).longValueExact()); } catch (RuntimeException ex) { throw new IllegalArgumentException("Expected integer", ex); } }
    }
}
