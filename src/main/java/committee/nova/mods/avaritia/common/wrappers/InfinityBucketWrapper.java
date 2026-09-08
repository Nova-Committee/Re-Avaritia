package committee.nova.mods.avaritia.common.wrappers;

import committee.nova.mods.avaritia.common.item.misc.InfinityBucketContents;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InfinityBucketWrapper implements IFluidHandlerItem, ICapabilityProvider {
    @NotNull
    private final ItemStack container;
    private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);

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
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return 0;
        }

        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);
        FluidStack contained = FluidStack.EMPTY;
        for (FluidStack fluid : fluids) {
            if (fluid.isFluidEqual(resource)) {
                contained = fluid;
                break;
            }
        }

        int fillAmount;
        if (contained.isEmpty()) {
            if (fluids.size() >= InfinityBucketContents.MAX_FLUID_ENTRIES) {
                return 0;
            }
            fillAmount = resource.getAmount();
            if (fillAmount <= 0) {
                return 0;
            }
            FluidStack filled = resource.copy();
            filled.setAmount(fillAmount);
            List<FluidStack> proposed = new ArrayList<>(fluids);
            proposed.add(filled);
            if (!InfinityBucketContents.canStore(proposed, InfinityBucketItem.getCreatures(container))) {
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
            List<FluidStack> proposed = new ArrayList<>(fluids.size());
            for (FluidStack fluid : fluids) {
                proposed.add(fluid.isFluidEqual(contained) ? grown : fluid.copy());
            }
            if (!InfinityBucketContents.canStore(proposed, InfinityBucketItem.getCreatures(container))) {
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
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack contained = fluids.get(i);
            if (!resource.isFluidEqual(contained)) {
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
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack selected = InfinityBucketItem.getSelectedFluid(container);
        if (selected.isEmpty()) {
            List<FluidStack> fluids = InfinityBucketItem.getFluids(container);
            selected = fluids.isEmpty() ? FluidStack.EMPTY : fluids.get(0);
        }
        if (selected.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack request = selected.copy();
        request.setAmount(Math.min(selected.getAmount(), maxDrain));
        return drain(request, action);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, holder);
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }
}
