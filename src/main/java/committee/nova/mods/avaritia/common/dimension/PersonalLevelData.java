package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

import java.util.Optional;

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
        Optional<Holder<WorldClock>> personalClock = personal.dimensionTypeRegistration().value().defaultClock();
        if (personalClock.isEmpty()) {
            return;
        }
        ServerClockManager clocks = personal.getServer().clockManager();
        Holder<WorldClock> clock = personalClock.get();
        switch (settings.time) {
            case FOLLOW -> overworld.dimensionTypeRegistration().value().defaultClock().ifPresent(overworldClock -> {
                long overworldTicks = clocks.getTotalTicks(overworldClock);
                long personalTicks = clocks.getTotalTicks(clock);
                boolean drifted = Math.abs(overworldTicks - personalTicks) > 2L;
                if (appliedTime != InfinityRingSettings.TimeMode.FOLLOW || drifted || appliedPaused) {
                    applyClockState(clocks, clock, settings.time, overworldTicks, false, 1.0F, true);
                }
            });
            case DAY -> applyClockState(clocks, clock, settings.time, aligned(clocks.getTotalTicks(clock), DAY_TIME), true, 1.0F, true);
            case NIGHT -> applyClockState(clocks, clock, settings.time, aligned(clocks.getTotalTicks(clock), NIGHT_TIME), true, 1.0F, true);
            case CYCLE -> applyClockState(clocks, clock, settings.time, clocks.getTotalTicks(clock), false, 1.0F, false);
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

    private void applyClockState(ServerClockManager clocks, Holder<WorldClock> clock,
                                 InfinityRingSettings.TimeMode mode, long ticks, boolean paused, float rate,
                                 boolean setTicks) {
        boolean same = appliedTime == mode && appliedPaused == paused && appliedRate == rate
                && (!setTicks || appliedTicks == ticks);
        if (same) {
            return;
        }
        clocks.setPaused(clock, paused);
        clocks.setRate(clock, rate);
        if (setTicks) {
            clocks.setTotalTicks(clock, ticks);
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
