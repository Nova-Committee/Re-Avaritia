package nova.committee.avaritia.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.api.client.screen.BaseContainerScreen;
import nova.committee.avaritia.common.menu.ExtremeCraftingMenu;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:40
 * Version: 1.0
 */
public class ExtremeCraftingScreen extends BaseContainerScreen<ExtremeCraftingMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Static.MOD_ID, "textures/gui/extreme_crafting_gui.png");

    public ExtremeCraftingScreen(ExtremeCraftingMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 234, 278, 512, 512);
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, title, 8, 6, 4210752, false);
        stack.drawString(font, this.playerInventoryTitle, 39, this.imageHeight - 94, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics stack, float partialTicks, int mouseX, int mouseY) {
        this.renderDefaultBg(stack, partialTicks, mouseX, mouseY);

    }
}
