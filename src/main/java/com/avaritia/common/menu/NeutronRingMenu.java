package com.avaritia.common.menu;

import com.avaritia.api.common.menu.BaseMenu;
import com.avaritia.api.common.slot.BlackListSlot;
import com.avaritia.api.utils.InventoryUtils;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/20 13:37
 * @Description:
 */
public class NeutronRingMenu extends BaseMenu {
    public ItemStack ring = ItemStack.EMPTY;
    public int slot;

    public NeutronRingMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory, buf.readInt());
    }

    public NeutronRingMenu(int id, Inventory playerInventory, int slot) {
        super(ModMenus.neutron_ring.get(), id, playerInventory);
        this.slot = slot;
        if (slot > -1) {
            this.ring = playerInventory.getItem(slot);
        }
        if (ring.isEmpty()) {
            this.ring = InventoryUtils.findItemInInv(playerInventory.player, stack -> stack.is(ModItems.neutron_ring.get()), stack -> stack);
        }
        Optional.ofNullable(ring.getCapability(Capabilities.ItemHandler.ITEM)).ifPresent(h -> {
            for (int j = 0; j < h.getSlots(); j++) {
                int row = j / 9;
                int col = j % 9;
                int xPos = 8 + col * 18;
                int yPos = 18 + row * 18;
                this.addSlot(new SlotItemHandler(h, j, xPos, yPos));
            }
        });
        int i, j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 9; j++) {
                this.addSlot(new BlackListSlot(playerInventory, j + i * 9 + 9, 8 + j * 18, 193 + i * 18, ring));
            }
        }

        for (j = 0; j < 9; j++) {
            this.addSlot(new BlackListSlot(playerInventory, j, 8 + j * 18, 251, ring));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int pIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot1 = this.slots.get(pIndex);
        if (slot1.hasItem()) {
            ItemStack slot1Item = slot1.getItem();
            itemStack = slot1Item.copy();

            if (pIndex < 9 * 9) {
                if (!this.moveItemStackTo(slot1Item, 9 * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slot1Item, 0, 9 * 9, false)) {
                return ItemStack.EMPTY;
            }


            if (slot1Item.isEmpty()) {
                slot1.setByPlayer(ItemStack.EMPTY);
            } else {
                slot1.setChanged();
            }
        }

        return itemStack;
    }

}
