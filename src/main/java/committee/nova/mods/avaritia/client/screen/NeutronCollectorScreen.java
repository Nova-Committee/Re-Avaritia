package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.common.tile.NeutronCollectorTile;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 15:12
 * Version: 1.0
 */
public class NeutronCollectorScreen extends BaseContainerScreen<NeutronCollectorMenu> {
    private Button configButton;

    public NeutronCollectorScreen(NeutronCollectorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.NEUTRON_COLLECTOR_TEX);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        // 添加配置按钮
        this.configButton = new ConfigButton(this, x - 20, y);

        this.addRenderableWidget(this.configButton);
    }

    @Override
    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

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
    protected void renderLabels(@NotNull GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, title, (176 / 2 - this.font.width(title) / 2), 6, 4210752, false);
        stack.drawString(font, this.playerInventoryTitle, 8, 166 - 94, 4210752, false);
    }

    @Override
    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        int i = this.getGuiLeft();
        int j = this.getGuiTop();
        if (this.getProgress() > 0) {
            int i2 = this.getProgressBarScaled(18);
            pGuiGraphics.blit(Res.NEUTRON_COLLECTOR_TEX, i + 99, j + 49 - i2, 176, 18 - i2, 4, i2);
        }
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
