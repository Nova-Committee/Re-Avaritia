package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.client.screen.side.SideConfigButton;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:12
 * Version: 1.0
 */
public class NeutronCollectorScreen extends BaseContainerScreen<NeutronCollectorMenu> {
    private Button configButton;
    private ScreenRectangle progress;
    public NeutronCollectorScreen(NeutronCollectorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.NEUTRON_COLLECTOR_TEX);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        this.progress = PortableLayout.translate(new ScreenRectangle(99, 31, 4, 18), x, y);
        this.configButton = UiInspector.name(new SideConfigButton(this, x - 20, y), "collector.config");
        this.addRenderableWidget(this.configButton);
    }

    @Override
    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (PortableLayout.contains(this.progress, pMouseX, pMouseY)) {
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
    protected void renderLabels(@NotNull GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, title, (176 / 2 - this.font.width(title) / 2), 6, 4210752, false);
        stack.drawString(font, this.playerInventoryTitle, 8, 166 - 94, 4210752, false);
    }

    @Override
    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        if (this.getProgress() > 0) {
            int filled = this.getProgressBarScaled(this.progress.height());
            pGuiGraphics.blit(Res.NEUTRON_COLLECTOR_TEX, this.progress.left(), this.progress.bottom() - filled, 176, 18 - filled, this.progress.width(), filled);
        }
        UiInspector.region("collector.progress", this.progress, null, true);
    }

    public int getProgress() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getProgress();//data by menu
    }

    public int getTimeRequired() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getTileEntity().getProductionTicks();// final data will not use menu
    }

    public int getProgressBarScaled(int pixels) {
        int i = Mth.clamp(this.getProgress(), 0, this.getTimeRequired());
        int j = this.getTimeRequired();
        return (int) (j != 0 && i != 0 ? (long) i * pixels / j : 0);
    }
}
