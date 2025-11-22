package committee.nova.mods.avaritia.init.data.save;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cnlimiter
 */
public class AcceleratedBlocksSavedData extends SavedData {
    public static final String NAME = "avaritia_accelerated_blocks";
    private final Map<ResourceKey<Level>, Map<BlockPos, Integer>> acceleratedBlocks = new HashMap<>();

    public AcceleratedBlocksSavedData() {
    }

    public AcceleratedBlocksSavedData(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
        ListTag dimensionsList = nbt.getList("Dimensions", Tag.TAG_COMPOUND);
        for (int i = 0; i < dimensionsList.size(); i++) {
            CompoundTag dimensionTag = dimensionsList.getCompound(i);
            ResourceLocation dimensionLocation = ResourceLocation.tryParse(dimensionTag.getString("Dimension"));
            ResourceKey<Level> dimensionKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);

            Map<BlockPos, Integer> blocksMap = new HashMap<>();
            ListTag blocksList = dimensionTag.getList("Blocks", Tag.TAG_COMPOUND);

            for (int j = 0; j < blocksList.size(); j++) {
                CompoundTag blockTag = blocksList.getCompound(j);
                BlockPos pos = BlockPos.of(blockTag.getLong("Pos"));
                int multiplier = blockTag.getInt("Multiplier");
                blocksMap.put(pos, multiplier);
            }

            this.acceleratedBlocks.put(dimensionKey, blocksMap);
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compound, HolderLookup.@NotNull Provider registries) {
        ListTag dimensionsList = new ListTag();

        for (Map.Entry<ResourceKey<Level>, Map<BlockPos, Integer>> dimensionEntry : acceleratedBlocks.entrySet()) {
            CompoundTag dimensionTag = new CompoundTag();
            dimensionTag.putString("Dimension", dimensionEntry.getKey().location().toString());

            ListTag blocksList = new ListTag();
            for (Map.Entry<BlockPos, Integer> blockEntry : dimensionEntry.getValue().entrySet()) {
                CompoundTag blockTag = new CompoundTag();
                blockTag.putLong("Pos", blockEntry.getKey().asLong());
                blockTag.putInt("Multiplier", blockEntry.getValue());
                blocksList.add(blockTag);
            }

            dimensionTag.put("Blocks", blocksList);
            dimensionsList.add(dimensionTag);
        }

        compound.put("Dimensions", dimensionsList);
        return compound;
    }

    public Map<ResourceKey<Level>, Map<BlockPos, Integer>> getAcceleratedBlocks() {
        return acceleratedBlocks;
    }

    public void setAcceleratedBlocks(Map<ResourceKey<Level>, Map<BlockPos, Integer>> blocks) {
        this.acceleratedBlocks.clear();
        this.acceleratedBlocks.putAll(blocks);
        setDirty();
    }
}
