package committee.nova.mods.avaritia.api.common.wrapper;

import committee.nova.mods.avaritia.api.common.crafting.ShapelessCraftingInput;
import committee.nova.mods.avaritia.api.common.inventory.CanExtractFunction;
import committee.nova.mods.avaritia.api.common.inventory.CanInsertFunction;
import committee.nova.mods.avaritia.api.common.inventory.OnContentsChangedFunction;
import committee.nova.mods.avaritia.api.common.inventory.RecipeInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Project item storage backed by NeoForge's transaction-aware ResourceHandler API.
 */
public class ItemStackWrapper extends ItemStacksResourceHandler implements BaseItemWrapper {
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

    public void setSize(int size) {
        this.setStacks(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    public int getSlots() {
        return this.size();
    }

    public @NotNull ItemStack getStackInSlot(int slot) {
        Objects.checkIndex(slot, this.size());
        return this.stacks.get(slot);
    }

    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        this.set(slot, ItemResource.of(stack), stack.getCount());
    }

    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return this.insertItem(slot, stack, simulate, false);
    }

    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean container) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!container && this.outputSlots != null && ArrayUtils.contains(this.outputSlots, slot)) {
            return stack;
        }

        try (var tx = Transaction.openRoot()) {
            int inserted = this.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int remaining = stack.getCount() - inserted;
            return remaining == 0 ? ItemStack.EMPTY : stack.copyWithCount(remaining);
        }
    }

    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.extractItem(slot, amount, simulate, false);
    }

    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate, boolean container) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        if (!container && this.canExtract != null && !this.canExtract.apply(slot)) {
            return ItemStack.EMPTY;
        }

        ItemResource resource = this.getResource(slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }

        try (var tx = Transaction.openRoot()) {
            int extracted = this.extract(slot, resource, amount, tx);
            if (!simulate) {
                tx.commit();
            }
            return extracted == 0 ? ItemStack.EMPTY : resource.toStack(extracted);
        }
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (this.outputSlots != null && ArrayUtils.contains(this.outputSlots, index)) {
            return 0;
        }
        return super.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (this.canExtract != null && !this.canExtract.apply(index)) {
            return 0;
        }
        return super.extract(index, resource, amount, transaction);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (resource.isEmpty()) {
            return false;
        }
        return this.canInsert == null || this.canInsert.apply(index, resource.toStack());
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        int slotLimit = this.slotSizeMap.getOrDefault(index, this.maxStackSize);
        return resource.isEmpty() ? slotLimit : Math.min(slotLimit, resource.getMaxStackSize());
    }

    public int getSlotLimit(int slot) {
        return this.slotSizeMap.getOrDefault(slot, this.maxStackSize);
    }

    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return !stack.isEmpty() && this.isValid(slot, ItemResource.of(stack));
    }

    public Container toIInventory() {
        return new SimpleContainer(this.stacks.toArray(new ItemStack[0]));
    }

    @Override
    protected void onContentsChanged(int slot, ItemStack previousContents) {
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
        super.serialize(output);
    }

    @Override
    public void deserialize(ValueInput input) {
        super.deserialize(input);
    }

    public NonNullList<ItemStack> getStacks() {
        return this.copyToList();
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
        }
        this.slotSizeMap.put(slot, size);
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
        return new RecipeInventory(this, this::set, start, size);
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

        for (int i = 0; i < this.getSlots(); ++i) {
            ItemStack stack = this.getStackInSlot(i);
            newInventory.setStackInSlot(i, stack.copy());
        }

        return newInventory;
    }

    public static ItemStackWrapper create(int size) {
        return create(size, builder -> {
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
