package nova.committee.avaritia.init.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/17 15:04
 * Version: 1.0
 */
public class CommonConfig {

    public static final ForgeConfigSpec COMMON_SPEC;

    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> common = new ForgeConfigSpec.Builder().configure(Common::new);

        COMMON_SPEC = common.getRight();
        COMMON = common.getLeft();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static class Common {
        public Common(ForgeConfigSpec.Builder builder) {

        }
    }
}
