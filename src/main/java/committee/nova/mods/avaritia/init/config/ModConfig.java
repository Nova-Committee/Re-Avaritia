package committee.nova.mods.avaritia.init.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/17 15:04
 * Version: 1.0
 */
public class ModConfig {

    public static final ForgeConfigSpec COMMON;

    public static final ForgeConfigSpec.DoubleValue foodTime; //foodTime
    public static final ForgeConfigSpec.BooleanValue isKeepStone;
    public static final ForgeConfigSpec.BooleanValue isMergeMatterCluster;
    public static final ForgeConfigSpec.IntValue swordRangeDamage;
    public static final ForgeConfigSpec.IntValue swordAttackRange;
    public static final ForgeConfigSpec.BooleanValue isSwordAttackItemEntity;
    public static final ForgeConfigSpec.BooleanValue isSwordAttackLightning;
    public static final ForgeConfigSpec.BooleanValue isSwordAttackEndless;
    public static final ForgeConfigSpec.IntValue subArrowDamage;
    public static final ForgeConfigSpec.IntValue axeChainCount;
    public static final ForgeConfigSpec.IntValue pickAxeBreakRange;
    public static final ForgeConfigSpec.IntValue shovelBreakRange;
    public static final ForgeConfigSpec.IntValue neutronCollectorProductTick;
    public static final ForgeConfigSpec.IntValue singularityTimeRequired;
    public static final ForgeConfigSpec.DoubleValue growthSoulFarmland;
    public static final ForgeConfigSpec.IntValue bladeSlashDamage;
    public static final ForgeConfigSpec.IntValue bladeSlashRadius;
    public static final ForgeConfigSpec.BooleanValue internalInfinityCatalystCraft;
    public static final ForgeConfigSpec.DoubleValue immortalItemEntitySpeed;
    public static final ForgeConfigSpec.DoubleValue immortalItemEntityRange;
    public static final ForgeConfigSpec.DoubleValue infinityElytraFlyingSpeed;
    public static final ForgeConfigSpec.DoubleValue bootSpeedBase;
    public static final ForgeConfigSpec.DoubleValue bootSpeedFlyingMultiplier;
    public static final ForgeConfigSpec.DoubleValue bootSpeedSwimmingMultiplier;
    public static final ForgeConfigSpec.DoubleValue bootSpeedSneakingMultiplier;
    public static final ForgeConfigSpec.DoubleValue bootSpeedBackwardMultiplier;
    public static final ForgeConfigSpec.DoubleValue bootSpeedStrafingMultiplier;
    public static final ForgeConfigSpec.DoubleValue bootSpeedSprintingMultiplier;


    public static final ForgeConfigSpec.IntValue neutronPileEmc;
    public static final ForgeConfigSpec.IntValue vanillaTotemEmc;

    public static final ForgeConfigSpec.IntValue chestMaxItemSize;
    public static final ForgeConfigSpec.BooleanValue useSinglePageMode;
    public static final ForgeConfigSpec.LongValue slotStackLimit;
    public static final ForgeConfigSpec.IntValue maxPageLimit;
    public static final ForgeConfigSpec.IntValue resetMaxPage;
    public static final ForgeConfigSpec.IntValue inventoryRows;

    public static ForgeConfigSpec.IntValue MAX_SIZE_PRE_CHANNEL;
    public static ForgeConfigSpec.IntValue MAX_CHANNELS_PRE_PLAYER;
    public static ForgeConfigSpec.IntValue MAX_PUBLIC_CHANNELS;
    public static ForgeConfigSpec.IntValue CHANNEL_FAST_UPDATE_RATE;
    public static ForgeConfigSpec.IntValue CHANNEL_FULL_UPDATE_RATE;

    public static final ForgeConfigSpec.BooleanValue useAdvanceTooltips;

