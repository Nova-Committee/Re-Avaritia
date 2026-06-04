package com.avaritia.init.data.save;

import com.avaritia.Const;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存无尽时钟加速方块数据。
 */
public class AcceleratedBlocksSavedData extends SavedData {
    public static final String NAME = "avaritia_accelerated_blocks";
    private static final Codec<BlockEntry> BLOCK_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(BlockEntry::pos),
            Codec.INT.fieldOf("multiplier").forGetter(BlockEntry::multiplier)
    ).apply(instance, BlockEntry::new));
    private static final Codec<DimensionEntry> DIMENSION_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionEntry::dimension),
            BLOCK_ENTRY_CODEC.listOf().fieldOf("blocks").forGetter(DimensionEntry::blocks)
    ).apply(instance, DimensionEntry::new));
    public static final Codec<AcceleratedBlocksSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DIMENSION_ENTRY_CODEC.listOf().optionalFieldOf("dimensions", List.of()).forGetter(AcceleratedBlocksSavedData::toEntries)
    ).apply(instance, AcceleratedBlocksSavedData::fromEntries));
    public static final SavedDataType<AcceleratedBlocksSavedData> TYPE = new SavedDataType<>(Const.rl(NAME), AcceleratedBlocksSavedData::new, CODEC);

    private final Map<ResourceKey<Level>, Map<BlockPos, Integer>> acceleratedBlocks = new HashMap<>();

    public AcceleratedBlocksSavedData() {
    }

    private static AcceleratedBlocksSavedData fromEntries(List<DimensionEntry> entries) {
        AcceleratedBlocksSavedData savedData = new AcceleratedBlocksSavedData();
        for (DimensionEntry dimensionEntry : entries) {
            Map<BlockPos, Integer> blocks = new HashMap<>();
            for (BlockEntry blockEntry : dimensionEntry.blocks()) {
                blocks.put(blockEntry.pos().immutable(), blockEntry.multiplier());
            }
            savedData.acceleratedBlocks.put(dimensionEntry.dimension(), blocks);
        }
        return savedData;
    }

    private List<DimensionEntry> toEntries() {
        List<DimensionEntry> entries = new ArrayList<>();
        for (Map.Entry<ResourceKey<Level>, Map<BlockPos, Integer>> dimensionEntry : acceleratedBlocks.entrySet()) {
            List<BlockEntry> blocks = new ArrayList<>();
            for (Map.Entry<BlockPos, Integer> blockEntry : dimensionEntry.getValue().entrySet()) {
                blocks.add(new BlockEntry(blockEntry.getKey(), blockEntry.getValue()));
            }
            entries.add(new DimensionEntry(dimensionEntry.getKey(), blocks));
        }
        return entries;
    }

    public Map<ResourceKey<Level>, Map<BlockPos, Integer>> getAcceleratedBlocks() {
        return acceleratedBlocks;
    }

    public void setAcceleratedBlocks(Map<ResourceKey<Level>, Map<BlockPos, Integer>> blocks) {
        this.acceleratedBlocks.clear();
        this.acceleratedBlocks.putAll(blocks);
        setDirty();
    }

    private record DimensionEntry(ResourceKey<Level> dimension, List<BlockEntry> blocks) {
    }

    private record BlockEntry(BlockPos pos, int multiplier) {
    }
}
