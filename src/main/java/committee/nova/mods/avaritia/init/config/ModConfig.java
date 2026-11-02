package committee.nova.mods.avaritia.init.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config values used by the migrated config screen.
 */
public class ModConfig {
    private static final String TRANSLATION_PREFIX = "config.avaritia.";

    public static final ModConfigSpec COMMON;

    public static final ModConfigSpec.BooleanValue isMergeMatterCluster;
    public static final ModConfigSpec.IntValue swordRangeDamage;
    public static final ModConfigSpec.IntValue swordAttackRange;
    public static final ModConfigSpec.BooleanValue isSwordAttackItemEntity;
    public static final ModConfigSpec.BooleanValue isSwordAttackProjectile;
    public static final ModConfigSpec.BooleanValue isSwordAttackLightning;
    public static final ModConfigSpec.BooleanValue isSwordAttackEndless;
    public static final ModConfigSpec.IntValue subArrowDamage;
    public static final ModConfigSpec.IntValue axeChainCount;
    public static final ModConfigSpec.IntValue pickAxeBreakRange;
    public static final ModConfigSpec.IntValue shovelBreakRange;
    public static final ModConfigSpec.IntValue singularityTimeRequired;
    public static final ModConfigSpec.DoubleValue growthSoulFarmland;
    public static final ModConfigSpec.IntValue bladeSlashDamage;
    public static final ModConfigSpec.IntValue bladeSlashRadius;
    public static final ModConfigSpec.IntValue endestPearlAbsorptionLimit;

    public static final ModConfigSpec.IntValue neutronPileEmc;
    public static final ModConfigSpec.IntValue blazeCubeEmc;
    public static final ModConfigSpec.IntValue vanillaTotemEmc;
    public static final ModConfigSpec.BooleanValue enableProjectESingularityCountBoost;

    public static final ModConfigSpec.IntValue CHANNEL_FAST_UPDATE_RATE;
    public static final ModConfigSpec.IntValue CHANNEL_FULL_UPDATE_RATE;
    public static final ModConfigSpec.IntValue MAX_CHANNELS_PRE_PLAYER;
    public static final ModConfigSpec.IntValue MAX_PUBLIC_CHANNELS;

    public static final ModConfigSpec.BooleanValue useAdvanceTooltips;
    public static final ModConfigSpec.DoubleValue immortalItemEntityRange;
    public static final ModConfigSpec.DoubleValue immortalItemEntitySpeed;
    public static final ModConfigSpec.DoubleValue infinityElytraFlyingSpeed;
    public static final ModConfigSpec.DoubleValue infinityElytraFlyingRangeDamage;
    public static final ModConfigSpec.BooleanValue InfinityArmorNightVision;
    public static final ModConfigSpec.DoubleValue bootSpeedBase;
    public static final ModConfigSpec.DoubleValue bootSpeedFlyingMultiplier;
    public static final ModConfigSpec.DoubleValue bootSpeedSwimmingMultiplier;
    public static final ModConfigSpec.DoubleValue bootSpeedSneakingMultiplier;
    public static final ModConfigSpec.DoubleValue bootSpeedBackwardMultiplier;
    public static final ModConfigSpec.DoubleValue bootSpeedStrafingMultiplier;
    public static final ModConfigSpec.DoubleValue bootSpeedSprintingMultiplier;

    public static final ModConfigSpec.BooleanValue neutronWolfArmorEnabled;
    public static final ModConfigSpec.DoubleValue neutronWolfBondRadius;
    public static final ModConfigSpec.DoubleValue neutronWolfMaxRange;
    public static final ModConfigSpec.DoubleValue neutronWolfMinReduction;
    public static final ModConfigSpec.DoubleValue neutronWolfHaxReduction;
    public static final ModConfigSpec.IntValue neutronWolfTotemDurabilityCost;
    public static final ModConfigSpec.IntValue neutronWolfBondCooldownSeconds;
    public static final ModConfigSpec.DoubleValue neutronWolfHealthBonus;

    public static final ModConfigSpec.DoubleValue neutronHarnessShieldRadius;
    public static final ModConfigSpec.IntValue neutronHarnessShieldDurationSeconds;
    public static final ModConfigSpec.IntValue neutronHarnessShieldCooldownSeconds;
    public static final ModConfigSpec.DoubleValue neutronHarnessGhastDamageReduction;
    public static final ModConfigSpec.DoubleValue neutronNautilusWaterDamageReduction;
    public static final ModConfigSpec.DoubleValue neutronNautilusSpeedBonus;

