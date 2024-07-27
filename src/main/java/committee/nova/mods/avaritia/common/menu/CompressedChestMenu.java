package committee.nova.mods.avaritia.common.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CompressedChestMenu extends AbstractContainerMenu {
    private static final int SLOTS_PER_ROW = 27;
    private final Container container;
    private final int containerRows;

    public CompressedChestMenu(MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, Container pContainer, int pRows) {
        super(pType, pContainerId);
        this.containerRows = pRows;
        this.container = pContainer;
        pContainer.startOpen(pPlayerInventory.player);
        int $$5 = (this.containerRows - 4) * 18;

        int $$10;
        int $$9;
        for($$10 = 0; $$10 < this.containerRows; ++$$10) {
            for($$9 = 0; $$9 < SLOTS_PER_ROW; ++$$9) {
                this.addSlot(new Slot(pContainer, $$9 + $$10 * SLOTS_PER_ROW, 8 + $$9 * 18, 17 + $$10 * 18));
            }
        }

        for($$10 = 0; $$10 < 3; ++$$10) {
            for($$9 = 0; $$9 < 9; ++$$9) {
                this.addSlot(new Slot(pPlayerInventory, $$9 + $$10 * 9 + 9, 170 + $$9 * 18, 104 + $$10 * 18 + $$5));
            }
        }

        for($$10 = 0; $$10 < 9; ++$$10) {
            this.addSlot(new Slot(pPlayerInventory, $$10, 170 + $$10 * 18, 162 + $$5));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int pIndex) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = this.slots.get(pIndex);
        if ($$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            if (pIndex < this.containerRows * SLOTS_PER_ROW) {
                if (!this.moveItemStackTo($$4, this.containerRows * SLOTS_PER_ROW, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo($$4, 0, this.containerRows * SLOTS_PER_ROW, false)) {
                return ItemStack.EMPTY;
            }

            if ($$4.isEmpty()) {
                $$3.setByPlayer(ItemStack.EMPTY);
            } else {
                $$3.setChanged();
            }
        }

        return $$2;
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.container.stopOpen(pPlayer);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    public int getRowCount() {
        return this.containerRows;
    }

    public Container getContainer() {
        return this.container;
    }
}
