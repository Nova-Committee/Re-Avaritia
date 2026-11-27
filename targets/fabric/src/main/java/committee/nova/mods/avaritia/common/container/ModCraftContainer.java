package committee.nova.mods.avaritia.common.container;

import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 11:09
 * Version: 1.0
 */
public class ModCraftContainer extends TransientCraftingContainer {
    private final AbstractContainerMenu menu;
    private final ItemStackWrapper inventory;

    public ModCraftContainer(AbstractContainerMenu menu, ItemStackWrapper inventory, int size) {
        super(menu, (int) Math.sqrt(size), (int) Math.sqrt(size), inventory.getStacks());
        this.menu = menu;
        this.inventory = inventory;
    }

    @Override
    public int getContainerSize() { return inventory.getSlots(); }

    @Override
    public java.util.List<ItemStack> getItems() { return inventory.getStacks(); }

    @Override
    public void fillStackedContents(net.minecraft.world.entity.player.StackedContents contents) {
        for (ItemStack stack : inventory.getStacks()) contents.accountSimpleStack(stack);
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
        var stack = this.inventory.extractItem(slot, amount, false, true);
        this.menu.slotsChanged(this);
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
        this.menu.slotsChanged(this);
    }

    @Override
    public void setChanged() {
        inventory.setChanged();
        menu.slotsChanged(this);
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
