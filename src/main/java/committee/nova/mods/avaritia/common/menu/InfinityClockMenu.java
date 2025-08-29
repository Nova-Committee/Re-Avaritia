package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InfinityClockMenu extends AbstractContainerMenu {
    private final Container container;
    public InfinityClockMenu(int id, Inventory playerInventory) {
        super(MenuType.GENERIC_9x1, id);
        this.container = new SimpleContainer(0);


        int slotIndex = 9;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, slotIndex, 8 + col * 18, 84 + row * 18));
                slotIndex++;
            }
        }

        // 添加玩家热键栏 (0~8)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
