package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.api.common.slot.OutputSlot;
import committee.nova.mods.avaritia.common.tile.collector.AbsNeutronCollectorTile;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:13
 * Version: 1.0
 */
public class NeutronCollectorMenu extends BaseMenu<AbsNeutronCollectorTile> {
    private final ContainerData data;

    public NeutronCollectorMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, AbsNeutronCollectorTile.createInventoryHandler(null), new SimpleContainerData(10), getTileEntityFromBuf(buffer, AbsNeutronCollectorTile.class));
    }

    public NeutronCollectorMenu(int id, Inventory playerInventory, SlottedStackStorage inventory, ContainerData data, AbsNeutronCollectorTile tile) {
        super(ModMenus.neutron_collector.get(), id, tile);
        this.data = data;
        this.addSlot(new OutputSlot(inventory, 0, 80, 32));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
        this.addDataSlots(data);
    }


    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotNumber) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(slotNumber);

        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotNumber < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else {
                if (slotNumber < 28) {
                    if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotNumber < 37) {
                    if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public int getProgress() {
        return data.get(0);
    }
}
