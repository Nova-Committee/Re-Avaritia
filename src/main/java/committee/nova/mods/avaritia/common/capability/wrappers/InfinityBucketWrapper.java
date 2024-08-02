package committee.nova.mods.avaritia.common.capability.wrappers;

import committee.nova.mods.avaritia.common.item.resources.InfinityBucketItem;
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
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 && resource.isEmpty()) {
            return 0;
        }

        List<FluidStack> fluids = InfinityBucketItem.getFluids(container);

        FluidStack contained = fluids.stream()
                .filter(fluid -> fluid.isFluidEqual(resource))
                .findFirst()
                .orElse(FluidStack.EMPTY);

        int fillAmount;
        if (contained.isEmpty()) {
            fillAmount = resource.getAmount();
            FluidStack filled = resource.copy();
            filled.setAmount(fillAmount);
            fluids.add(0, filled);
        } else {
            fillAmount = Math.min(Integer.MAX_VALUE - contained.getAmount(), resource.getAmount());
            contained.grow(fillAmount);
            fluids.remove(contained);
            fluids.add(0, contained);
        }

        if (action.execute()) {
            InfinityBucketItem.setFluids(container, fluids);
        }

        return fillAmount;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack firstContained = InfinityBucketItem.getFluids(container).stream().findFirst().orElse(FluidStack.EMPTY);
        if (!resource.isFluidEqual(firstContained)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, holder);
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }
}
