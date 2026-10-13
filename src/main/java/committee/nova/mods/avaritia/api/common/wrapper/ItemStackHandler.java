package committee.nova.mods.avaritia.api.common.wrapper;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

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
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) return stack;
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
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList items = output.childrenList("Items");
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                ValueOutput item = items.addChild();
                item.putInt("Slot", i);
                item.putInt("ExtendedCount", stack.getCount());
                item.store("item", ItemStack.CODEC, stack.copyWithCount(1));
            }
        }
        output.putInt("Size", stacks.size());
    }
    public void deserialize(ValueInput input) {
        this.setSize(input.getIntOr("Size", stacks.size()));
        Optional<ValueInput.ValueInputList> items = input.childrenList("Items");
        if (items.isPresent()) {
            for (ValueInput item : items.get()) {
                int slot = item.getIntOr("Slot", 0);
                if (slot >= 0 && slot < stacks.size()) {
                    ItemStack stack = item.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
                    stack.setCount(item.getIntOr("ExtendedCount", stack.getCount()));
                    stacks.set(slot, stack);
                }
            }
        }
        this.onLoad();
    }
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        this.serialize(output);
        return output.buildResult();
    }
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }
    public void setChanged() { onContentsChanged(-1); }
    protected void onContentsChanged(int slot) {}
    protected void onLoad() {}
}