package committee.nova.mods.avaritia.common.item.misc;

/**
 * Shared in-day time values for the Infinity Clock GUI and server packet handling.
 */
public final class InfinityClockTimes {
    private static final long DAY_TICKS = 24000L;

    public static final int SUNRISE = 0;
    public static final int DAY = 6000;
    public static final int SUNSET = 12000;
    public static final int NIGHT = 14000;
    public static final int MIDNIGHT = 18000;
    public static final int LATE_NIGHT = 22000;

    private InfinityClockTimes() {
    }

    public static long resolveSelectedDayTime(long currentTicks, int time) {
        long currentDays = currentTicks / DAY_TICKS;
        return currentDays * DAY_TICKS + time;
    }
}
