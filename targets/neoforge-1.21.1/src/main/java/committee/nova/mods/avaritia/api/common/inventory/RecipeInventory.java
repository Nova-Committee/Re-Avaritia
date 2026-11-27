package committee.nova.mods.avaritia.api.common.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/14 19:14
 * @Description:
 */
public class RecipeInventory implements Container {
    private final IItemHandlerModifiable inventory;
    private final int start;
    private final int size;

    public RecipeInventory(IItemHandlerModifiable inventory) {
        this(inventory, 0, inventory.getSlots());
    }

    public RecipeInventory(IItemHandlerModifiable inventory, int start, int size) {
        this.inventory = inventory;
        this.start = start;
        this.size = size;
    }

    public int getContainerSize() {
        return this.size;
    }

    public @NotNull ItemStack getItem(int slot) {
        return this.inventory.getStackInSlot(slot + this.start);
    }

    public @NotNull ItemStack removeItem(int slot, int count) {
        ItemStack stack = this.inventory.getStackInSlot(slot + this.start);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
    }

    public void setItem(int slot, @NotNull ItemStack stack) {
        this.inventory.setStackInSlot(slot + this.start, stack);
    }

    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.getItem(slot + this.start);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.setItem(slot + this.start, ItemStack.EMPTY);
            return stack;
        }
    }

    public boolean isEmpty() {
        for(int i = this.start; i < this.size; ++i) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return this.inventory.isItemValid(slot + this.start, stack);
    }

    public void clearContent() {
        for(int i = this.start; i < this.size; ++i) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }

    }

    public int getMaxStackSize() {
        return 0;
    }

    public void setChanged() {
    }

    public boolean stillValid(@NotNull Player player) {
        return false;
    }

    public void startOpen(@NotNull Player player) {
    }

    public void stopOpen(@NotNull Player player) {
    }
}
