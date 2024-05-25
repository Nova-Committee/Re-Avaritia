package committee.nova.mods.avaritia.api.common.item;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 10:59
 * Version: 1.0
 */

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class BaseItemStackHandler extends ItemStackHandlerContainer {
    private final Runnable onContentsChanged;
    private final Map<Integer, Integer> slotSizeMap;
    @Setter
    private BiFunction<Integer, ItemVariant, Boolean> slotValidator;
    private int maxStackSize;
    @Getter
    private int[] outputSlots;

    public BaseItemStackHandler(int size) {
        this(size, null);
    }

    public BaseItemStackHandler(int size, Runnable onContentsChanged) {
        super(size);
        this.slotValidator = null;
        this.maxStackSize = 64;
        this.outputSlots = null;
        this.onContentsChanged = onContentsChanged;
        this.slotSizeMap = new HashMap<>();
    }

    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return this.outputSlots != null && ArrayUtils.contains(this.outputSlots, slot) ? stack : insertItemSuper(slot, stack, simulate);
    }

    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.outputSlots != null && !ArrayUtils.contains(this.outputSlots, slot) ? ItemStack.EMPTY : extractItemSuper(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.slotSizeMap.containsKey(slot) ? this.slotSizeMap.get(slot) : this.maxStackSize;
    }

    @Override
    public boolean isItemValid(int slot, ItemVariant resource, int count) {
        return this.slotValidator == null || this.slotValidator.apply(slot, resource);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.onContentsChanged != null) {
            this.onContentsChanged.run();
        }

    }

    public ItemStack insertItemSuper(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else if (!this.isItemValid(slot, ItemVariant.of(stack), stack.getCount())) {
            return stack;
        } else {
            this.validateSlotIndex(slot);
            ItemStack existing = this.getStackInSlot(slot);
            int limit = this.getStackLimit(slot, ItemVariant.of(stack));
            if (!existing.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                    return stack;
                }

                limit -= existing.getCount();
            }

            if (limit <= 0) {
                return stack;
            } else {
                boolean reachedLimit = stack.getCount() > limit;
                if (!simulate) {
                    if (existing.isEmpty()) {
                        this.setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                    } else {
                        existing.grow(reachedLimit ? limit : stack.getCount());
                    }

                    this.onContentsChanged(slot);
                }

                return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
            }
        }
    }

    public ItemStack extractItemSuper(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        } else {
            this.validateSlotIndex(slot);
            ItemStack existing = this.getStackInSlot(slot);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                int toExtract = Math.min(amount, existing.getMaxStackSize());
                if (existing.getCount() <= toExtract) {
                    if (!simulate) {
                        this.setStackInSlot(slot, ItemStack.EMPTY);
                        this.onContentsChanged(slot);
                        return existing;
                    } else {
                        return existing.copy();
                    }
                } else {
                    if (!simulate) {
                        this.setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                        this.onContentsChanged(slot);
                    }

                    return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
                }
            }
        }
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

    public BaseItemStackHandler copy() {
        BaseItemStackHandler newInventory = new BaseItemStackHandler(this.getSlotCount(), this.onContentsChanged);
        newInventory.setDefaultSlotLimit(this.maxStackSize);
        newInventory.setSlotValidator(this.slotValidator);
        newInventory.setOutputSlots(this.outputSlots);
        Objects.requireNonNull(newInventory);
        this.slotSizeMap.forEach(newInventory::addSlotLimit);

        for (int i = 0; i < this.getSlotCount(); ++i) {
            ItemStack stack = this.getStackInSlot(i);
            newInventory.setStackInSlot(i, stack.copy());
        }

        return newInventory;
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.getSlotCount()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.getSlotCount() + ")");
        }
    }
}
