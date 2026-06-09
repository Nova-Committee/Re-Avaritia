package committee.nova.mods.avaritia.common.wrappers;

import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfinityBucketWrapper extends FluidStacksResourceHandler {
    @NotNull
    private final ItemStack container;

    public InfinityBucketWrapper(@NotNull ItemStack container) {
        super(createStacks(container), Integer.MAX_VALUE);
        this.container = container;
    }

    private static NonNullList<FluidStack> createStacks(ItemStack container) {
        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);
        NonNullList<FluidStack> stacks = NonNullList.createWithCapacity(fluids.size() + 1);
        fluids.stream()
                .filter(fluid -> !fluid.isEmpty())
                .map(FluidStack::copy)
                .forEach(stacks::add);
        stacks.add(FluidStack.EMPTY);
        return stacks;
    }

    @Override
    protected void onContentsChanged(int slot, FluidStack previous) {
        InfinityBucketItem.setFluids(
                this.container,
                this.copyToList()
                        .stream()
                        .filter(fluid -> !fluid.isEmpty())
                        .map(FluidStack::copy)
                        .toList()
        );
    }
}
