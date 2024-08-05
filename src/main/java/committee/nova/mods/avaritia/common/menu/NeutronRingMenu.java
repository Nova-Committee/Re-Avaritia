package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.util.MenuUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/6 上午12:38
 * @Description:
 */
public class NeutronRingMenu extends BaseMenu {
    private final Player player;
    public ItemStack ring = ItemStack.EMPTY;
    public int slot;

    public static NeutronRingMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        return new NeutronRingMenu(ModMenus.neutron_ring.get(), windowId, playerInventory, buf);
    }

    public static NeutronRingMenu create(int windowId, Inventory playerInventory, BlockPos pos, int slot) {
        return new NeutronRingMenu(ModMenus.neutron_ring.get(), windowId, playerInventory, pos, slot);
    }

    private NeutronRingMenu(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(type, id, playerInventory, buf.readBlockPos(), buf.readInt());
    }

    public NeutronRingMenu(MenuType<?> type, int id, Inventory playerInventory, BlockPos pos, int slot) {
        super(type, id, pos);
        this.slot = slot;
        this.player = playerInventory.player;
        int i, j;
        if (slot > -1) {
            this.ring = playerInventory.getItem(slot);
        }
        if (ring.isEmpty()) {
            this.ring = MenuUtils.findInInventory(player, playerInventory, ModItems.neutron_ring.get());
        }
        ring.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            //todo
        });
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 39 + j * 18, 196 + i * 18));
            }
        }

        for (j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 39 + j * 18, 254));
        }
    }
    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

}
