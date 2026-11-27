package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;


/**
 * Writable game-time for a personal dimension. {@link DerivedLevelData#setGameTime(long)} is a no-op.
 * Daytime is the isolated {@code avaritia:personal} {@link WorldClock}, applied here.
 */
public final class PersonalLevelData extends DerivedLevelData {
    private static final long DAY_TICKS = 24000L;
    private static final long DAY_TIME = 6000L;
    private static final long NIGHT_TIME = 18000L;

    private long gameTime;
    private InfinityRingSettings.TimeMode appliedTime;
    private long appliedTicks = Long.MIN_VALUE;
    private boolean appliedPaused;
    private float appliedRate = Float.NaN;
    private LevelData.RespawnData respawnData = LevelData.RespawnData.DEFAULT;

    public PersonalLevelData(WorldData worldData, ServerLevelData wrapped) {
        super(worldData, wrapped);
        this.gameTime = wrapped.getGameTime();
    }

    public void apply(InfinityRingSettings settings, ServerLevel overworld, ServerLevel personal) {
        applyClock(personal, overworld, settings);
        applyWeather(personal, overworld, settings);
    }

    public void applyClock(ServerLevel personal, ServerLevel overworld, InfinityRingSettings settings) {
        switch (settings.time) {
            case FOLLOW -> {
                long overworldTicks = overworld.getDayTime();
                boolean drifted = Math.abs(overworldTicks - personal.getDayTime()) > 2L;
                if (appliedTime != InfinityRingSettings.TimeMode.FOLLOW || drifted || appliedPaused) {
                    applyClockState(personal, settings.time, overworldTicks, false, 1.0F, true);
                }
            }
            case DAY -> applyClockState(personal, settings.time, aligned(personal.getDayTime(), DAY_TIME), true, 1.0F, true);
            case NIGHT -> applyClockState(personal, settings.time, aligned(personal.getDayTime(), NIGHT_TIME), true, 1.0F, true);
            case CYCLE -> applyClockState(personal, settings.time, personal.getDayTime(), false, 1.0F, false);
        }
    }

    private void applyWeather(ServerLevel personal, ServerLevel overworld, InfinityRingSettings settings) {
        switch (settings.weather) {
            case FOLLOW -> {
                personal.rainLevel = overworld.rainLevel;
                personal.thunderLevel = overworld.thunderLevel;
            }
            case CLEAR -> {
                personal.rainLevel = 0.0F;
                personal.thunderLevel = 0.0F;
            }
            case RAIN -> {
                personal.rainLevel = 1.0F;
                personal.thunderLevel = 0.0F;
            }
            case THUNDER -> {
                personal.rainLevel = 1.0F;
                personal.thunderLevel = 1.0F;
            }
        }
    }

    private static long aligned(long currentTicks, long timeOfDay) {
        return (currentTicks / DAY_TICKS) * DAY_TICKS + timeOfDay;
    }

    /**
     * 1.21.11 keeps day time on the level. {@code ServerLevel#setDayTimePerTick} ignores a
     * rate of zero, so a paused mode (DAY/NIGHT) cannot be expressed as a rate: the fixed
     * day time is re-asserted on every apply while the mode stays paused.
     */
    private void applyClockState(ServerLevel personal, InfinityRingSettings.TimeMode mode, long ticks,
                                 boolean paused, float rate, boolean setTicks) {
        boolean same = appliedTime == mode && appliedPaused == paused && appliedRate == rate
                && (!setTicks || appliedTicks == ticks);
        if (same && !paused) {
            return;
        }
        if (setTicks) {
            personal.setDayTime(ticks);
        }
        appliedTime = mode;
        appliedTicks = ticks;
        appliedPaused = paused;
        appliedRate = rate;
    }

    @Override
    public long getGameTime() {
        return this.gameTime;
    }

    @Override
    public void setGameTime(long time) {
        this.gameTime = time;
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return this.respawnData;
    }

    @Override
    public void setSpawn(LevelData.RespawnData data) {
        this.respawnData = data;
    }
}
