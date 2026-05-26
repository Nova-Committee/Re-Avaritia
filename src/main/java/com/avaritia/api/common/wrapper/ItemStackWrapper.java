package com.avaritia.api.common.wrapper;


import com.avaritia.Const;
import com.avaritia.api.common.crafting.ShapelessCraftingInput;
import com.avaritia.api.common.inventory.CanExtractFunction;
import com.avaritia.api.common.inventory.CanInsertFunction;
import com.avaritia.api.common.inventory.OnContentsChangedFunction;
import com.avaritia.api.common.inventory.RecipeInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/4/2 10:59
 * @Description:
 */
@SuppressWarnings("removal")
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

    public @NotNull CompoundTag serializeNBT(HolderLookup.@NotNull Provider lookup) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
        this.serialize(output);
        return output.buildResult();
    }

    public void deserializeNBT(HolderLookup.@NotNull Provider lookup, CompoundTag nbt) {
        this.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, lookup, nbt));
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> itemList = output.list("Items", ItemStackWithSlot.CODEC);

        for (int i = 0; i < this.stacks.size(); ++i) {
            ItemStack stack = this.stacks.get(i);
            if (!stack.isEmpty()) {
                itemList.add(new ItemStackWithSlot(i, stack));
            }
        }

        output.putInt("Size", this.stacks.size());
    }

    @Override
    public void deserialize(ValueInput input) {
        this.setSize(Math.max(input.getIntOr("Size", this.stacks.size()), this.stacks.size()));
        input.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(item -> {
            if (item.isValidInContainer(this.stacks.size())) {
                this.stacks.set(item.slot(), item.stack());
            }
        });
        this.onLoad();
    }

    public @NotNull CompoundTag serializeNBTLegacy(HolderLookup.@NotNull Provider lookup) {
        ListTag items = new ListTag();

        for(int i = 0; i < this.stacks.size(); ++i) {
            ItemStack stack = (ItemStack)this.stacks.get(i);
            if (!stack.isEmpty()) {
                CompoundTag item = new CompoundTag();
                item.putInt("Slot", i);
                ItemStack.CODEC.encode(stack, lookup.createSerializationContext(NbtOps.INSTANCE), item)
                        .resultOrPartial(error -> Const.LOGGER.error("Tried to save invalid item: '{}'", error))
                        .ifPresent(items::add);
            }
        }

        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", items);
        nbt.putInt("Size", this.stacks.size());
        return nbt;
    }

    public void deserializeNBTLegacy(HolderLookup.@NotNull Provider lookup, CompoundTag nbt) {
        int size = nbt.contains("Size", 3) ? nbt.getInt("Size") : this.stacks.size();
        this.setSize(Math.max(size, this.stacks.size()));
        ListTag items = nbt.getList("Items", 10);

        for(int i = 0; i < items.size(); ++i) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getInt("Slot");
            if (slot >= 0 && slot < this.stacks.size()) {
                ItemStack.CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), item).resultOrPartial((error) -> Const.LOGGER.error("Tried to load invalid item: '{}'", error)).ifPresent((stack) -> this.stacks.set(slot, stack));
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

    public RecipeInventory toRecipeInventory() {
        return this.toRecipeInventory(0, this.stacks.size());
    }

    public RecipeInventory toRecipeInventory(int start, int size) {
        return new RecipeInventory(this, start, size);
    }

    public CraftingInput toCraftingInput(int width, int height) {
        return CraftingInput.of(width, height, this.stacks);
    }

    public CraftingInput toShapelessCraftingInput() {
        return new ShapelessCraftingInput(this.stacks);
    }

    public CraftingInput toCraftingInput(int width, int height, int startIndex, int endIndex) {
        return CraftingInput.of(width, height, this.stacks.subList(startIndex, endIndex));
    }

    public CraftingInput toShapelessCraftingInput(int startIndex, int endIndex) {
        return new ShapelessCraftingInput(this.stacks.subList(startIndex, endIndex));
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
