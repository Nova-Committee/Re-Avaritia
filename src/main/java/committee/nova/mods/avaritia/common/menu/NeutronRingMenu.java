package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.util.InventoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

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
                int yPos = 8 + row * 18;
                this.addSlot(new SlotItemHandler(h, j, xPos, yPos));
            }
        });
        int i, j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 39 + j * 18, 196 + i * 18));
            }
        }

        for (j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 39 + j * 18, 254));
        }
    }

}
