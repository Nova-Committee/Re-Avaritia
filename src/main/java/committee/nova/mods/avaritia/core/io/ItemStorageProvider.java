package committee.nova.mods.avaritia.core.io;
import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
public interface ItemStorageProvider {
    default ItemHandler getItemHandler(Direction side) { return null; }
    default ItemHandler getItemHandler(ItemStack stack) { return null; }
}
