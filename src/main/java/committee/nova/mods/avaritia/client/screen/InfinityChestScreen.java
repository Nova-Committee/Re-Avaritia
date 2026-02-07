package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 上午11:32
 * @Description:
 */
public class InfinityChestScreen extends BaseContainerScreen<InfinityChestMenu>{

    public InfinityChestScreen(InfinityChestMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, Res.INFINITY_CHEST_TEX, 500, 276, 500, 276);
        int containerRows = pMenu.getRowCount();
        this.imageHeight = 114 + containerRows * 18;
        this.inventoryLabelX = 170;
        this.inventoryLabelY = this.imageHeight - 94;
    }

}
