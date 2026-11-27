package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.common.menu.CompressedChestMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 压缩箱界面。
 */
public class CompressedChestScreen extends BaseContainerScreen<CompressedChestMenu> {
    public CompressedChestScreen(CompressedChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ScreenTextures.GENERIC_243, 500, 113 + menu.getRowCount() * 18, 500, 275);
        this.inventoryLabelX = 170;
        this.inventoryLabelY = this.imageHeight - 95;
    }
}
