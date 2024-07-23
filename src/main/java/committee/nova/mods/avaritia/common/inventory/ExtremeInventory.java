package committee.nova.mods.avaritia.common.inventory;

import committee.nova.mods.avaritia.api.common.item.BaseItemStackHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:09
 * Version: 1.0
 */
public final class ExtremeInventory extends TransientCraftingContainer {
    private final AbstractContainerMenu container;
    private final BaseItemStackHandler inventory;
    private final boolean autoTable;

    public ExtremeInventory(AbstractContainerMenu container, BaseItemStackHandler inventory, int size) {
        this(container, inventory, size, false);
    }

    public ExtremeInventory(AbstractContainerMenu container, BaseItemStackHandler inventory, int size, boolean autoTable) {
        super(container, size, size);
        this.container = container;
        this.inventory = inventory;
        this.autoTable = autoTable;
    }

    @Override
    public int getContainerSize() {
        return this.autoTable ? this.inventory.getSlots() - 1 : this.inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            if (!this.inventory.getStackInSlot(i).isEmpty())
                return false;
        }

        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return this.inventory.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        var stack = this.inventory.extractItemSuper(slot, amount, false);

        this.container.slotsChanged(this);

        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        var stack = this.inventory.getStackInSlot(slot);

        this.inventory.setStackInSlot(slot, ItemStack.EMPTY);

        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        this.inventory.setStackInSlot(slot, stack);
        this.container.slotsChanged(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
