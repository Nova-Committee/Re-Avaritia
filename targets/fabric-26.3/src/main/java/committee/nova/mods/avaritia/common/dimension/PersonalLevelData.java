package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

/**
 * Writable spawn/game-time for a personal dimension. {@link DerivedLevelData} setters are no-ops.
 * Reloaded levels start at the same origin as their platform, never the overworld's spawn.
 *
 * <p>26.3 note: day time moved to the global WorldClock system and weather to the server-wide
 * {@code WeatherData}; neither is per-dimension any more, so the Infinity Ring FOLLOW/CYCLE modes
 * now coincide with vanilla's shared behaviour, while forced DAY/NIGHT and forced weather modes
 * degrade to the shared server state (see {@link #apply}).
 */
public final class PersonalLevelData extends DerivedLevelData {
    private LevelData.RespawnData spawn;
    private long gameTime;

    public PersonalLevelData(WorldData worldData, ServerLevelData wrapped) {
        super(worldData, wrapped);
        this.gameTime = wrapped.getGameTime();
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return this.spawn != null ? this.spawn : super.getRespawnData();
    }

    @Override
    public void setSpawn(LevelData.RespawnData respawnData) {
        this.spawn = respawnData;
    }

    /**
     * Applies Infinity Ring time/weather preferences. Time FOLLOW/CYCLE and every weather mode
     * are already the vanilla-shared behaviour in 26.3 (global clock + global WeatherData);
     * forced DAY/NIGHT would need a dedicated registered WorldClock per dimension and is not
     * wired up yet, so this currently records nothing.
     */
    public void apply(InfinityRingSettings settings, ServerLevel overworld) {
    }

    public void copyClock(ServerLevelData current) {
        this.gameTime = current.getGameTime();
    }

    @Override
    public long getGameTime() {
        return this.gameTime;
    }

    @Override
    public void setGameTime(long time) {
        this.gameTime = time;
    }
}
