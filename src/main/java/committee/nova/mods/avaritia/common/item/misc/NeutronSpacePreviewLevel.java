package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Isolated, read-only BlockState snapshot of a captured Neutron Ring volume.
 * Neighbor and block-entity lookups never touch the metadata source world.
 */
public final class NeutronSpacePreviewLevel extends Level {
    private static final BlockState AIR = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    private static final LevelEntityGetter<Entity> NO_ENTITIES = new EmptyEntities();

    private final Level metadataSource;
    private final Holder<Biome> tintBiome;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final BlockState[] cells;
    private final Map<BlockPos, BlockEntity> entities = new HashMap<>();
    private final Collection<BlockEntity> blockEntities = Collections.unmodifiableCollection(entities.values());
    private final SnapshotChunkSource chunkSource;
    private final TickRateManager tickRateManager = new TickRateManager();
    private final Scoreboard scoreboard = new Scoreboard();
    private final SnapshotData snapshotData;
    private final net.minecraft.world.level.border.WorldBorder worldBorder = new net.minecraft.world.level.border.WorldBorder();

    public NeutronSpacePreviewLevel(Level metadataSource, BlockPos tintOrigin, NeutronSpacePreview preview) {
        super(
                new SnapshotData(metadataSource.getLevelData()),
                metadataSource.dimension(),
                metadataSource.registryAccess(),
                metadataSource.dimensionTypeRegistration(),
                metadataSource.isClientSide(),
                false,
                0L,
                0);
        this.metadataSource = metadataSource;
        this.snapshotData = (SnapshotData) getLevelData();
        this.tintBiome = metadataSource.getBiome(tintOrigin.immutable());
        this.sizeX = Math.max(1, preview.sizeX());
        this.sizeY = Math.max(1, preview.sizeY());
        this.sizeZ = Math.max(1, preview.sizeZ());
        this.cells = new BlockState[this.sizeX * this.sizeY * this.sizeZ];
        this.chunkSource = new SnapshotChunkSource();
        fillCells(preview);
        bindBlockEntities(preview);
    }

    @Override
    public int getSeaLevel() {
        return metadataSource.getSeaLevel();
    }

    @Override
    public net.minecraft.world.level.border.WorldBorder getWorldBorder() {
        return worldBorder;
    }

    public Collection<BlockEntity> blockEntities() {
        return blockEntities;
    }

    private void fillCells(NeutronSpacePreview preview) {
        int count = Math.min(preview.positions().length, preview.states().length);
        List<BlockState> palette = preview.palette();
        for (int i = 0; i < count; i++) {
            int packed = preview.positions()[i];
            int x = NeutronSpacePreview.unpackX(packed);
            int y = NeutronSpacePreview.unpackY(packed);
            int z = NeutronSpacePreview.unpackZ(packed);
            if (!inside(x, y, z)) {
                continue;
            }
            int index = preview.states()[i];
            if (index < 0 || index >= palette.size()) {
                continue;
            }
            cells[cellIndex(x, y, z)] = palette.get(index);
        }
    }

    private void bindBlockEntities(NeutronSpacePreview preview) {
        Map<Integer, NeutronSpacePreview.BlockEntityData> extras = new HashMap<>();
        for (NeutronSpacePreview.BlockEntityData data : preview.blockEntities()) {
            extras.put(data.packedPos(), data);
        }
        int count = Math.min(preview.positions().length, preview.states().length);
        for (int i = 0; i < count; i++) {
            int packed = preview.positions()[i];
            int x = NeutronSpacePreview.unpackX(packed);
            int y = NeutronSpacePreview.unpackY(packed);
            int z = NeutronSpacePreview.unpackZ(packed);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = getBlockState(pos);
            if (!(state.getBlock() instanceof EntityBlock entityBlock)) {
                continue;
            }
            BlockEntity blockEntity = entityBlock.newBlockEntity(pos, state);
            if (blockEntity == null) {
                continue;
            }
            NeutronSpacePreview.BlockEntityData extra = extras.get(packed);
            if (extra != null && !extra.updateTag().isEmpty()) {
                try {
                    blockEntity.loadWithComponents(TagValueInput.create(
                            ProblemReporter.DISCARDING, registryAccess(), extra.updateTag().copy()));
                } catch (RuntimeException ignored) {
                }
            }
            blockEntity.setLevel(this);
            entities.put(pos.immutable(), blockEntity);
        }
    }

    private boolean inside(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ;
    }

    private boolean inside(BlockPos pos) {
        return inside(pos.getX(), pos.getY(), pos.getZ());
    }

    private int cellIndex(int x, int y, int z) {
        return x + z * sizeX + y * sizeX * sizeZ;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getHeight() {
        return sizeY;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return inside(pos) ? entities.get(pos) : null;
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        BlockEntity blockEntity = getBlockEntity(pos);
        return blockEntity != null ? blockEntity.getModelData() : ModelData.EMPTY;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!inside(pos)) {
            return AIR;
        }
        BlockState state = cells[cellIndex(pos.getX(), pos.getY(), pos.getZ())];
        return state == null ? AIR : state;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos blockPos, int amount) {
        return 15;
    }

