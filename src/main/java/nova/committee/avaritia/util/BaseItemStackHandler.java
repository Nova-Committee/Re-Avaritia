package nova.committee.avaritia.util;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 10:59
 * Version: 1.0
 */

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class BaseItemStackHandler extends ItemStackHandler {
    private final Runnable onContentsChanged;
    private final Map<Integer, Integer> slotSizeMap;
    private BiFunction<Integer, ItemStack, Boolean> slotValidator;
    private int maxStackSize;
    private int[] outputSlots;

    public BaseItemStackHandler(int size) {
        this(size, (Runnable) null);
    }

    public BaseItemStackHandler(int size, Runnable onContentsChanged) {
        super(size);
        this.slotValidator = null;
        this.maxStackSize = 64;
        this.outputSlots = null;
        this.onContentsChanged = onContentsChanged;
        this.slotSizeMap = new HashMap();
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.outputSlots != null && ArrayUtils.contains(this.outputSlots, slot) ? stack : super.insertItem(slot, stack, simulate);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.outputSlots != null && !ArrayUtils.contains(this.outputSlots, slot) ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
    }

    public int getSlotLimit(int slot) {
        return this.slotSizeMap.containsKey(slot) ? (Integer) this.slotSizeMap.get(slot) : this.maxStackSize;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return this.slotValidator == null || (Boolean) this.slotValidator.apply(slot, stack);
    }

    protected void onContentsChanged(int slot) {
        if (this.onContentsChanged != null) {
            this.onContentsChanged.run();
        }

    }

    public ItemStack insertItemSuper(int slot, ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }

    public ItemStack extractItemSuper(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }

    public NonNullList<ItemStack> getStacks() {
        return this.stacks;
    }

    public int[] getOutputSlots() {
        return this.outputSlots;
    }

    public void setOutputSlots(int... slots) {
        this.outputSlots = slots;
    }

    public void setDefaultSlotLimit(int size) {
        this.maxStackSize = size;
    }

    public void addSlotLimit(int slot, int size) {
        this.slotSizeMap.put(slot, size);
    }

    public void setSlotValidator(BiFunction<Integer, ItemStack, Boolean> validator) {
        this.slotValidator = validator;
    }

    public Container toIInventory() {
        return new SimpleContainer((ItemStack[]) this.stacks.toArray(new ItemStack[0]));
    }

    public BaseItemStackHandler copy() {
        BaseItemStackHandler newInventory = new BaseItemStackHandler(this.getSlots(), this.onContentsChanged);
        newInventory.setDefaultSlotLimit(this.maxStackSize);
        newInventory.setSlotValidator(this.slotValidator);
        newInventory.setOutputSlots(this.outputSlots);
        Map<Integer, Integer> sizeMap = this.slotSizeMap;
        Objects.requireNonNull(newInventory);
        sizeMap.forEach(newInventory::addSlotLimit);

        for (int i = 0; i < this.getSlots(); ++i) {
            ItemStack stack = this.getStackInSlot(i);
            newInventory.setStackInSlot(i, stack.copy());
        }

        return newInventory;
    }
}
