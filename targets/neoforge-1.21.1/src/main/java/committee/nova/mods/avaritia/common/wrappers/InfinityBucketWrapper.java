package committee.nova.mods.avaritia.common.wrappers;

import committee.nova.mods.avaritia.common.component.InfinityBucketBudget;
import committee.nova.mods.avaritia.common.component.InfinityBucketFluids;
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
        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);
        if (tank < 0 || tank >= fluids.size()) {
            return FluidStack.EMPTY;
        }
        return fluids.get(tank);
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
        if (container.getCount() != 1 || resource.isEmpty()) {
            return 0;
        }

        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);
        FluidStack contained = FluidStack.EMPTY;
        for (FluidStack fluid : fluids) {
            if (FluidStack.isSameFluidSameComponents(fluid, resource)) {
                contained = fluid;
                break;
            }
        }

        int fillAmount;
        if (contained.isEmpty()) {
            if (fluids.size() >= InfinityBucketFluids.MAX_ENTRIES) {
                return 0;
            }
            fillAmount = resource.getAmount();
            if (fillAmount <= 0) {
                return 0;
            }
            FluidStack filled = resource.copy();
            filled.setAmount(fillAmount);
            List<FluidStack> proposed = new java.util.ArrayList<>(fluids);
            proposed.add(filled);
            if (!InfinityBucketBudget.canStore(proposed, InfinityBucketItem.getCreatures(container))) {
                return 0;
            }
            if (action.execute()) {
                if (!InfinityBucketItem.trySetFluids(container, proposed)) {
                    return 0;
                }
            }
        } else {
            fillAmount = Math.min(Integer.MAX_VALUE - contained.getAmount(), resource.getAmount());
            if (fillAmount <= 0) {
                return 0;
            }
            FluidStack grown = contained.copy();
            grown.grow(fillAmount);
            List<FluidStack> proposed = new java.util.ArrayList<>(fluids.size());
            for (FluidStack fluid : fluids) {
                proposed.add(FluidStack.isSameFluidSameComponents(fluid, contained) ? grown : fluid.copy());
            }
            if (!InfinityBucketBudget.canStore(proposed, InfinityBucketItem.getCreatures(container))) {
                return 0;
            }
            if (action.execute()) {
                if (!InfinityBucketItem.trySetFluids(container, proposed)) {
                    return 0;
                }
            }
        }
        return fillAmount;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, IFluidHandler.@NotNull FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack contained = fluids.get(i);
            if (!FluidStack.isSameFluidSameComponents(contained, resource)) {
                continue;
            }
            int drainAmount = Math.min(contained.getAmount(), resource.getAmount());
            if (drainAmount <= 0) {
                return FluidStack.EMPTY;
            }
            FluidStack drained = contained.copy();
            drained.setAmount(drainAmount);
            if (action.execute()) {
                contained.shrink(drainAmount);
                if (contained.isEmpty()) {
                    fluids.remove(i);
                }
                InfinityBucketItem.setFluids(container, fluids);
            }
            return drained;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.@NotNull FluidAction action) {
        if (container.getCount() != 1 || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack selected = InfinityBucketItem.getSelectedFluid(container);
        if (selected.isEmpty()) {
            selected = InfinityBucketItem.getFluids(container).stream().findFirst().orElse(FluidStack.EMPTY);
        }
        if (selected.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack request = selected.copy();
        request.setAmount(Math.min(selected.getAmount(), maxDrain));
        return drain(request, action);
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }
}
