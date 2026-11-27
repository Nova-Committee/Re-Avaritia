package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.client.screen.side.SideConfigButton;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NeutronCollectorScreen extends BaseContainerScreen<NeutronCollectorMenu> {
    private Button configButton;

    public NeutronCollectorScreen(NeutronCollectorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.NEUTRON_COLLECTOR_TEX);
    }

    private ScreenRectangle progressBounds() {
        return new ScreenRectangle(this.leftPos + 99, this.topPos + 31, 4, 18);
    }

    @Override
    protected void init() {
        super.init();
        this.configButton = UiInspector.name(new SideConfigButton(this, this.leftPos - 20, this.topPos), "collector.config");
        this.addRenderableWidget(this.configButton);
    }

    @Override
    protected void renderFg(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        ScreenRectangle progress = progressBounds();
        UiInspector.region("collector.progress", progress, null, true);
        if (PortableLayout.contains(progress, pMouseX, pMouseY) && this.getProgress() > 0) {
            double i = (double) getProgress() / getTimeRequired();
            pGuiGraphics.setComponentTooltipForNextFrame(font, List.of(ModTooltips.PROGRESS.args(fraction(i)).build()), pMouseX, pMouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphicsExtractor stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();
        stack.text(font, title, (176 / 2 - this.font.width(title) / 2), 6, LABEL_COLOR, false);
        stack.text(font, this.playerInventoryTitle, 8, 166 - 94, LABEL_COLOR, false);
    }

    @Override
    protected void renderBgs(GuiGraphicsExtractor pGuiGraphics, float pPartialTick, int pX, int pY) {
        ScreenRectangle progress = progressBounds();
        if (this.getProgress() > 0) {
            int i2 = this.getProgressBarScaled(progress.height());
            pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, Res.NEUTRON_COLLECTOR_TEX,
                    progress.left(), progress.bottom() - i2, 176, 18 - i2, progress.width(), i2, 256, 256);
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
