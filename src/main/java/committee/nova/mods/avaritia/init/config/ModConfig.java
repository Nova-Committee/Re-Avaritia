package committee.nova.mods.avaritia.init.config;


import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/17 15:04
 * Version: 1.0
 */
public class ModConfig {

    public static final ModConfigSpec COMMON;

    public static final ModConfigSpec.BooleanValue isInfinityLight;

    public static final ModConfigSpec.BooleanValue isMergeMatterCluster;
    public static final ModConfigSpec.IntValue swordRangeDamage;
    public static final ModConfigSpec.IntValue swordAttackRange;
    public static final ModConfigSpec.BooleanValue isSwordAttackAnimal;
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

    public static final ModConfigSpec.IntValue neutronPileEmc;
    public static final ModConfigSpec.IntValue blazeCubeEmc;
    public static final ModConfigSpec.IntValue vanillaTotemEmc;

    public static ModConfigSpec.IntValue CHANNEL_FAST_UPDATE_RATE;
    public static ModConfigSpec.IntValue CHANNEL_FULL_UPDATE_RATE;
    public static ModConfigSpec.IntValue MAX_CHANNELS_PRE_PLAYER;
    public static ModConfigSpec.IntValue MAX_PUBLIC_CHANNELS;

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

    //SERVER
    static {
        final var common = new ModConfigSpec.Builder();
        common.comment("Avaritia Common Config");
        common.push("blocks");
        isInfinityLight = buildBoolean(common, "Is Infinity Light", false, "The blocks near the infinity blocks are the brightest");
        common.pop();
        common.push("tools");
        isMergeMatterCluster = buildBoolean(common, "Is Merge Matter Cluster", true, "Whether to merge matter cluster");
        swordRangeDamage = buildInt(common, "Sword Range Damage", 10000, 100, 100000, "Range damage value of the right key of Infinity sword");
        swordAttackRange = buildInt(common, "Sword Attack Range", 32, 8, 64, "Infinity sword right click attack range");
        isSwordAttackAnimal = buildBoolean(common, "Is Sword Damage Animal", false, "Does the right key range attack of endless sword attack neutral creatures");
        isSwordAttackItemEntity = buildBoolean(common, "config.avaritia.is_sword_attack_item_entity", false, "config.avaritia.is_sword_attack_item_entity.tooltip");
        isSwordAttackProjectile= buildBoolean(common, "config.avaritia.is_sword_attack_projectile", false, "config.avaritia.is_sword_attack_projectile.tooltip");
        isSwordAttackLightning = buildBoolean(common, "Is Sword Cause Lightning", false, "Does the right key spawn lightning with range of attack");
        isSwordAttackEndless = buildBoolean(common, "Is Sword Cause Endless damage", true, "Does the right key cause infinity damage");
        subArrowDamage = buildInt(common, "Sub Arrow Damage", 10000, 100, 100000, "Infinity bow scattering light arrow damage");
        axeChainCount = buildInt(common, "Axe Chain Count", 64, 16, 128, "Chain number of endless axe cutting trees");
        pickAxeBreakRange = buildInt(common, "PickAxe Break Range", 8, 2, 32, "The range of Infinity PickAxe can break");
        shovelBreakRange = buildInt(common, "Shovel Break Range", 8, 2, 32, "The range of Infinity Shovel can break");
        singularityTimeRequired = buildInt(common, "Singularity Time Required", 240, 1, Integer.MAX_VALUE, "Singularity default time required");
        growthSoulFarmland = buildDouble(common, "Growth soul farmland rate", 0.8, 0.0, 1.0, "Growth soul farmland rate");
        bladeSlashDamage = buildInt(common, "BladeSlash Damage", 200, 0, Integer.MAX_VALUE, "Damage of BladeSlash for Crystal Sword");
        bladeSlashRadius = buildInt(common, "BladeSlash Radius", 10, 5, 100, "Radius of BladeSlash for Crystal Sword");
        common.pop();
        common.push("emc");
        neutronPileEmc = buildInt(common, "Neutron Pile Emc", 512, 0, Integer.MAX_VALUE, "Emc of Neutron Pile");
        blazeCubeEmc = buildInt(common, "Blaze Cube Emc", 30568, 0, Integer.MAX_VALUE, "Emc of Blaze Cube");
        vanillaTotemEmc = buildInt(common, "Vanilla Totem Emc", 1000, 0, Integer.MAX_VALUE, "Emc of Totem Of Undying");
        common.pop();
        common.push("channel");
        CHANNEL_FAST_UPDATE_RATE = buildInt(common,  "FastUpdate Rate", 1, 1, 40, "");
        CHANNEL_FULL_UPDATE_RATE = buildInt(common, "FullUpdate Rate", 40, 20, 1200, "");
        MAX_CHANNELS_PRE_PLAYER = buildInt(common, "config.avaritia.max_channels_pre_player", 16, 4, 64,
                "config.avaritia.max_channels_pre_player.tooltip");
        MAX_PUBLIC_CHANNELS = buildInt(common, "config.avaritia.max_public_channels", 128, 32, 1024,
                "config.avaritia.max_public_channels.tooltip");
        common.pop();
        common.push("misc");
        useAdvanceTooltips = buildBoolean(common, "Use Advance Tooltips", false, "For develop");
        immortalItemEntityRange = buildDouble(common, "Endless ItemEntity Range", 1000, 1, 10000, "The range of Endless ItemEntity");
        immortalItemEntitySpeed = buildDouble(common, "Endless ItemEntity Speed", 3, 1, 10,"The speed of Endless ItemEntity");
        infinityElytraFlyingSpeed = buildDouble(common, "Infinity Elytra Flying Speed", 1.5, 1, 10, "Infinity Elytra Flying Speed");
        infinityElytraFlyingRangeDamage = buildDouble(common, "Infinity Elytra Flying Range Damage", 100, 0, 10000, "Infinity Elytra Flying Range Damage");
        InfinityArmorNightVision = buildBoolean(common, "Infinity Open or Off Night Vision", true, "Infinity Armor Night Vision");
        bootSpeedBase = buildDouble(common, "config.avaritia.boot_speed_base", 0.1, 0.01, 1.0, "config.avaritia.boot_speed_base.tooltip");
        bootSpeedFlyingMultiplier = buildDouble(common, "config.avaritia.boot_speed_flying_multiplier", 1.1, 0.1, 5.0, "config.avaritia.boot_speed_flying_multiplier.tooltip");
        bootSpeedSwimmingMultiplier = buildDouble(common, "config.avaritia.boot_speed_swimming_multiplier", 1.2, 0.1, 5.0, "config.avaritia.boot_speed_swimming_multiplier.tooltip");
        bootSpeedSneakingMultiplier = buildDouble(common, "config.avaritia.boot_speed_sneaking_multiplier", 0.1, 0.01, 1.0, "config.avaritia.boot_speed_sneaking_multiplier");
        bootSpeedBackwardMultiplier = buildDouble(common, "config.avaritia.boot_speed_backward_multiplier", 0.25, 0.01, 1.0, "config.avaritia.boot_speed_backward_multiplier.tooltip");
        bootSpeedStrafingMultiplier = buildDouble(common, "config.avaritia.boot_speed_strafing_multiplier", 0.45, 0.01, 1.0, "config.avaritia.boot_speed_strafing_multiplier");
        bootSpeedSprintingMultiplier = buildDouble(common, "config.avaritia.boot_speed_sprinting_multiplier", 0.2, 0.01, 1.0, "config.avaritia.boot_speed_sprinting_multiplier");
        common.pop();
        COMMON = common.build();
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfig.COMMON);
    }


    private static ModConfigSpec.BooleanValue buildBoolean(ModConfigSpec.Builder builder, String name, boolean defaultValue, String comment) {
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

    private static ModConfigSpec.IntValue buildInt(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment) {
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.DoubleValue buildDouble(ModConfigSpec.Builder builder, String name, double defaultValue, double min, double max, String comment) {
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ModConfigSpec.LongValue buildLong(ModConfigSpec.Builder builder, String name, long defaultValue, long min, long max, String comment) {
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    public static void save() {
        COMMON.save();
    }
}
