package com.avaritia.client.screen.craft;

import com.avaritia.Res;
import com.avaritia.api.client.screen.BaseContainerScreen;
import com.avaritia.common.menu.TierCraftMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class EndCraftScreen extends BaseContainerScreen<TierCraftMenu> {

    public EndCraftScreen(TierCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.END_CRAFT_TEX, 200, 242);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, this.playerInventoryTitle, 22, 148, 4210752, false);
    }
}