    public int foliageTint(ColorResolver color) {
        return color.getColor(tintBiome.value(), 0.0, 0.0);
    }


    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z) {
        return tintBiome;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return tintBiome;
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return false;
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return false;
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        return false;
    }

    @Override
    public void explode(@Nullable Entity source, @Nullable DamageSource damageSource,
                        @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z,
                        float radius, boolean fire, ExplosionInteraction interaction,
                        ParticleOptions smallParticles, ParticleOptions largeParticles,
                        WeightedList<net.minecraft.core.particles.ExplosionParticleInfo> blockParticles,
                        Holder<SoundEvent> sound) {
        throw new UnsupportedOperationException("NeutronSpacePreviewLevel cannot create explosions");
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
    }

    @Override
    public void blockEntityChanged(BlockPos pos) {
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return false;
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public LevelChunk getChunk(int chunkX, int chunkZ) {
        throw new UnsupportedOperationException("NeutronSpacePreviewLevel is a read-only snapshot without chunks");
    }

    @Override
    public @Nullable ChunkAccess getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
        return null;
    }

    @Override
    public ChunkSource getChunkSource() {
        return chunkSource;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        if (x < 0 || z < 0 || x >= sizeX || z >= sizeZ) {
            return getMinY();
        }
        Predicate<BlockState> opaque = heightmapType.isOpaque();
        for (int y = sizeY - 1; y >= 0; y--) {
            BlockState state = cells[cellIndex(x, y, z)];
            if (state != null && opaque.test(state)) {
                return y + 1;
            }
        }
        return getMinY();
    }


    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return metadataSource.enabledFeatures();
    }

    @Override
    public RecipeAccess recipeAccess() {
        return metadataSource.recipeAccess();
    }

    @Override
    public PotionBrewing potionBrewing() {
        return metadataSource.potionBrewing();
    }

    @Override
    public FuelValues fuelValues() {
        return metadataSource.fuelValues();
    }

    @Override
    public ClockManager clockManager() {
        return metadataSource.clockManager();
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes() {
        return metadataSource.environmentAttributes();
    }

    @Override
    public TickRateManager tickRateManager() {
        return tickRateManager;
    }

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public @Nullable Entity getEntity(int id) {
        return null;
    }

    @Override
    public Collection<? extends PartEntity<?>> dragonParts() {
        return List.of();
    }

    @Override
    public void setRespawnData(LevelData.RespawnData respawnData) {
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return snapshotData.getRespawnData();
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return NO_ENTITIES;
    }

    @Override
    public LevelTickAccess<net.minecraft.world.level.block.Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<net.minecraft.world.level.material.Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
    }

    @Override
    public void playSeededSound(@Nullable Entity player, double x, double y, double z, Holder<SoundEvent> sound,
                                SoundSource category, float volume, float pitch, long seed) {
    }

    @Override
    public void playSeededSound(@Nullable Entity player, Entity entity, Holder<SoundEvent> sound, SoundSource category,
                                float volume, float pitch, long seed) {
    }

    @Override
    public String gatherChunkSourceStats() {
        return "NeutronSpacePreview";
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
    }

    @Override
    public void levelEvent(@Nullable Entity source, int type, BlockPos pos, int data) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {
    }

    @Override
    public void close() {
    }

    private final class SnapshotChunkSource extends ChunkSource {
        @Override
        public @Nullable ChunkAccess getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
            return null;
        }

        @Override
        public void tick(BooleanSupplier hasTimeLeft, boolean tickChunks) {
        }

        @Override
        public String gatherStats() {
            return "NeutronSpacePreview";
        }

        @Override
        public int getLoadedChunksCount() {
            return 0;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return LevelLightEngine.EMPTY;
        }

        @Override
        public BlockGetter getLevel() {
            return NeutronSpacePreviewLevel.this;
        }
    }

    private static final class SnapshotData implements WritableLevelData {
        private final LevelData.RespawnData respawn;
        private final long gameTime;
        private final boolean hardcore;
        private final Difficulty difficulty;
        private final boolean difficultyLocked;

        private SnapshotData(LevelData source) {
            this.respawn = source.getRespawnData();
            this.gameTime = source.getGameTime();
            this.hardcore = source.isHardcore();
            this.difficulty = source.getDifficulty();
            this.difficultyLocked = source.isDifficultyLocked();
        }

        @Override
        public LevelData.RespawnData getRespawnData() {
            return respawn;
        }

        @Override
        public long getGameTime() {
            return gameTime;
        }

        @Override
        public boolean isHardcore() {
            return hardcore;
        }

        @Override
        public Difficulty getDifficulty() {
            return difficulty;
        }

        @Override
        public boolean isDifficultyLocked() {
            return difficultyLocked;
        }

        @Override
        public void setSpawn(LevelData.RespawnData respawnData) {
        }
    }

    private static final class EmptyEntities implements LevelEntityGetter<Entity> {
        @Override
        public @Nullable Entity get(int id) {
            return null;
        }

        @Override
        public @Nullable Entity get(UUID uuid) {
            return null;
        }

        @Override
        public Iterable<Entity> getAll() {
            return List.of();
        }

        @Override
        public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AbortableIterationConsumer<U> consumer) {
        }

        @Override
        public void get(AABB boundingBox, Consumer<Entity> consumer) {
        }

        @Override
        public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AABB bounds, AbortableIterationConsumer<U> consumer) {
        }
    }
}
