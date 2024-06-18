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
    public static final ForgeConfigSpec.IntValue subArrowDamage;
    public static final ForgeConfigSpec.IntValue axeChainCount;
    public static final ForgeConfigSpec.IntValue pickAxeBreakRange;
    public static final ForgeConfigSpec.IntValue shovelBreakRange;
    public static final ForgeConfigSpec.IntValue neutronCollectorProductTick;
    public static final ForgeConfigSpec.IntValue singularityTimeRequired;

    //SERVER
    static {
        final var common = new ForgeConfigSpec.Builder();
        common.comment("Avaritia Base Config").push("general");
        isKeepStone = buildBoolean(common, "Is Stone", false, "Does the super mode of endless tools retain stone and soil");
        isMergeMatterCluster = buildBoolean(common, "Is Merge Matter Cluster", false, "Whether to merge matter cluster");
        swordRangeDamage = buildInt(common, "Sword Range Damage", 10000, 100, 100000, "Range damage value of the right key of Infinity sword");
        swordAttackRange = buildInt(common, "Sword Attack Range", 32, 8, 64, "Infinity sword right click attack range");
        isSwordAttackAnimal = buildBoolean(common, "Is Sword Damage Animal", false, "Does the right key range attack of endless sword attack neutral creatures");
        subArrowDamage = buildInt(common, "Sub Arrow Damage", 10000, 100, 100000, "Infinity bow scattering light arrow damage");
        axeChainCount = buildInt(common, "Axe Chain Count", 64, 16, 128, "Chain number of endless axe cutting trees");
        foodTime = buildDouble(common, "Food Time", 1d, 0.1d, 5d, "Food effect time scaling factor");
        pickAxeBreakRange = buildInt(common, "PickAxe Break Range", 8, 2, 32, "The range of Infinity PickAxe can break");
        shovelBreakRange = buildInt(common, "Shovel Break Range", 8, 2, 32, "The range of Infinity Shovel can break");
        neutronCollectorProductTick = buildInt(common, "Neutron Collector Product Tick", 3600, 1200, Integer.MAX_VALUE, "The product tick of NeutronCollector");
        singularityTimeRequired = buildInt(common, "Singularity Time Required", 240, 0, Integer.MAX_VALUE, "Singularity default time required");
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

}
