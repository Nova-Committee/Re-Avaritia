package committee.nova.mods.avaritia.init.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/17 15:04
 * Version: 1.0
 */
public class ModConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final Common COMMON;

    public static final Server SERVER;

    static {
        final Pair<Common, ForgeConfigSpec> common = new ForgeConfigSpec.Builder().configure(Common::new);
        final Pair<Server, ForgeConfigSpec> server = new ForgeConfigSpec.Builder().configure(Server::new);

        COMMON_SPEC = common.getRight();
        SERVER_SPEC = server.getRight();
        COMMON = common.getLeft();
        SERVER = server.getLeft();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, COMMON_SPEC);
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, SERVER_SPEC);

    }

    public static class Common {
        public Common(ForgeConfigSpec.Builder builder) {

        }
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

    public static class Server {

        public final ForgeConfigSpec.DoubleValue foodTime; //foodTime
        public final ForgeConfigSpec.BooleanValue isKeepStone;
        public final ForgeConfigSpec.BooleanValue isMergeMatterCluster;
        public final ForgeConfigSpec.IntValue swordRangeDamage;
        public final ForgeConfigSpec.IntValue swordAttackRange;
        public final ForgeConfigSpec.BooleanValue isSwordAttackAnimal;
        public final ForgeConfigSpec.IntValue subArrowDamage;
        public final ForgeConfigSpec.IntValue axeChainCount;
        public final ForgeConfigSpec.IntValue pickAxeBreakRange;
        public final ForgeConfigSpec.IntValue shovelBreakRange;
        public final ForgeConfigSpec.IntValue neutronCollectorProductTick;
        public final ForgeConfigSpec.IntValue singularityTimeRequired;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Endless Base Config").push("general");
            this.isKeepStone = buildBoolean(builder, "Is Stone", false, "Does the super mode of endless tools retain stone and soil");
            this.isMergeMatterCluster = buildBoolean(builder, "Is Merge Matter Cluster", false, "Whether to merge matter cluster");
            this.swordRangeDamage = buildInt(builder, "Sword Range Damage", 10000, 100, 100000, "Range damage value of the right key of Infinity sword");
            this.swordAttackRange = buildInt(builder, "Sword Attack Range", 32, 8, 64, "Infinity sword right click attack range");
            this.isSwordAttackAnimal = buildBoolean(builder, "Is Sword Damage", true, "Does the right key range attack of endless sword attack neutral creatures");
            this.subArrowDamage = buildInt(builder, "Sub Arrow Damage", 10000, 100, 100000, "Infinity bow scattering light arrow damage");
            this.axeChainCount = buildInt(builder, "Axe Chain Count", 64, 16, 128, "Chain number of endless axe cutting trees");
            this.foodTime = buildDouble(builder, "Food Time", 1d, 0.1d, 5d, "Food effect time scaling factor");
            this.pickAxeBreakRange = buildInt(builder, "PickAxe Break Range", 8, 2, 32, "The range of Infinity PickAxe can break");
            this.shovelBreakRange = buildInt(builder, "Shovel Break Range", 8, 2, 32, "The range of Infinity Shovel can break");
            this.neutronCollectorProductTick = buildInt(builder, "Neutron Collector Product Tick", 3600, 1200, Integer.MAX_VALUE, "The product tick of NeutronCollector");
            this.singularityTimeRequired = buildInt(builder, "Singularity Time Required", 240, 0, Integer.MAX_VALUE, "Singularity default time required");

            builder.pop();

        }
    }
}
