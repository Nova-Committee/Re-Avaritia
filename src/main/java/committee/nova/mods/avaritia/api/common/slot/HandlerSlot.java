package committee.nova.mods.avaritia.api.common.slot;
import committee.nova.mods.avaritia.api.common.wrapper.*;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public class HandlerSlot extends Slot {
    private final ItemHandler handler;
    private final int handlerIndex;
    public HandlerSlot(ItemHandler handler, int index, int x, int y) { super(new HandlerContainer(handler), index, x, y); this.handler = handler; this.handlerIndex = index; }
    public ItemHandler getItemHandler() { return handler; }
    @Override public boolean mayPlace(ItemStack stack) { return handler.isItemValid(handlerIndex, stack); }
    @Override public boolean mayPickup(Player player) { return !handler.extractItem(handlerIndex, 1, true).isEmpty(); }
    @Override public int getMaxStackSize() { return handler.getSlotLimit(handlerIndex); }
    @Override public int getMaxStackSize(ItemStack stack) { return Math.min(getMaxStackSize(), stack.getMaxStackSize()); }
    @Override public ItemStack remove(int amount) { return handler.extractItem(handlerIndex, amount, false); }
    @Override public void setChanged() { if (handler instanceof ModifiableItemHandler modifiable) modifiable.setStackInSlot(handlerIndex, handler.getStackInSlot(handlerIndex)); else super.setChanged(); }
}
