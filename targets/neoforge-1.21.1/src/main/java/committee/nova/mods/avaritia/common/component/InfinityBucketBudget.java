package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Conservative encoded-size budget for Infinity Bucket persistence and packets.
 * Sizes are native NBT/component encodings, not string lengths.
 */
public final class InfinityBucketBudget {
    public static final int MAX_FLUID_ENTRIES = 256;
    public static final int MAX_CREATURE_ENTRIES = 64;
    public static final int MAX_CREATURE_BYTES = 32 * 1024;
    public static final int MAX_TOTAL_BYTES = 256 * 1024;

    private InfinityBucketBudget() {
    }

    public static int encodedFluidSize(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return 0;
        }
        return FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, fluid)
                .result()
                .map(InfinityBucketBudget::tagSize)
                .orElse(0);
    }

    public static int encodedFluidsSize(List<FluidStack> fluids) {
        int total = 0;
        for (FluidStack fluid : fluids) {
            total += encodedFluidSize(fluid);
            if (total > MAX_TOTAL_BYTES) {
                return total;
            }
        }
        return total;
    }

    public static int encodedCreatureSize(InfinityBucketCreature creature) {
        if (creature == null) {
            return 0;
        }
        CompoundTag encoded = new CompoundTag();
        encoded.putString("id", creature.typeId().toString());
        encoded.put("data", creature.entityData().copy());
        return encoded.sizeInBytes();
    }
    public static int encodedCreaturesSize(List<InfinityBucketCreature> creatures) {
        int total = 0;
        for (InfinityBucketCreature creature : creatures) {
            total += encodedCreatureSize(creature);
            if (total > MAX_TOTAL_BYTES) {
                return total;
            }
        }
        return total;
    }

    public static boolean canStore(List<FluidStack> fluids, List<InfinityBucketCreature> creatures) {
        if (fluids.size() > MAX_FLUID_ENTRIES || creatures.size() > MAX_CREATURE_ENTRIES) {
            return false;
        }
        int total = 0;
        for (FluidStack fluid : fluids) {
            total += encodedFluidSize(fluid);
            if (total > MAX_TOTAL_BYTES) {
                return false;
            }
        }
        for (InfinityBucketCreature creature : creatures) {
            int creatureSize = encodedCreatureSize(creature);
            if (creatureSize > MAX_CREATURE_BYTES) {
                return false;
            }
            total += creatureSize;
            if (total > MAX_TOTAL_BYTES) {
                return false;
            }
        }
        return true;
    }

    public static DataResult<InfinityBucketFluids> validateFluids(InfinityBucketFluids fluids) {
        if (fluids.size() > MAX_FLUID_ENTRIES) {
            return DataResult.error(() -> "Infinity Bucket fluid list exceeds " + MAX_FLUID_ENTRIES + " entries");
        }
        int size = encodedFluidsSize(fluids.fluids());
        if (size > MAX_TOTAL_BYTES) {
            return DataResult.error(() -> "Infinity Bucket fluid payload exceeds " + MAX_TOTAL_BYTES + " bytes");
        }
        return DataResult.success(fluids);
    }

    public static DataResult<InfinityBucketCreatures> validateCreatures(InfinityBucketCreatures creatures) {
        if (creatures.size() > MAX_CREATURE_ENTRIES) {
            return DataResult.error(() -> "Infinity Bucket creature list exceeds " + MAX_CREATURE_ENTRIES + " entries");
        }
        int total = 0;
        for (InfinityBucketCreature creature : creatures.creatures()) {
            int size = encodedCreatureSize(creature);
            if (size > MAX_CREATURE_BYTES) {
                return DataResult.error(() -> "Infinity Bucket creature payload exceeds " + MAX_CREATURE_BYTES + " bytes");
            }
            total += size;
            if (total > MAX_TOTAL_BYTES) {
                return DataResult.error(() -> "Infinity Bucket creature list exceeds " + MAX_TOTAL_BYTES + " bytes");
            }
        }
        return DataResult.success(creatures);
    }

    private static int tagSize(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            return compoundTag.sizeInBytes();
        }
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("v", tag);
        return wrapper.sizeInBytes();
    }
}
