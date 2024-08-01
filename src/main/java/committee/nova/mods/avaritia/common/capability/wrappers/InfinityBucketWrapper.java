package committee.nova.mods.avaritia.common.capability.wrappers;

import committee.nova.mods.avaritia.common.item.resources.InfinityBucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.NotNull;

public class InfinityBucketWrapper extends FluidBucketWrapper {
    public InfinityBucketWrapper(@NotNull ItemStack container) {
        super(container);
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    public int getTankCapacity(int tank) {
        return InfinityBucketItem.getFirstFluid(this.container).getAmount();
    }

    public @NotNull FluidStack getFluid() {
        return InfinityBucketItem.getFirstFluid(this.container);
    }

    protected void setFluid(@NotNull FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            InfinityBucketItem.emptyFirstFluid(this.container);
        } else {
            InfinityBucketItem.fillFluid(this.container, fluidStack.getFluid(), (long)fluidStack.getAmount());
        }
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (this.container.getCount() == 1 && resource.getAmount() > 0) {
            if (action.execute()) {
                this.setFluid(resource);
            }
            return resource.getAmount();
        } else {
            return 0;
        }
    }

    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (this.container.getCount() == 1 && resource.getAmount() > 0) {
            FluidStack fluidStack = this.getFluid();
            if (!fluidStack.isEmpty() && fluidStack.isFluidEqual(resource)) {
                if (action.execute()) {
                    InfinityBucketItem.drainFluid(this.container, (long)resource.getAmount());
                }
                return fluidStack;
            } else {
                return FluidStack.EMPTY;
            }
        } else {
            return FluidStack.EMPTY;
        }
    }

    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (this.container.getCount() == 1 && maxDrain > 0) {
            FluidStack fluidStack = this.getFluid();
            if (!fluidStack.isEmpty()) {
                if (action.execute()) {
                    this.setFluid(FluidStack.EMPTY);
                }
                return fluidStack;
            } else {
                return FluidStack.EMPTY;
            }
        } else {
            return FluidStack.EMPTY;
        }
    }
}
