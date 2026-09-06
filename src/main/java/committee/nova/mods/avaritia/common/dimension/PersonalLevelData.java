package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

/**
 * Writable time/weather for a personal dimension. {@link DerivedLevelData} setters are no-ops
 * and would keep the level tied to the overworld clock.
 */
public final class PersonalLevelData extends DerivedLevelData {
    private long gameTime;
    private long dayTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private int clearWeatherTime;
    private boolean followTime;
    private boolean followWeather;
    private boolean daylightCycle;
    private boolean weatherCycle;

    public PersonalLevelData(WorldData worldData, ServerLevelData wrapped) {
        super(worldData, wrapped);
        this.gameTime = wrapped.getGameTime();
        this.dayTime = wrapped.getDayTime();
    }

    public void apply(InfinityRingSettings settings, ServerLevel overworld) {
        this.followTime = settings.time == InfinityRingSettings.TimeMode.FOLLOW;
        this.followWeather = settings.weather == InfinityRingSettings.WeatherMode.FOLLOW;
        this.daylightCycle = settings.time == InfinityRingSettings.TimeMode.CYCLE;
        this.weatherCycle = false;
        if (settings.time == InfinityRingSettings.TimeMode.DAY) {
            this.dayTime = 6000;
        } else if (settings.time == InfinityRingSettings.TimeMode.NIGHT) {
            this.dayTime = 18000;
        }
        if (settings.weather == InfinityRingSettings.WeatherMode.CLEAR) {
            forceWeather(6000, 0, 0, false, false);
        } else if (settings.weather == InfinityRingSettings.WeatherMode.RAIN) {
            forceWeather(0, 6000, 0, true, false);
        } else if (settings.weather == InfinityRingSettings.WeatherMode.THUNDER) {
            forceWeather(0, 0, 6000, true, true);
        }
        if (this.followTime) {
            this.dayTime = overworld.getDayTime();
        }
    }

    public void copyClock(ServerLevelData current) {
        this.gameTime = current.getGameTime();
        this.dayTime = current.getDayTime();
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
    public long getDayTime() {
        return this.followTime ? super.getDayTime() : this.dayTime;
    }

    @Override
    public void setDayTime(long time) {
        if (!this.followTime && this.daylightCycle) {
            this.dayTime = time;
        }
    }

    @Override
    public boolean isRaining() {
        return this.followWeather ? super.isRaining() : this.raining;
    }

    @Override
    public void setRaining(boolean raining) {
        if (!this.followWeather && this.weatherCycle) {
            this.raining = raining;
        }
    }

    @Override
    public int getRainTime() {
        return this.followWeather ? super.getRainTime() : this.rainTime;
    }

    @Override
    public void setRainTime(int time) {
        if (!this.followWeather && this.weatherCycle) {
            this.rainTime = time;
        }
    }

    @Override
    public boolean isThundering() {
        return this.followWeather ? super.isThundering() : this.thundering;
    }

    @Override
    public void setThundering(boolean thundering) {
        if (!this.followWeather && this.weatherCycle) {
            this.thundering = thundering;
        }
    }

    @Override
    public int getThunderTime() {
        return this.followWeather ? super.getThunderTime() : this.thunderTime;
    }

    @Override
    public void setThunderTime(int time) {
        if (!this.followWeather && this.weatherCycle) {
            this.thunderTime = time;
        }
    }

    @Override
    public int getClearWeatherTime() {
        return this.followWeather ? super.getClearWeatherTime() : this.clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int time) {
        if (!this.followWeather && this.weatherCycle) {
            this.clearWeatherTime = time;
        }
    }

    public void forceWeather(int clear, int rain, int thunder, boolean raining, boolean thundering) {
        if (this.followWeather) {
            return;
        }
        this.clearWeatherTime = clear;
        this.rainTime = rain;
        this.thunderTime = thunder;
        this.raining = raining;
        this.thundering = thundering;
    }
}
