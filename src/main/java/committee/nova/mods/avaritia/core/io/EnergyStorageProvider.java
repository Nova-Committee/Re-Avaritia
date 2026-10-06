package committee.nova.mods.avaritia.core.io;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
public interface EnergyStorageProvider {
    default EnergyStorage getEnergyHandler(Direction side) { return null; }
    default EnergyStorage getEnergyHandler(ItemStack stack) { return null; }
}
