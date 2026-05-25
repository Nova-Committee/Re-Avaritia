package com.avaritia.client.screen.craft;

import com.avaritia.Res;
import com.avaritia.api.client.screen.BaseContainerScreen;
import com.avaritia.common.menu.TierCraftMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NetherCraftScreen extends BaseContainerScreen<TierCraftMenu> {
    public NetherCraftScreen(TierCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.NETHER_CRAFT_TEX, 176, 206);
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, this.playerInventoryTitle, 10, 112, 4210752, false);

    }
}
