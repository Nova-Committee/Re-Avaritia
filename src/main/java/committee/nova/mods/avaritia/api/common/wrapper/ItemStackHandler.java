package committee.nova.mods.avaritia.api.common.wrapper;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class ItemStackHandler implements ModifiableItemHandler {
    protected NonNullList<ItemStack> stacks;
    public ItemStackHandler() { this(1); }
    public ItemStackHandler(int size) { setSize(size); }
    public ItemStackHandler(NonNullList<ItemStack> stacks) { this.stacks = stacks; }
    public void setSize(int size) { stacks = NonNullList.withSize(size, ItemStack.EMPTY); }
    protected void validateSlotIndex(int slot) { if (slot < 0 || slot >= stacks.size()) throw new IndexOutOfBoundsException("Slot " + slot); }
    public int getSlots() { return stacks.size(); }
    public ItemStack getStackInSlot(int slot) { validateSlotIndex(slot); return stacks.get(slot); }
    public void setStackInSlot(int slot, ItemStack stack) { validateSlotIndex(slot); stacks.set(slot, stack); onContentsChanged(slot); }
    public int getSlotLimit(int slot) { return 64; }
    public boolean isItemValid(int slot, ItemStack stack) { return true; }
    protected int getStackLimit(int slot, ItemStack stack) { return Math.min(getSlotLimit(slot), stack.getMaxStackSize()); }
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
        ItemStack existing = stacks.get(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, stack)) return stack;
        int accepted = Math.min(stack.getCount(), Math.max(0, getStackLimit(slot, stack) - existing.getCount()));
        if (accepted == 0) return stack;
        if (!simulate) { if (existing.isEmpty()) stacks.set(slot, stack.copyWithCount(accepted)); else existing.grow(accepted); onContentsChanged(slot); }
        return accepted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
    }
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);
        ItemStack existing = stacks.get(slot);
        if (amount <= 0 || existing.isEmpty()) return ItemStack.EMPTY;
        int taken = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(taken);
        if (!simulate) { if (taken == existing.getCount()) stacks.set(slot, ItemStack.EMPTY); else existing.shrink(taken); onContentsChanged(slot); }
        return result;
    }
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag(); ListTag items = new ListTag();
        for (int i = 0; i < stacks.size(); i++) if (!stacks.get(i).isEmpty()) { CompoundTag item = stacks.get(i).save(new CompoundTag()); item.putInt("Slot", i); item.putInt("ExtendedCount", stacks.get(i).getCount()); items.add(item); }
        tag.put("Items", items); tag.putInt("Size", stacks.size()); return tag;
    }
    public void deserializeNBT(CompoundTag tag) {
        setSize(tag.contains("Size", 3) ? Math.max(0, tag.getInt("Size")) : stacks.size());
        ListTag items = tag.getList("Items", 10);
        for (int i = 0; i < items.size(); i++) { CompoundTag item = items.getCompound(i); int slot = item.getInt("Slot"); if (slot >= 0 && slot < stacks.size()) { ItemStack stack = ItemStack.of(item); if (item.contains("ExtendedCount", 3)) stack.setCount(item.getInt("ExtendedCount")); stacks.set(slot, stack); } }
        onLoad();
    }
    public void setChanged() { onContentsChanged(-1); }
    protected void onContentsChanged(int slot) {}
    protected void onLoad() {}
}
