package committee.nova.mods.avaritia.api.common.wrapper;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

public class ContainerItemHandler implements ModifiableItemHandler {
    private final Container container;
    private final Direction side;
    public ContainerItemHandler(Container container) { this(container, null); }
    public ContainerItemHandler(Container container, Direction side) { this.container = container; this.side = side; }
    private int slot(int index) { return side != null && container instanceof WorldlyContainer sided ? sided.getSlotsForFace(side)[index] : index; }
    public int getSlots() { return side != null && container instanceof WorldlyContainer sided ? sided.getSlotsForFace(side).length : container.getContainerSize(); }
    public ItemStack getStackInSlot(int index) { return container.getItem(slot(index)); }
    public int getSlotLimit(int index) { return container.getMaxStackSize(); }
    public boolean isItemValid(int index, ItemStack stack) { int slot = slot(index); return container.canPlaceItem(slot, stack) && (!(container instanceof WorldlyContainer sided) || side == null || sided.canPlaceItemThroughFace(slot, stack, side)); }
    public void setStackInSlot(int index, ItemStack stack) { container.setItem(slot(index), stack); container.setChanged(); }
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(index, stack)) return stack;
        ItemStack current = getStackInSlot(index);
        if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, stack)) return stack;
        int accepted = Math.min(stack.getCount(), Math.max(0, Math.min(getSlotLimit(index), stack.getMaxStackSize()) - current.getCount()));
        if (!simulate && accepted > 0) setStackInSlot(index, stack.copyWithCount(current.getCount() + accepted));
        return accepted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
    }
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        int slot = slot(index); ItemStack stack = container.getItem(slot);
        if (amount <= 0 || stack.isEmpty() || side != null && container instanceof WorldlyContainer sided && !sided.canTakeItemThroughFace(slot, stack, side)) return ItemStack.EMPTY;
        int count = Math.min(amount, stack.getCount());
        if (simulate) return stack.copyWithCount(count);
        ItemStack result = container.removeItem(slot, count); container.setChanged(); return result;
    }
}
