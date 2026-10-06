package committee.nova.mods.avaritia.core.io;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
public final class BucketFluidHandler implements FluidItemHandler {
    private ItemStack container;
    public BucketFluidHandler(ItemStack stack) { container = stack; }
    public ItemStack getContainer() { return container; }
    public int getTanks() { return 1; }
    public FluidStack getFluidInTank(int tank) { return container.getItem() instanceof BucketItem bucket && !(bucket instanceof MobBucketItem) ? new FluidStack(((committee.nova.mods.avaritia.init.mixins.DimensionBucketItemAccessor) bucket).avaritia$getContent(), 1000) : FluidStack.EMPTY; }
    public int getTankCapacity(int tank) { return 1000; }
    public boolean isFluidValid(int tank, FluidStack stack) { return !stack.isEmpty() && stack.getFluid().getBucket() != Items.AIR; }
    public int fill(FluidStack resource, FluidAction action) { if (!container.is(Items.BUCKET) || container.getCount() != 1 || resource.getAmount() < 1000 || !isFluidValid(0, resource)) return 0; if (action.execute()) container = new ItemStack(resource.getFluid().getBucket()); return 1000; }
    public FluidStack drain(FluidStack resource, FluidAction action) { return resource.isFluidEqual(getFluidInTank(0)) ? drain(resource.getAmount(), action) : FluidStack.EMPTY; }
    public FluidStack drain(int amount, FluidAction action) { FluidStack fluid = getFluidInTank(0); if (fluid.isEmpty() || amount < 1000 || container.getCount() != 1) return FluidStack.EMPTY; if (action.execute()) container = new ItemStack(Items.BUCKET); return fluid; }
}
