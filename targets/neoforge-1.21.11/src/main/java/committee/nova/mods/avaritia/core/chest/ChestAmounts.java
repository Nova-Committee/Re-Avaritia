package committee.nova.mods.avaritia.core.chest;

/** 无尽箱 long 数量边界运算。 */
final class ChestAmounts {
    private ChestAmounts() {
    }

    static long insertable(long stored, long requested) {
        return Math.min(requested, Long.MAX_VALUE - stored);
    }

    static long extractable(long stored, long requested) {
        return Math.min(requested, stored);
    }
}
