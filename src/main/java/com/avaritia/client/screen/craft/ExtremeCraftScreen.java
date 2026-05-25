package com.avaritia.client.screen.craft;

import com.avaritia.Res;
import com.avaritia.api.client.screen.BaseContainerScreen;
import com.avaritia.common.menu.TierCraftMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtremeCraftScreen extends BaseContainerScreen<TierCraftMenu> {
    public ExtremeCraftScreen(TierCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.EXTREME_CRAFT_TEX, 234, 278, 512, 512);
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, title, 8, 6, 4210752, false);
        stack.drawString(font, this.playerInventoryTitle, 39, this.imageHeight - 94, 4210752, false);
    }
}
