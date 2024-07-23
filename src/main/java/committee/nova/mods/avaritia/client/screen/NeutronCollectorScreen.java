package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.common.tile.collector.BaseNeutronCollectorTile;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
public final class NeutronCollectorScreen extends BaseContainerScreen<NeutronCollectorMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Static.MOD_ID, "textures/gui/neutron_collector.png");
    private BaseNeutronCollectorTile tile;

    public NeutronCollectorScreen(NeutronCollectorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 176, 166, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        this.tile = this.getTileEntity();
    }

    private BaseNeutronCollectorTile getTileEntity() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var tile = level.getBlockEntity(this.getMenu().getBlockPos());

            if (tile instanceof BaseNeutronCollectorTile compressor)
                return compressor;
        }

        return null;
    }

    @Override
    public void render(@NotNull GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        if (mouseX > x + 99 && mouseX < x + 104 && mouseY > y + 30 && mouseY < y + 50) {
            List<Component> tooltip = new ArrayList<>();

            if (this.getProgress() > 0) {
                double i = (double) getProgress() / getTimeRequired();
                var text = ModTooltips.PROGRESS.args(fraction(i)).build();
                tooltip.add(text);
            }

            stack.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, title, (176 / 2 - this.font.width(title) / 2), 6, 4210752, false);
        stack.drawString(font, this.playerInventoryTitle, 8, 166 - 94, 4210752, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics stack, float pPartialTick, int pMouseX, int pMouseY) {
        int i = this.getGuiLeft();
        int j = this.getGuiTop();
        stack.blit(BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.bgImgWidth, this.bgImgHeight);
        if (this.getProgress() > 0) {
            int i2 = this.getProgressBarScaled(18);
            stack.blit(BACKGROUND, i + 99, j + 49 - i2, 176, 18 - i2, 4, i2);
        }
    }

    public int getProgress() {
        if (this.tile == null)
            return 0;

        return this.menu.getProgress();//data by menu
    }

    public int getTimeRequired() {
        if (this.tile == null)
            return 0;

        return this.tile.getProductionTicks();// final data will not use menu
    }

    public int getProgressBarScaled(int pixels) {
        int i = Mth.clamp(this.getProgress(), 0, this.getTimeRequired());
        int j = this.getTimeRequired();
        return (int) (j != 0 && i != 0 ? (long) i * pixels / j : 0);
    }
}
