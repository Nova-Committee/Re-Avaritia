package com.avaritia.client.screen.craft;

import com.avaritia.Res;
import com.avaritia.api.client.screen.BaseContainerScreen;
import com.avaritia.common.menu.TierCraftMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SculkCraftScreen extends BaseContainerScreen<TierCraftMenu> {
    public SculkCraftScreen(TierCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.SCULK_CRAFT_TEX, 176, 167);
    }

    @Override
    protected void renderLabels(GuiGraphicsExtractor stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, this.playerInventoryTitle, 10, 72, 4210752, false);
    }
}
