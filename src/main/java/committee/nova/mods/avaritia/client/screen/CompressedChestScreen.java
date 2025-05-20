package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.CompressedChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 上午11:32
 * @Description:
 */
public class CompressedChestScreen extends BaseContainerScreen<CompressedChestMenu>{
    private static final ResourceLocation CONTAINER_BACKGROUND = Const.rl("textures/gui/generic_243.png");

    public CompressedChestScreen(CompressedChestMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, CONTAINER_BACKGROUND, 500, 276, 500, 276);
        int containerRows = pMenu.getRowCount();
        this.imageHeight = 114 + containerRows * 18;
        this.inventoryLabelX = 170;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBgOthers(GuiGraphics pGuiGraphics, int pX, int pY) {

    }
}
