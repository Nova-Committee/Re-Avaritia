package committee.nova.mods.avaritia.api.common.wrapper;
import net.minecraft.world.item.ItemStack;
public interface ModifiableItemHandler extends ItemHandler { void setStackInSlot(int slot, ItemStack stack); }
