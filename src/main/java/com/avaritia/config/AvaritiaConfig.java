package com.avaritia.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Avaritia 模组配置。
 * 使用 NeoForge {@link ModConfigSpec} 构建，通过 {@link ModContainer#registerConfig(ModConfig.Type, ModConfigSpec)} 注册。
 * <p>
 * 所有配置项及默认值从 Avaritia-1.21 迁移而来。
 */
public class AvaritiaConfig {

    public static final ModConfigSpec COMMON;

    // —— blocks ——
    public static final ModConfigSpec.BooleanValue isInfinityLight;

    // —— tools ——
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

    // —— emc ——
    public static final ModConfigSpec.IntValue neutronPileEmc;
    public static final ModConfigSpec.IntValue blazeCubeEmc;
    public static final ModConfigSpec.IntValue vanillaTotemEmc;

    // —— channel ——
    public static final ModConfigSpec.IntValue CHANNEL_FAST_UPDATE_RATE;
    public static final ModConfigSpec.IntValue CHANNEL_FULL_UPDATE_RATE;

    // —— misc ——
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

    static {
        final var builder = new ModConfigSpec.Builder();
        builder.comment("Avaritia Common Config");

        // —— blocks ——
        builder.push("blocks");
        isInfinityLight = builder
                .comment("The blocks near the infinity blocks are the brightest")
                .define("Is Infinity Light", false);
        builder.pop();

        // —— tools ——
        builder.push("tools");
        isMergeMatterCluster = builder
                .comment("Whether to merge matter cluster")
                .define("Is Merge Matter Cluster", true);
        swordRangeDamage = builder
                .comment("Range damage value of the right key of Infinity sword")
                .defineInRange("Sword Range Damage", 10000, 100, 100000);
        swordAttackRange = builder
                .comment("Infinity sword right click attack range")
                .defineInRange("Sword Attack Range", 32, 8, 64);
        isSwordAttackAnimal = builder
                .comment("Does the right key range attack of endless sword attack neutral creatures")
                .define("Is Sword Damage Animal", false);
        isSwordAttackItemEntity = builder
                .comment("config.avaritia.is_sword_attack_item_entity.tooltip")
                .define("config.avaritia.is_sword_attack_item_entity", false);
        isSwordAttackProjectile = builder
                .comment("config.avaritia.is_sword_attack_projectile.tooltip")
                .define("config.avaritia.is_sword_attack_projectile", false);
        isSwordAttackLightning = builder
                .comment("Does the right key spawn lightning with range of attack")
                .define("Is Sword Cause Lightning", false);
        isSwordAttackEndless = builder
                .comment("Does the right key cause infinity damage")
                .define("Is Sword Cause Endless damage", true);
        subArrowDamage = builder
                .comment("Infinity bow scattering light arrow damage")
                .defineInRange("Sub Arrow Damage", 10000, 100, 100000);
        axeChainCount = builder
                .comment("Chain number of endless axe cutting trees")
                .defineInRange("Axe Chain Count", 64, 16, 128);
        pickAxeBreakRange = builder
                .comment("The range of Infinity PickAxe can break")
                .defineInRange("PickAxe Break Range", 8, 2, 32);
        shovelBreakRange = builder
                .comment("The range of Infinity Shovel can break")
                .defineInRange("Shovel Break Range", 8, 2, 32);
        singularityTimeRequired = builder
                .comment("Singularity default time required")
                .defineInRange("Singularity Time Required", 240, 0, Integer.MAX_VALUE);
        growthSoulFarmland = builder
                .comment("Growth soul farmland rate")
                .defineInRange("Growth soul farmland rate", 0.8, 0.0, 1.0);
        bladeSlashDamage = builder
                .comment("Damage of BladeSlash for Crystal Sword")
                .defineInRange("BladeSlash Damage", 200, 0, Integer.MAX_VALUE);
        bladeSlashRadius = builder
                .comment("Radius of BladeSlash for Crystal Sword")
                .defineInRange("BladeSlash Radius", 10, 5, 100);
        builder.pop();

        // —— emc ——
        builder.push("emc");
        neutronPileEmc = builder
                .comment("Emc of Neutron Pile")
                .defineInRange("Neutron Pile Emc", 512, 0, Integer.MAX_VALUE);
        blazeCubeEmc = builder
                .comment("Emc of Blaze Cube")
                .defineInRange("Blaze Cube Emc", 30568, 0, Integer.MAX_VALUE);
        vanillaTotemEmc = builder
                .comment("Emc of Totem Of Undying")
                .defineInRange("Vanilla Totem Emc", 1000, 0, Integer.MAX_VALUE);
        builder.pop();

        // —— channel ——
        builder.push("channel");
        CHANNEL_FAST_UPDATE_RATE = builder
                .comment("Fast update rate for channels")
                .defineInRange("FastUpdate Rate", 1, 1, 40);
        CHANNEL_FULL_UPDATE_RATE = builder
                .comment("Full update rate for channels")
                .defineInRange("FullUpdate Rate", 40, 20, 1200);
        builder.pop();

        // —— misc ——
        builder.push("misc");
        useAdvanceTooltips = builder
                .comment("For develop")
                .define("Use Advance Tooltips", false);
        immortalItemEntityRange = builder
                .comment("The range of Endless ItemEntity")
                .defineInRange("Endless ItemEntity Range", 1000, 1, 10000);
        immortalItemEntitySpeed = builder
                .comment("The speed of Endless ItemEntity")
                .defineInRange("Endless ItemEntity Speed", 3, 1, 10);
        infinityElytraFlyingSpeed = builder
                .comment("Infinity Elytra Flying Speed")
                .defineInRange("Infinity Elytra Flying Speed", 1.5, 1, 10);
        infinityElytraFlyingRangeDamage = builder
                .comment("Infinity Elytra Flying Range Damage")
                .defineInRange("Infinity Elytra Flying Range Damage", 100, 0, 10000);
        InfinityArmorNightVision = builder
                .comment("Infinity Armor Night Vision")
                .define("Infinity Open or Off Night Vision", true);
        bootSpeedBase = builder
                .comment("config.avaritia.boot_speed_base.tooltip")
                .defineInRange("config.avaritia.boot_speed_base", 0.1, 0.01, 1.0);
        bootSpeedFlyingMultiplier = builder
                .comment("config.avaritia.boot_speed_flying_multiplier.tooltip")
                .defineInRange("config.avaritia.boot_speed_flying_multiplier", 1.1, 0.1, 5.0);
        bootSpeedSwimmingMultiplier = builder
                .comment("config.avaritia.boot_speed_swimming_multiplier.tooltip")
                .defineInRange("config.avaritia.boot_speed_swimming_multiplier", 1.2, 0.1, 5.0);
        bootSpeedSneakingMultiplier = builder
                .comment("config.avaritia.boot_speed_sneaking_multiplier")
                .defineInRange("config.avaritia.boot_speed_sneaking_multiplier", 0.1, 0.01, 1.0);
        bootSpeedBackwardMultiplier = builder
                .comment("config.avaritia.boot_speed_backward_multiplier.tooltip")
                .defineInRange("config.avaritia.boot_speed_backward_multiplier", 0.25, 0.01, 1.0);
        bootSpeedStrafingMultiplier = builder
                .comment("config.avaritia.boot_speed_strafing_multiplier")
                .defineInRange("config.avaritia.boot_speed_strafing_multiplier", 0.45, 0.01, 1.0);
        bootSpeedSprintingMultiplier = builder
                .comment("config.avaritia.boot_speed_sprinting_multiplier")
                .defineInRange("config.avaritia.boot_speed_sprinting_multiplier", 0.2, 0.01, 1.0);
        builder.pop();

        COMMON = builder.build();
    }

    /**
     * 向 {@link ModContainer} 注册公共配置。
     *
     * @param modContainer 模组容器实例
     */
    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON);
    }

    /**
     * 保存当前配置到磁盘。
     */
    public static void save() {
        COMMON.save();
    }
}