    static {
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        common.push("tools");
        isMergeMatterCluster = buildBoolean(common, "is_merge_matter_cluster", true, "Whether to merge matter clusters");
        swordRangeDamage = buildInt(common, "sword_range_damage", 10000, 100, 100000, "Range damage dealt by the Infinity Sword's right-click attack");
        swordAttackRange = buildInt(common, "sword_attack_range", 32, 8, 64, "Range of the Infinity Sword's right-click attack");
        isSwordAttackItemEntity = buildBoolean(common, "is_sword_attack_item_entity", false, "Whether kill mode destroys item entities");
        isSwordAttackProjectile = buildBoolean(common, "is_sword_attack_projectile", false, "Whether kill mode destroys projectiles");
        isSwordAttackLightning = buildBoolean(common, "is_sword_attack_lightning", false, "Whether the right-click attack summons lightning within its range");
        isSwordAttackEndless = buildBoolean(common, "is_sword_attack_endless", true, "Whether the right-click attack deals infinity damage");
        subArrowDamage = buildInt(common, "sub_arrow_damage", 10000, 100, 100000, "Damage dealt by the Infinity Bow and Crossbow arrow rain");
        axeChainCount = buildInt(common, "axe_chain_count", 64, 16, 128, "Number of logs chained by the Infinity Axe");
        pickAxeBreakRange = buildInt(common, "pickaxe_break_range", 8, 2, 32, "Block-breaking range of the Infinity Pickaxe");
        shovelBreakRange = buildInt(common, "shovel_break_range", 8, 2, 32, "Block-breaking range of the Infinity Shovel");
        singularityTimeRequired = buildInt(common, "singularity_time_required", 240, 0, Integer.MAX_VALUE, "Default time required to produce a singularity");
        growthSoulFarmland = buildDouble(common, "growth_soul_farmland", 0.8, 0.0, 1.0, "Growth rate of Soul Farmland");
        bladeSlashDamage = buildInt(common, "blade_slash_damage", 200, 0, Integer.MAX_VALUE, "Damage dealt by the Crystal Sword's blade slash");
        bladeSlashRadius = buildInt(common, "blade_slash_radius", 10, 5, 100, "Radius of the Crystal Sword's blade slash");
        endestPearlAbsorptionLimit = buildInt(common, "endest_pearl_absorption_limit", 256, 16, 4096, "Matter units absorbed before the Endest Pearl black hole evaporates");
        common.pop();

        common.push("emc");
        neutronPileEmc = buildInt(common, "neutron_pile_emc", 512, 0, Integer.MAX_VALUE, "EMC value of a Neutron Pile");
        blazeCubeEmc = buildInt(common, "blaze_cube_emc", 30568, 0, Integer.MAX_VALUE, "EMC value of a Blaze Cube");
        vanillaTotemEmc = buildInt(common, "vanilla_totem_emc", 1000, 0, Integer.MAX_VALUE, "EMC value of a Totem of Undying");
        enableProjectESingularityCountBoost = buildBoolean(common, "enable_projecte_singularity_count_boost", true,
                "Whether ProjectE or Equivox raises singularity recipe counts to at least 10000 items");
        common.pop();

        common.push("channel");
        CHANNEL_FAST_UPDATE_RATE = buildInt(common, "channel_fast_update_rate", 1, 1, 40, "Fast update interval for channels");
        CHANNEL_FULL_UPDATE_RATE = buildInt(common, "channel_full_update_rate", 40, 20, 1200, "Full update interval for channels");
        MAX_CHANNELS_PRE_PLAYER = buildInt(common, "max_channels_pre_player", 16, 4, 64, "Maximum number of channels per player");
        MAX_PUBLIC_CHANNELS = buildInt(common, "max_public_channels", 128, 32, 1024, "Maximum number of public channels");
        common.pop();

        common.push("misc");
        useAdvanceTooltips = buildBoolean(common, "use_advance_tooltips", false, "Enables advanced tooltips for development purposes");
        immortalItemEntityRange = buildDouble(common, "endless_item_entity_range", 1000, 1, 10000, "Tracking range of immortal item entities");
        immortalItemEntitySpeed = buildDouble(common, "endless_item_entity_speed", 3, 1, 50, "Tracking speed of immortal item entities");
        infinityElytraFlyingSpeed = buildDouble(common, "infinity_elytra_flying_speed", 1.5, 0, 100, "Flight speed of the Infinity Elytra");
        infinityElytraFlyingRangeDamage = buildDouble(common, "infinity_elytra_flying_damage_range", 100, 0, 10000, "Damage radius of Infinity Elytra flight collisions");
        InfinityArmorNightVision = buildBoolean(common, "infinity_helmet_night_vision", true, "Whether the Infinity Helmet grants night vision");
        bootSpeedBase = buildDouble(common, "boot_speed_base", 0.1, 0.01, 1.0, "Base movement speed of the Infinity Boots");
        bootSpeedFlyingMultiplier = buildDouble(common, "boot_speed_flying_multiplier", 1.1, 0.1, 5.0, "Infinity Boots speed multiplier while flying");
        bootSpeedSwimmingMultiplier = buildDouble(common, "boot_speed_swimming_multiplier", 1.2, 0.1, 5.0, "Infinity Boots speed multiplier while swimming");
        bootSpeedSneakingMultiplier = buildDouble(common, "boot_speed_sneaking_multiplier", 0.1, 0.01, 1.0, "Infinity Boots speed multiplier while sneaking");
        bootSpeedBackwardMultiplier = buildDouble(common, "boot_speed_backward_multiplier", 0.25, 0.01, 1.0, "Infinity Boots speed multiplier while moving backward");
        bootSpeedStrafingMultiplier = buildDouble(common, "boot_speed_strafing_multiplier", 0.45, 0.01, 1.0, "Infinity Boots speed multiplier while strafing");
        bootSpeedSprintingMultiplier = buildDouble(common, "boot_speed_sprinting_multiplier", 0.2, 0.01, 1.0, "Additional Infinity Boots speed while sprinting");
        neutronWolfArmorEnabled = buildBoolean(common, "wolf_enabled", true, "Whether the Neutron Wolf Armor grants its bond and totem mechanics");
        neutronWolfBondRadius = buildDouble(common, "wolf_bond_radius", 5.0, 0.0, 64.0, "Radius around its owner in which the wolf is unharmed and can absorb a lethal hit");
        neutronWolfMaxRange = buildDouble(common, "wolf_max_range", 25.0, 1.0, 128.0, "Distance at which damage reduction stops decaying");
        neutronWolfMinReduction = buildDouble(common, "wolf_min_reduction", 0.5, 0.0, 1.0, "Damage reduction kept at and beyond wolf_max_range");
        neutronWolfHaxReduction = buildDouble(common, "wolf_hax_reduction", 0.3, 0.0, 1.0, "Damage reduction cap against armor-bypassing or invulnerability-bypassing damage");
        neutronWolfTotemDurabilityCost = buildInt(common, "wolf_totem_durability_cost", 500, 0, 100000, "Durability the wolf armor must pay to refuse a lethal hit (0 disables the cost)");
        neutronWolfBondCooldownSeconds = buildInt(common, "wolf_bond_cooldown_seconds", 3, 0, 3600, "Cooldown of the bond that absorbs a lethal hit aimed at the owner");
        neutronWolfHealthBonus = buildDouble(common, "wolf_health_bonus", 100.0, 0.0, 1024.0, "Extra max health granted to a wolf wearing the armor");
        neutronHarnessShieldRadius = buildDouble(common, "harness_shield_radius", 4.0, 1.0, 32.0, "Radius of the harness shield that protects friendly and neutral entities");
        neutronHarnessShieldDurationSeconds = buildInt(common, "harness_shield_duration_seconds", 15, 1, 120, "How long the harness shield lasts");
        neutronHarnessShieldCooldownSeconds = buildInt(common, "harness_shield_cooldown_seconds", 20, 0, 600, "Cooldown of the harness shield");
        neutronHarnessGhastDamageReduction = buildDouble(common, "harness_ghast_damage_reduction", 0.8, 0.0, 1.0, "Damage reduction granted to a happy ghast wearing the neutron harness");
        neutronNautilusWaterDamageReduction = buildDouble(common, "nautilus_water_damage_reduction", 0.99, 0.0, 1.0, "Damage reduction while an armored nautilus is in water");
        neutronNautilusSpeedBonus = buildDouble(common, "nautilus_speed_bonus", 0.5, 0.0, 10.0, "Extra movement speed multiplier granted by the nautilus armor");
        common.pop();
        COMMON = common.build();
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfig.COMMON);
    }

    private static ModConfigSpec.BooleanValue buildBoolean(ModConfigSpec.Builder builder, String path, boolean defaultValue, String comment) {
        return builder.comment(comment).translation(translationKey(path)).define(path, defaultValue);
    }

    private static ModConfigSpec.IntValue buildInt(ModConfigSpec.Builder builder, String path, int defaultValue, int min, int max, String comment) {
        return builder.comment(comment).translation(translationKey(path)).defineInRange(path, defaultValue, min, max);
    }

    private static ModConfigSpec.DoubleValue buildDouble(ModConfigSpec.Builder builder, String path, double defaultValue, double min, double max, String comment) {
        return builder.comment(comment).translation(translationKey(path)).defineInRange(path, defaultValue, min, max);
    }

    private static String translationKey(String path) {
        return TRANSLATION_PREFIX + path;
    }

    public static void save() {
        COMMON.save();
    }
}
