package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

/**
 * Writable spawn/time/weather for a personal dimension. {@link DerivedLevelData} setters are no-ops.
 * Reloaded levels start at the same origin as their platform, never the overworld's spawn.
 */
public final class PersonalLevelData extends DerivedLevelData {
    private BlockPos spawn = InfinityRingDimensions.SPAWN_ORIGIN;
    private float spawnAngle;
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

    @Override
    public int getXSpawn() {
        return spawn.getX();
    }

    @Override
    public int getYSpawn() {
        return spawn.getY();
    }

    @Override
    public int getZSpawn() {
        return spawn.getZ();
    }

    @Override
    public float getSpawnAngle() {
        return spawnAngle;
    }

    @Override
    public void setXSpawn(int x) {
        spawn = new BlockPos(x, spawn.getY(), spawn.getZ());
    }

    @Override
    public void setYSpawn(int y) {
        spawn = new BlockPos(spawn.getX(), y, spawn.getZ());
    }

    @Override
    public void setZSpawn(int z) {
        spawn = new BlockPos(spawn.getX(), spawn.getY(), z);
    }

    @Override
    public void setSpawnAngle(float angle) {
        spawnAngle = angle;
    }

    @Override
    public void setSpawn(BlockPos position, float angle) {
        spawn = position.immutable();
        spawnAngle = angle;
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
