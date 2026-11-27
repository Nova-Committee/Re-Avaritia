package committee.nova.mods.avaritia.core.io;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
public interface FluidStorageProvider {
    default FluidHandler getFluidHandler(Direction side) { return null; }
    default FluidItemHandler getFluidHandler(ItemStack stack) { return null; }
}
