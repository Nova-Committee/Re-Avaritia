package committee.nova.mods.avaritia.api.common.wrapper;


import committee.nova.mods.avaritia.api.common.inventory.CanExtractFunction;
import committee.nova.mods.avaritia.api.common.inventory.CanInsertFunction;
import committee.nova.mods.avaritia.api.common.inventory.OnContentsChangedFunction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2022/4/2 10:59
 * @Description:
 */
public class ItemStackWrapper extends ItemStackHandler {
    private final OnContentsChangedFunction onContentsChanged;
    private final Map<Integer, Integer> slotSizeMap;
    private CanInsertFunction canInsert = null;
    private CanExtractFunction canExtract = null;
    private int maxStackSize = 64;
    private int[] outputSlots = null;

    public ItemStackWrapper(int size, OnContentsChangedFunction onContentsChanged) {
        super(size);
        this.onContentsChanged = onContentsChanged;
        this.slotSizeMap = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return this.insertItem(slot, stack, simulate, false);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate, boolean container) {
        return !container && this.outputSlots != null && ArrayUtils.contains(this.outputSlots, slot) ? stack : super.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.extractItem(slot, amount, simulate, false);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean container) {
        if (!container) {
            if (this.canExtract != null && !this.canExtract.apply(slot)) {
                return ItemStack.EMPTY;
            }

            if (this.outputSlots != null && !ArrayUtils.contains(this.outputSlots, slot)) {
                return ItemStack.EMPTY;
            }
        }

        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.slotSizeMap.containsKey(slot) ? this.slotSizeMap.get(slot) : this.maxStackSize;
    }

    @Override
    public int getStackLimit(int slot, @NotNull ItemStack stack) {
        return super.getStackLimit(slot, stack);
    }
    public Container toIInventory() {
        return new SimpleContainer(this.stacks.toArray(new ItemStack[0]));
    }
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return this.canInsert == null || this.canInsert.apply(slot, stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.onContentsChanged != null) {
            this.onContentsChanged.apply(slot);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var items = new ListTag();

        for (int i = 0; i < this.stacks.size(); i++) {
            var stack = this.stacks.get(i);

            if (!stack.isEmpty()) {
                var item = new CompoundTag();

                item.putInt("Slot", i);

                // change: store additional ExtendedCount value for stack sizes larger than normal
                if (stack.getCount() > 64) {
                    item.putInt("ExtendedCount", stack.getCount());
                }

                stack.save(item);
                items.add(item);
            }
        }

        var nbt = new CompoundTag();

        nbt.put("Items", items);
        nbt.putInt("Size", this.stacks.size());

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.setSize(nbt.contains("Size", 3) ? nbt.getInt("Size") : this.stacks.size());

        var items = nbt.getList("Items", 10);

        for (int i = 0; i < items.size(); ++i) {
            var item = items.getCompound(i);
            int slot = item.getInt("Slot");

            if (slot >= 0 && slot < this.stacks.size()) {
                var stack = ItemStack.of(item);

                // change: use the ExtendedCount value instead if it exists
                if (item.contains("ExtendedCount")) {
                    stack.setCount(item.getInt("ExtendedCount"));
                }

                this.stacks.set(slot, stack);
            }
        }

        this.onLoad();
    }
    public NonNullList<ItemStack> getStacks() {
        return this.stacks;
    }

    public int[] getOutputSlots() {
        return this.outputSlots;
    }

    public void setDefaultSlotLimit(int size) {
        this.maxStackSize = size;
    }

    public void addSlotLimit(int slot, int size) {
        if (size > 64 && size % 64 != 0) {
            throw new IllegalArgumentException("Slot limits above 64 must be a multiple of 64");
        } else {
            this.slotSizeMap.put(slot, size);
        }
    }

    public void setCanInsert(CanInsertFunction canInsert) {
        this.canInsert = canInsert;
    }

    public void setCanExtract(CanExtractFunction canExtract) {
        this.canExtract = canExtract;
    }

    public void setOutputSlots(int... slots) {
        this.outputSlots = slots;
    }

    public ItemStackWrapper copy() {
        ItemStackWrapper newInventory = new ItemStackWrapper(this.getSlots(), this.onContentsChanged);
        newInventory.setDefaultSlotLimit(this.maxStackSize);
        newInventory.setCanInsert(this.canInsert);
        newInventory.setCanExtract(this.canExtract);
        newInventory.setOutputSlots(this.outputSlots);
        Objects.requireNonNull(newInventory);
        this.slotSizeMap.forEach(newInventory::addSlotLimit);

        for(int i = 0; i < this.getSlots(); ++i) {
            ItemStack stack = this.getStackInSlot(i);
            newInventory.setStackInSlot(i, stack.copy());
        }

        return newInventory;
    }

    public static ItemStackWrapper create(int size) {
        return create(size, (builder) -> {
        });
    }

    public static ItemStackWrapper create(int size, Consumer<ItemStackWrapper> builder) {
        return create(size, null, builder);
    }

    public static ItemStackWrapper create(int size, OnContentsChangedFunction onContentsChanged, Consumer<ItemStackWrapper> builder) {
        ItemStackWrapper handler = new ItemStackWrapper(size, onContentsChanged);
        builder.accept(handler);
        return handler;
    }

}
