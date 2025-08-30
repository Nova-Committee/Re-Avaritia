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
    public static final ForgeConfigSpec.BooleanValue isSwordAttackAnimal;
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
    public static final ForgeConfigSpec.DoubleValue endlessItemEntitySpeed;
    public static final ForgeConfigSpec.DoubleValue endlessItemEntityRange;

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
        isKeepStone = buildBoolean(common, "Is Stone", false, "Does the super mode of endless tools retain stone and soil");
        isMergeMatterCluster = buildBoolean(common, "Is Merge Matter Cluster", true, "Whether to merge matter cluster");
        swordRangeDamage = buildInt(common, "Sword Range Damage", 10000, 100, 100000, "Range damage value of the right key of Infinity sword");
        swordAttackRange = buildInt(common, "Sword Attack Range", 32, 8, 64, "Infinity sword right click attack range");
        isSwordAttackAnimal = buildBoolean(common, "Is Sword Damage Animal", false, "Does the right key range attack of endless sword attack neutral creatures");
        isSwordAttackLightning = buildBoolean(common, "Is Sword Cause Lightning", false, "Does the right key spawn lightning with range of attack");
        isSwordAttackEndless = buildBoolean(common, "Is Sword Cause Endless damage", true, "Does the right key cause infinity damage");
        subArrowDamage = buildInt(common, "Sub Arrow Damage", 10000, 100, 100000, "Infinity bow scattering light arrow damage");
        axeChainCount = buildInt(common, "Axe Chain Count", 64, 16, 128, "Chain number of endless axe cutting trees");
        foodTime = buildDouble(common, "Food Time", 1d, 0.1d, 5d, "Food effect time scaling factor");
        pickAxeBreakRange = buildInt(common, "PickAxe Break Range", 8, 2, 32, "The range of Infinity PickAxe can break");
        shovelBreakRange = buildInt(common, "Shovel Break Range", 8, 2, 32, "The range of Infinity Shovel can break");
        neutronCollectorProductTick = buildInt(common, "Neutron Collector Product Tick", 3600, 1200, Integer.MAX_VALUE, "The product tick of NeutronCollector");
        singularityTimeRequired = buildInt(common, "Singularity Time Required", 240, 0, Integer.MAX_VALUE, "Singularity default time required");
        growthSoulFarmland = buildDouble(common, "Growth soul farmland rate", 0.8, 0.0, 1.0, "Growth soul farmland rate");
        bladeSlashDamage = buildInt(common, "BladeSlash Damage", 200, 0, Integer.MAX_VALUE, "Damage of BladeSlash for Crystal Sword");
        bladeSlashRadius = buildInt(common, "BladeSlash Radius", 10, 5, 100, "Radius of BladeSlash for Crystal Sword");
        internalInfinityCatalystCraft = buildBoolean(common, "Internal InfinityCatalyst Craft", true, "Is InfinityCatalyst Craft use all Singularity");
        common.pop();
        common.push("emc");
        neutronPileEmc = buildInt(common, "Neutron Pile Emc", 100, 0, Integer.MAX_VALUE, "Emc of Neutron Pile");
        vanillaTotemEmc = buildInt(common, "Vanilla Totem Emc", 1000, 0, Integer.MAX_VALUE, "Emc of Totem Of Undying");
        common.pop();
        common.push("storage");
        chestMaxItemSize = buildInt(common, "Chest Max ItemSize", 32768, 2048, Integer.MAX_VALUE, "Define the maximum number of item .types. that can be stored in a Infinity Chest.");
        useSinglePageMode = buildBoolean(common, "Use Single PageMode", false, "Use single page mode");
        slotStackLimit = buildLong(common, "Slot Stack Limit", 4294967295L, 64L, 4294967295L, "Stack size limit of slot");
        maxPageLimit = buildInt(common, "Max Page Limit", 79536431, 2, 79536431, "Maximum page limit");
        resetMaxPage = buildInt(common, "Reset Max Page", 1, 1, 79536431, "*Recovery options* Reset the max page that is 0.");
        inventoryRows =buildInt(common, "Inventory Rows", 6, 1, 6, "Inventory rows for multi page mode");
        common.pop();
        common.push("channel");
        MAX_SIZE_PRE_CHANNEL = buildInt(common, "Channel Size", 32768, 2048, Integer.MAX_VALUE, "");
        MAX_CHANNELS_PRE_PLAYER = buildInt(common, "MaxPlayer Channels", 16, 4, 64, "");
        MAX_PUBLIC_CHANNELS = buildInt(common, "MaxPublic Channels", 128, 32, 1024, "");
        CHANNEL_FAST_UPDATE_RATE = buildInt(common,  "FastUpdate Rate", 1, 1, 40, "");
        CHANNEL_FULL_UPDATE_RATE = buildInt(common, "FullUpdate Rate", 40, 20, 1200, "");
        common.pop();
        common.push("misc");
        useAdvanceTooltips = buildBoolean(common, "Use Advance Tooltips", false, "For develop");
        endlessItemEntitySpeed = buildDouble(common, "Endless Item Speed", 10,1,50, "Tracking Endless Item Speed");
        endlessItemEntityRange = buildDouble(common, "Endless Item Range", 100,1,1000, "Tracking Endless Item Range");
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
