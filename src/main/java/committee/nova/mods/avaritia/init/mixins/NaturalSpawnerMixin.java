package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.dimension.InfinityRingKeys;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSavedData;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/** Honors the per-owner Infinity Ring "natural spawning" toggle without changing global game rules. */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Inject(method = "spawnForChunk", at = @At("HEAD"), cancellable = true)
    private static void avaritia$blockPersonalNaturalSpawn(ServerLevel level, LevelChunk chunk,
                                                           NaturalSpawner.SpawnState spawnState,
                                                           boolean spawnMonsters, boolean spawnAnimals,
                                                           boolean spawnPassives, CallbackInfo ci) {
        if (avaritia$isNaturalSpawningDisabled(level)) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnMobsForChunkGeneration", at = @At("HEAD"), cancellable = true)
    private static void avaritia$blockPersonalChunkGenerationSpawn(ServerLevelAccessor levelAccessor,
                                                                   Holder<Biome> biome, ChunkPos chunkPos,
                                                                   RandomSource random, CallbackInfo ci) {
        if (avaritia$isNaturalSpawningDisabled(levelAccessor.getLevel())) {
            ci.cancel();
        }
    }

    private static boolean avaritia$isNaturalSpawningDisabled(ServerLevel level) {
        UUID owner = InfinityRingKeys.ownerOf(level.dimension()).orElse(null);
        if (owner == null) {
            return false;
        }
        InfinityRingSettings settings = InfinityRingSavedData.get(level.getServer()).settings(owner);
        return settings != null && !settings.mobSpawning;
    }
}
