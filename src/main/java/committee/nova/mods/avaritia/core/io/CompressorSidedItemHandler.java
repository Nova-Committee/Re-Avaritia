package committee.nova.mods.avaritia.core.io;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Maps a compressor face to its input slot, output slot, or both.
 * The underlying inventory remains responsible for its own insertion/extraction rules.
 */
public final class CompressorSidedItemHandler implements IItemHandler {
    private final IItemHandler inventory;
    private final Supplier<SideConfiguration.SideMode> mode;

    public CompressorSidedItemHandler(IItemHandler inventory, Supplier<SideConfiguration.SideMode> mode) {
        this.inventory = inventory;
        this.mode = mode;
    }

    @Override
    public int getSlots() {
        var currentMode = mode.get();
        return currentMode.isMixed() ? 2 : currentMode.canInput() || currentMode.canOutput() ? 1 : 0;
    }

    private int inventorySlot(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            throw new IndexOutOfBoundsException("Invalid compressor face slot: " + slot);
        }
        var currentMode = mode.get();
        return currentMode.isMixed() ? (slot == 0 ? 1 : 0) : currentMode.canInput() ? 1 : 0;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(inventorySlot(slot));
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        int targetSlot = inventorySlot(slot);
        return targetSlot == 1 && mode.get().canInput()
                ? inventory.insertItem(targetSlot, stack, simulate) : stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        int targetSlot = inventorySlot(slot);
        return targetSlot == 0 && mode.get().canOutput()
                ? inventory.extractItem(targetSlot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(inventorySlot(slot));
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        int targetSlot = inventorySlot(slot);
        return targetSlot == 1 && mode.get().canInput() && inventory.isItemValid(targetSlot, stack);
    }
}