    //SERVER
    static {
        final var common = new ForgeConfigSpec.Builder();
        common.comment("Avaritia Common Config");
        common.push("tools");
        isKeepStone = buildBoolean(common, "config.avaritia.is_keep_stone", false, "config.avaritia.is_keep_stone.tooltip");
        isMergeMatterCluster = buildBoolean(common, "config.avaritia.is_merge_matter_cluster", true, "config.avaritia.is_merge_matter_cluster.tooltip");
        swordRangeDamage = buildInt(common, "config.avaritia.sword_range_damage", 10000, 100, 100000, "config.avaritia.sword_range_damage.tooltip");
        swordAttackRange = buildInt(common, "config.avaritia.sword_attack_range", 32, 8, 64, "config.avaritia.sword_attack_range.tooltip");
        isSwordAttackItemEntity = buildBoolean(common, "config.avaritia.is_sword_attack_item_entity", false, "config.avaritia.is_sword_attack_item_entity.tooltip");
        isSwordAttackLightning = buildBoolean(common, "config.avaritia.is_sword_attack_lightning", false, "config.avaritia.is_sword_attack_lightning.tooltip");
        isSwordAttackEndless = buildBoolean(common, "config.avaritia.is_sword_attack_endless", true, "config.avaritia.is_sword_attack_endless.tooltip");
        subArrowDamage = buildInt(common, "config.avaritia.sub_arrow_damage", 10000, 100, 100000, "config.avaritia.sub_arrow_damage.tooltip");
        axeChainCount = buildInt(common, "config.avaritia.axe_chain_count", 64, 16, 128, "config.avaritia.axe_chain_count.tooltip");
        foodTime = buildDouble(common, "config.avaritia.food_time", 1d, 0.1d, 5d, "config.avaritia.food_time.tooltip");
        pickAxeBreakRange = buildInt(common, "config.avaritia.pickaxe_break_range", 8, 2, 32, "config.avaritia.pickaxe_break_range.tooltip");
        shovelBreakRange = buildInt(common, "config.avaritia.shovel_break_range", 8, 2, 32, "config.avaritia.shovel_break_range.tooltip");
        neutronCollectorProductTick = buildInt(common, "config.avaritia.neutron_collector_product_tick", 3600, 1200, Integer.MAX_VALUE, "config.avaritia.neutron_collector_product_tick.tooltip");
        singularityTimeRequired = buildInt(common, "config.avaritia.singularity_time_required", 240, 0, Integer.MAX_VALUE, "config.avaritia.singularity_time_required.tooltip");
        growthSoulFarmland = buildDouble(common, "config.avaritia.growth_soul_farmland", 0.8, 0.0, 1.0, "config.avaritia.growth_soul_farmland.tooltip");
        bladeSlashDamage = buildInt(common, "config.avaritia.blade_slash_damage", 200, 0, Integer.MAX_VALUE, "config.avaritia.blade_slash_damage.tooltip");
        bladeSlashRadius = buildInt(common, "config.avaritia.blade_slash_radius", 10, 5, 100, "config.avaritia.blade_slash_radius.tooltip");
        internalInfinityCatalystCraft = buildBoolean(common, "config.avaritia.internal_infinity_catalyst_craft", true, "config.avaritia.internal_infinity_catalyst_craft.tooltip");
        common.pop();
        common.push("emc");
        neutronPileEmc = buildInt(common, "config.avaritia.neutron_pile_emc", 100, 0, Integer.MAX_VALUE, "config.avaritia.neutron_pile_emc.tooltip");
        vanillaTotemEmc = buildInt(common, "config.avaritia.vanilla_totem_emc", 1000, 0, Integer.MAX_VALUE, "config.avaritia.vanilla_totem_emc.tooltip");
        common.pop();
        common.push("storage");
        chestMaxItemSize = buildInt(common, "config.avaritia.chest_max_item_size", 32768, 2048, Integer.MAX_VALUE, "config.avaritia.chest_max_item_size.tooltip");
        useSinglePageMode = buildBoolean(common, "config.avaritia.use_single_page_mode", false, "config.avaritia.use_single_page_mode.tooltip");
        slotStackLimit = buildLong(common, "config.avaritia.slot_stack_limit", 4294967295L, 64L, 4294967295L, "config.avaritia.slot_stack_limit.tooltip");
        maxPageLimit = buildInt(common, "config.avaritia.max_page_limit", 79536431, 2, 79536431, "config.avaritia.max_page_limit.tooltip");
        resetMaxPage = buildInt(common, "config.avaritia.reset_max_page", 1, 1, 79536431, "config.avaritia.reset_max_page.tooltip");
        inventoryRows = buildInt(common, "config.avaritia.inventory_rows", 6, 1, 6, "config.avaritia.inventory_rows.tooltip");
        common.pop();
        common.push("channel");
        MAX_SIZE_PRE_CHANNEL = buildInt(common, "config.avaritia.max_size_pre_channel", 32768, 2048, Integer.MAX_VALUE, "config.avaritia.max_size_pre_channel.tooltip");
        MAX_CHANNELS_PRE_PLAYER = buildInt(common, "config.avaritia.max_channels_pre_player", 16, 4, 64, "config.avaritia.max_channels_pre_player.tooltip");
        MAX_PUBLIC_CHANNELS = buildInt(common, "config.avaritia.max_public_channels", 128, 32, 1024, "config.avaritia.max_public_channels.tooltip");
        CHANNEL_FAST_UPDATE_RATE = buildInt(common, "config.avaritia.channel_fast_update_rate", 1, 1, 40, "config.avaritia.channel_fast_update_rate.tooltip");
        CHANNEL_FULL_UPDATE_RATE = buildInt(common, "config.avaritia.channel_full_update_rate", 40, 20, 1200, "config.avaritia.channel_full_update_rate.tooltip");
        common.pop();
        common.push("misc");
        common.push("immortal item entity");
        immortalItemEntitySpeed = buildDouble(common, "config.avaritia.endless_item_entity_speed", 3, 1, 50, "config.avaritia.endless_item_entity_speed.tooltip");
        immortalItemEntityRange = buildDouble(common, "config.avaritia.endless_item_entity_range", 1000, 1, 10000, "config.avaritia.endless_item_entity_range.tooltip");
        common.pop();
        useAdvanceTooltips = buildBoolean(common, "config.avaritia.use_advance_tooltips", false, "config.avaritia.use_advance_tooltips.tooltip");
        infinityElytraFlyingSpeed = buildDouble(common, "config.avaritia.infinity_elytra_flying_speed", 1.5, 1, 10, "config.avaritia.infinity_elytra_flying_speed.tooltip");
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

    public static void register() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, COMMON);
    }


    private static ForgeConfigSpec.BooleanValue buildBoolean(ForgeConfigSpec.Builder builder, String name, boolean defaultValue, String comment) {
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

    private static ForgeConfigSpec.IntValue buildInt(ForgeConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment) {
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ForgeConfigSpec.DoubleValue buildDouble(ForgeConfigSpec.Builder builder, String name, double defaultValue, double min, double max, String comment) {
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ForgeConfigSpec.LongValue buildLong(ForgeConfigSpec.Builder builder, String name, long defaultValue, long min, long max, String comment) {
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

}
