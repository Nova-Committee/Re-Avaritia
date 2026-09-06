package committee.nova.mods.avaritia.common.dimension;

import com.mojang.serialization.Lifecycle;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.S2CUpdateDimensionsPack;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/** Runtime registration and deletion of per-player Infinity Ring levels. */
public final class DynamicDimensions {
    private static final RegistrationInfo REGISTRATION =
            new RegistrationInfo(Optional.empty(), Lifecycle.stable());

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
                .registryOrThrow(Registries.DIMENSION_TYPE)
                .getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD);
        Holder<net.minecraft.world.level.biome.Biome> biome = server.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(Biomes.PLAINS);
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biome, List.of());
        settings.getLayersInfo().clear();
        settings.getLayersInfo().addAll(layers);
        settings.updateLayers();
        return new LevelStem(type, new FlatLevelSource(settings));
    }

    @SuppressWarnings("deprecation")
    public static ServerLevel getOrCreate(MinecraftServer server, ResourceKey<Level> key, Supplier<LevelStem> stem) {
        Map<ResourceKey<Level>, ServerLevel> levels = server.forgeGetWorldMap();
        ServerLevel existing = levels.get(key);
        if (existing != null) {
            rebind(existing);
            return existing;
        }
        return create(server, levels, key, stem);
    }

    @SuppressWarnings("deprecation")
    private static ServerLevel create(MinecraftServer server, Map<ResourceKey<Level>, ServerLevel> levels,
                                      ResourceKey<Level> key, Supplier<LevelStem> stemFactory) {
        ResourceKey<LevelStem> stemKey = ResourceKey.create(Registries.LEVEL_STEM, key.location());
        LevelStem stem = stemFactory.get();
        Registry<LevelStem> registry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        if (registry instanceof MappedRegistry<LevelStem> mapped && !registry.containsKey(stemKey)) {
            mapped.unfreeze();
            mapped.register(stemKey, stem, REGISTRATION);
        }
        ServerLevel overworld = server.overworld();
        ChunkProgressListener listener = server.progressListenerFactory.create(11);
        Executor executor = server.executor;
        WorldData worldData = server.getWorldData();
        ServerLevel level = new ServerLevel(
                server,
                executor,
                server.storageSource,
                new PersonalLevelData(worldData, worldData.overworldData()),
                key,
                stem,
                listener,
                worldData.isDebugWorld(),
                overworld.getSeed(),
                List.of(),
                true,
                (RandomSequences) null);
        overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(level.getWorldBorder()));
        levels.put(key, level);
        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
        S2CUpdateDimensionsPack.addToAll(server, key);
        Const.LOGGER.info("Created Infinity Ring dimension {}", key.location());
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
    public static boolean remove(MinecraftServer server, ResourceKey<Level> key) {
        Map<ResourceKey<Level>, ServerLevel> levels = server.forgeGetWorldMap();
        ServerLevel level = levels.get(key);
        if (level != null) {
            if (!level.players().isEmpty()) {
                Const.LOGGER.warn("Refusing to delete occupied personal dimension {}", key.location());
                return false;
            }
            NeoForge.EVENT_BUS.post(new LevelEvent.Unload(level));
            level.save(null, true, false);
            try {
                level.getChunkSource().close();
            } catch (IOException e) {
                Const.LOGGER.error("Failed to close personal dimension {}", key.location(), e);
                return false;
            }
            levels.remove(key);
        }
        unregisterStem(server, key);
        S2CUpdateDimensionsPack.removeFromAll(server, key);
        server.markWorldsDirty();
        Path path = server.storageSource.getDimensionPath(key);
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                throw new java.io.UncheckedIOException(e);
                            }
                        });
            }
        } catch (IOException | java.io.UncheckedIOException e) {
            Const.LOGGER.error("Failed to delete personal dimension files {}", path, e);
            return false;
        }
        Const.LOGGER.info("Deleted Infinity Ring dimension {}", key.location());
        return true;
    }


    private static void unregisterStem(MinecraftServer server, ResourceKey<Level> key) {
        ResourceKey<LevelStem> stemKey = ResourceKey.create(Registries.LEVEL_STEM, key.location());
        Registry<LevelStem> registry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        if (!(registry instanceof MappedRegistry<LevelStem> mapped) || !mapped.containsKey(stemKey)) {
            return;
        }
        LevelStem stem = mapped.get(stemKey);
        mapped.unfreeze();
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
        mapped.byLocation.remove(stemKey.location());
        mapped.byKey.remove(stemKey);
        mapped.byValue.remove(stem);
        mapped.registrationInfos.remove(stemKey);
    }
}
