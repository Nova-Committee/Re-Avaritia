package com.avaritia.client.screen;

import com.avaritia.Res;
import com.avaritia.api.client.screen.BaseContainerScreen;
import com.avaritia.client.screen.side.SideConfigButton;
import com.avaritia.common.menu.NeutronCollectorMenu;
import com.avaritia.init.registry.ModTooltips;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NeutronCollectorScreen extends BaseContainerScreen<NeutronCollectorMenu> {
    private Button configButton;

    public NeutronCollectorScreen(NeutronCollectorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.NEUTRON_COLLECTOR_TEX);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        this.configButton = new SideConfigButton(this, x - 20, y);

        this.addRenderableWidget(this.configButton);
    }

    @Override
    protected void renderFg(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = this.leftPos;
        int y = this.topPos;

        if (pMouseX > x + 99 && pMouseX < x + 104 && pMouseY > y + 30 && pMouseY < y + 50) {
            List<Component> tooltip = new ArrayList<>();

            if (this.getProgress() > 0) {
                double i = (double) getProgress() / getTimeRequired();
                var text = ModTooltips.PROGRESS.args(fraction(i)).build();
                tooltip.add(text);
            }

            pGuiGraphics.renderComponentTooltip(font, tooltip, pMouseX, pMouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphicsExtractor stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, title, (176 / 2 - this.font.width(title) / 2), 6, 4210752, false);
        stack.drawString(font, this.playerInventoryTitle, 8, 166 - 94, 4210752, false);
    }

    @Override
    protected void renderBgs(GuiGraphicsExtractor pGuiGraphics, float pPartialTick, int pX, int pY) {
        int i = this.leftPos;
        int j = this.topPos;
        if (this.getProgress() > 0) {
            int i2 = this.getProgressBarScaled(18);
            pGuiGraphics.blit(Res.NEUTRON_COLLECTOR_TEX, i + 99, j + 49 - i2, 176, 18 - i2, 4, i2);
        }
    }

    public int getProgress() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getProgress();
    }

    public int getTimeRequired() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getTileEntity().getProductionTicks();
    }

    public int getProgressBarScaled(int pixels) {
        int i = Mth.clamp(this.getProgress(), 0, this.getTimeRequired());
        int j = this.getTimeRequired();
        return (int) (j != 0 && i != 0 ? (long) i * pixels / j : 0);
    }
}
