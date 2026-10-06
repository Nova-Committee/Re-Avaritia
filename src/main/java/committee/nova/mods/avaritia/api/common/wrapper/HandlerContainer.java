package committee.nova.mods.avaritia.api.common.wrapper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public class HandlerContainer implements Container {
    protected final ItemHandler handler;
    public HandlerContainer(ItemHandler handler) { this.handler = handler; }
    public int getContainerSize() { return handler.getSlots(); }
    public boolean isEmpty() { for (int i = 0; i < handler.getSlots(); i++) if (!handler.getStackInSlot(i).isEmpty()) return false; return true; }
    public ItemStack getItem(int slot) { return handler.getStackInSlot(slot); }
    public ItemStack removeItem(int slot, int amount) { return handler.extractItem(slot, amount, false); }
    public ItemStack removeItemNoUpdate(int slot) { return removeItem(slot, Integer.MAX_VALUE); }
    public void setItem(int slot, ItemStack stack) { if (handler instanceof ModifiableItemHandler modifiable) modifiable.setStackInSlot(slot, stack); else { handler.extractItem(slot, Integer.MAX_VALUE, false); handler.insertItem(slot, stack, false); } }
    public void setChanged() { if (handler instanceof ItemStackHandler inventory) inventory.setChanged(); }
    public boolean stillValid(Player player) { return true; }
    public boolean canPlaceItem(int slot, ItemStack stack) { return handler.isItemValid(slot, stack); }
    public void clearContent() { for (int i = 0; i < handler.getSlots(); i++) removeItemNoUpdate(i); }
}
