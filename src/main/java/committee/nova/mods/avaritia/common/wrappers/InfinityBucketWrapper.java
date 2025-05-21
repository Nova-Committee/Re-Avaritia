package committee.nova.mods.avaritia.common.wrappers;

    import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfinityBucketWrapper implements IFluidHandlerItem {
    @NotNull
    private final ItemStack container;

    public InfinityBucketWrapper(@NotNull ItemStack container) {
        this.container = container;
    }

    @Override
    public int getTanks() {
        return Math.max(InfinityBucketItem.getFluids(container).size(), 1);
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return InfinityBucketItem.getFluids(container).stream()
                .skip(tank)
                .findFirst()
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(@NotNull FluidStack resource, IFluidHandler.@NotNull FluidAction action) {
        if (container.getCount() != 1 && resource.isEmpty()) {
            return 0;
        }

        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);

        FluidStack contained = fluids.stream()
                .filter(fluid -> FluidStack.isSameFluidSameComponents(fluid, resource))
                .findFirst()
                .orElse(FluidStack.EMPTY);

        int fillAmount;
        if (contained.isEmpty()) {
            fillAmount = resource.getAmount();
            FluidStack filled = resource.copy();
            filled.setAmount(fillAmount);
            fluids.addFirst(filled);
        } else {
            fillAmount = Math.min(Integer.MAX_VALUE - contained.getAmount(), resource.getAmount());
            contained.grow(fillAmount);
            fluids.remove(contained);
            fluids.addFirst(contained);
        }

        if (action.execute()) {
            InfinityBucketItem.setFluids(container, fluids);
        }

        return fillAmount;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, IFluidHandler.@NotNull FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack firstContained = InfinityBucketItem.getFluids(container).stream().findFirst().orElse(FluidStack.EMPTY);
        if (!FluidStack.isSameFluidSameComponents(resource, firstContained)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.@NotNull FluidAction action) {
        if (container.getCount() != 1 || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);
        FluidStack firstContained = fluids.stream().findFirst().orElse(FluidStack.EMPTY);
        if (firstContained.isEmpty()) {
            return FluidStack.EMPTY;
        }

        int drainAmount = Math.min(firstContained.getAmount(), maxDrain);
        FluidStack drained = firstContained.copy();
        drained.setAmount(drainAmount);
        firstContained.shrink(drainAmount);
        if (firstContained.isEmpty()) {
            fluids.remove(firstContained);
        }

        if (action.execute()) {
            InfinityBucketItem.setFluids(container, fluids);
        }

        return drained;
    }


    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }
}
