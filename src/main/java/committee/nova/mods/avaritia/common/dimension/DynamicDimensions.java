package committee.nova.mods.avaritia.common.dimension;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.S2CUpdateDimensionsPack;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/** Runtime registration and deletion of per-player Infinity Ring levels. */
public final class DynamicDimensions {
    private static final ConcurrentHashMap<ResourceKey<Level>, CompletableFuture<Boolean>> CLOSING = new ConcurrentHashMap<>();

    private DynamicDimensions() {
    }

    public static LevelStem voidStem(MinecraftServer server) {
        return stem(server, List.of());
    }

    public static LevelStem flatStem(MinecraftServer server) {
        return stem(server, List.of(
                new FlatLayerInfo(1, Blocks.BEDROCK),
                new FlatLayerInfo(2, Blocks.DIRT),
                new FlatLayerInfo(1, Blocks.GRASS_BLOCK)));
    }

    public static LevelStem stemFor(MinecraftServer server, InfinityRingSettings.Terrain terrain) {
        return terrain == InfinityRingSettings.Terrain.FLAT ? flatStem(server) : voidStem(server);
    }

    private static LevelStem stem(MinecraftServer server, List<FlatLayerInfo> layers) {
        Holder<net.minecraft.world.level.dimension.DimensionType> type = server.registryAccess()
                .lookupOrThrow(Registries.DIMENSION_TYPE)
                .get(BuiltinDimensionTypes.OVERWORLD).orElseThrow();
        Holder<net.minecraft.world.level.biome.Biome> biome = server.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .get(Biomes.PLAINS).orElseThrow();
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biome, List.of());
        settings.getLayersInfo().clear();
        settings.getLayersInfo().addAll(layers);
        settings.updateLayers();
        return new LevelStem(type, new FlatLevelSource(settings));
    }

    @SuppressWarnings("deprecation")
    public static ServerLevel getOrCreate(MinecraftServer server, ResourceKey<Level> key, Supplier<LevelStem> stem) {
        Map<ResourceKey<Level>, ServerLevel> levels = server.levels;
        ServerLevel existing = levels.get(key);
        if (existing != null) {
            rebind(existing);
            return existing;
        }
        if (isClosing(key)) {
            return null;
        }
        return create(server, levels, key, stem);
    }

    public static boolean isClosing(ResourceKey<Level> key) {
        return CLOSING.containsKey(key);
    }

    public static void clearServerState() {
        CLOSING.values().forEach(future -> future.complete(false));
        CLOSING.clear();
    }

    @SuppressWarnings("deprecation")
    private static ServerLevel create(MinecraftServer server, Map<ResourceKey<Level>, ServerLevel> levels,
                                      ResourceKey<Level> key, Supplier<LevelStem> stemFactory) {
        ResourceKey<LevelStem> stemKey = ResourceKey.create(Registries.LEVEL_STEM, key.identifier());
        LevelStem stem = stemFactory.get();
        Registry<LevelStem> registry = server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM);
        if (registry instanceof MappedRegistry<LevelStem> mapped && !registry.containsKey(stemKey)) {
            boolean frozen = mapped.frozen;
            mapped.frozen = false;
            try {
                // Runtime additions do not pass through MappedRegistry.freeze's value-binding step.
                mapped.register(stemKey, stem, RegistrationInfo.BUILT_IN).bindValue(stem);
            } finally {
                mapped.frozen = frozen;
            }
        } else {
            stem = registry.get(stemKey).map(Holder::value).orElse(stem);
        }
        ServerLevel overworld = server.overworld();
        Executor executor = server.executor;
        WorldData worldData = server.getWorldData();
        ServerLevel level = new ServerLevel(
                server,
                executor,
                server.storageSource,
                new PersonalLevelData(worldData, worldData.overworldData()),
                key,
                stem,
                worldData.isDebugWorld(),
                overworld.getSeed(),
                List.of(),
                true);
        // 26.3 no longer cross-syncs level borders (BorderChangeListener.DelegateBorderChangeListener is gone);
        // personal dimensions keep their own default border, matching vanilla's per-level borders.
        levels.put(key, level);
        S2CUpdateDimensionsPack.addToAll(server, key);
        Const.LOGGER.info("Created Infinity Ring dimension {}", key.identifier());
        return level;
    }

    public static void rebind(ServerLevel level) {
        if (level.getLevelData() instanceof PersonalLevelData) {
            level.tickTime = true;
            return;
        }
        WorldData worldData = level.getServer().getWorldData();
        PersonalLevelData data = new PersonalLevelData(worldData, worldData.overworldData());
        if (level.getLevelData() instanceof ServerLevelData current) {
            data.copyClock(current);
        }
        level.levelData = data;
        level.serverLevelData = data;
        level.tickTime = true;
    }

    @SuppressWarnings("deprecation")
    public static CompletableFuture<Boolean> remove(MinecraftServer server, ResourceKey<Level> key) {
        CompletableFuture<Boolean> started = new CompletableFuture<>();
        CompletableFuture<Boolean> inFlight = CLOSING.putIfAbsent(key, started);
        if (inFlight != null) {
            return inFlight;
        }
        Map<ResourceKey<Level>, ServerLevel> levels = server.levels;
        ServerLevel level = levels.get(key);
        if (level == null) {
            completeRemoval(server, key, started);
            return started;
        }
        if (!level.players().isEmpty()) {
            Const.LOGGER.warn("Refusing to delete occupied personal dimension {}", key.identifier());
            CLOSING.remove(key, started);
            started.complete(false);
            return started;
        }
        level.save(null, true, false);
        level.getPoiManager().tick(() -> true);
        levels.remove(key);
        server.schedule(new TickTask(server.getTickCount(), () -> closeAndFinish(server, key, level, started)));
        return started;
    }

    /**
     * Close after already-queued {@link MinecraftServer} TickTasks. {@code onBlockStateChange}
     * enqueues {@code PoiManager.add} on that mailbox; {@code tell} is FIFO, so those tasks run
     * while the POI {@code IOWorker} is still open. Do not drain unrelated mailbox work.
     */
    private static void closeAndFinish(MinecraftServer server, ResourceKey<Level> key, ServerLevel level,
                                       CompletableFuture<Boolean> started) {
        try {
            level.close();
        } catch (IOException e) {
            Const.LOGGER.error("Failed to close personal dimension {}", key.identifier(), e);
            CLOSING.remove(key, started);
            started.complete(false);
            return;
        }
        completeRemoval(server, key, started);
    }

    private static void completeRemoval(MinecraftServer server, ResourceKey<Level> key,
                                        CompletableFuture<Boolean> started) {
        unregisterStem(server, key);
        S2CUpdateDimensionsPack.removeFromAll(server, key);
        Path path = server.storageSource.getDimensionPath(key);
        try {
            if (Files.exists(path)) {
                try (var files = Files.walk(path)) {
                    files.sorted(Comparator.reverseOrder()).forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            throw new java.io.UncheckedIOException(e);
                        }
                    });
                }
            }
        } catch (IOException | java.io.UncheckedIOException e) {
            Const.LOGGER.error("Failed to delete personal dimension files {}", path, e);
            CLOSING.remove(key, started);
            started.complete(false);
            return;
        }
        Const.LOGGER.info("Deleted Infinity Ring dimension {}", key.identifier());
        CLOSING.remove(key, started);
        started.complete(true);
    }

    private static void unregisterStem(MinecraftServer server, ResourceKey<Level> key) {
        ResourceKey<LevelStem> stemKey = ResourceKey.create(Registries.LEVEL_STEM, key.identifier());
        Registry<LevelStem> registry = server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM);
        if (!(registry instanceof MappedRegistry<LevelStem> mapped) || !mapped.containsKey(stemKey)) {
            return;
        }
        LevelStem stem = mapped.get(stemKey).map(Holder::value).orElse(null);
        if (stem == null) {
            return;
        }
        int id = mapped.getId(stem);
        int last = mapped.byId.size() - 1;
        if (id >= 0 && last >= 0) {
            if (id != last) {
                Holder.Reference<LevelStem> lastHolder = mapped.byId.get(last);
                mapped.byId.set(id, lastHolder);
                if (lastHolder != null) {
                    mapped.toId.put(lastHolder.value(), id);
                }
            }
            mapped.byId.remove(last);
        }
        mapped.toId.removeInt(stem);
        mapped.byLocation.remove(stemKey.identifier());
        mapped.byKey.remove(stemKey);
        mapped.byValue.remove(stem);
        mapped.registrationInfos.remove(stemKey);
    }
}
