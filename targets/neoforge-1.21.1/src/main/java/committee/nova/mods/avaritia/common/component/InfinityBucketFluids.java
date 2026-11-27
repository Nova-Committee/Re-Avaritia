package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable stored fluid list. Distinct component patches never collapse together.
 */
public final class InfinityBucketFluids {
    public static final int MAX_ENTRIES = InfinityBucketBudget.MAX_FLUID_ENTRIES;
    public static final InfinityBucketFluids EMPTY = new InfinityBucketFluids(List.of());
    public static final Codec<InfinityBucketFluids> CODEC = FluidStack.CODEC.sizeLimitedListOf(MAX_ENTRIES)
            .xmap(InfinityBucketFluids::new, InfinityBucketFluids::fluids)
            .validate(InfinityBucketBudget::validateFluids);
    public static final StreamCodec<RegistryFriendlyByteBuf, InfinityBucketFluids> STREAM_CODEC = FluidStack.STREAM_CODEC
            .apply(ByteBufCodecs.list(MAX_ENTRIES))
            .map(InfinityBucketFluids::new, InfinityBucketFluids::fluids)
            .map(fluids -> InfinityBucketBudget.validateFluids(fluids).result().orElse(EMPTY), fluids -> fluids);

    private final List<FluidStack> fluids;
    private final int hashCode;

    public InfinityBucketFluids(List<FluidStack> fluids) {
        List<FluidStack> copy = new ArrayList<>(fluids.size());
        for (FluidStack fluid : fluids) {
            if (fluid != null && !fluid.isEmpty()) {
                copy.add(fluid.copy());
            }
        }
        this.fluids = List.copyOf(copy);
        int hash = 1;
        for (FluidStack fluid : this.fluids) {
            hash = 31 * hash + FluidStack.hashFluidAndComponents(fluid);
            hash = 31 * hash + fluid.getAmount();
        }
        this.hashCode = hash;
    }

    public List<FluidStack> fluids() {
        return fluids;
    }

    public List<FluidStack> copyFluids() {
        List<FluidStack> copy = new ArrayList<>(fluids.size());
        for (FluidStack fluid : fluids) {
            copy.add(fluid.copy());
        }
        return copy;
    }

    public boolean isEmpty() {
        return fluids.isEmpty();
    }

    public int size() {
        return fluids.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InfinityBucketFluids other) || fluids.size() != other.fluids.size()) {
            return false;
        }
        for (int i = 0; i < fluids.size(); i++) {
            if (!FluidStack.matches(fluids.get(i), other.fluids.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
