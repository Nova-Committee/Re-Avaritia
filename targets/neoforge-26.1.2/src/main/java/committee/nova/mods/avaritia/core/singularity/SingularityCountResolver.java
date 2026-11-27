package committee.nova.mods.avaritia.core.singularity;

/**
 * 计算可选兼容模组影响后的奇点配方需求数量。
 */
public final class SingularityCountResolver {
    private static final int PROJECTE_MINIMUM_COUNT = 10_000;

    private SingularityCountResolver() {
    }

    public static int resolve(int configuredCount, boolean boostEnabled, boolean projectELoaded) {
        return boostEnabled && projectELoaded
                ? Math.max(configuredCount, PROJECTE_MINIMUM_COUNT)
                : configuredCount;
    }
}
