package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.common.inventory.slot.BlackListSlot;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.util.InventoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/6 上午12:38
 * @Description:
 */
public class NeutronRingMenu extends BaseMenu {
    public ItemStack ring = ItemStack.EMPTY;
    public int slot;

    public static NeutronRingMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        return new NeutronRingMenu(ModMenus.neutron_ring.get(), windowId, playerInventory, buf);
    }

    public static NeutronRingMenu create(int windowId, Inventory playerInventory, int slot) {
        return new NeutronRingMenu(ModMenus.neutron_ring.get(), windowId, playerInventory, slot);
    }

    private NeutronRingMenu(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(type, id, playerInventory, buf.readInt());
    }

    public NeutronRingMenu(MenuType<?> type, int id, Inventory playerInventory, int slot) {
        super(type, id, null);
        this.slot = slot;
        if (slot > -1) {
            this.ring = playerInventory.getItem(slot);
        }
        if (ring.isEmpty()) {
            this.ring = InventoryUtils.findInInv(playerInventory.player, playerInventory, ModItems.neutron_ring.get());
        }
        ring.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            for (int j = 0; j < h.getSlots(); j++) {
                int row = j / 9;
                int col = j % 9;
                int xPos = 8 + col * 18;
                int yPos = 18 + row * 18;
                this.addSlot(new SlotItemHandler(h, j, xPos, yPos){
                    @Override
                    public int getMaxStackSize() {
                        return Integer.MAX_VALUE;
                    }
                });
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
