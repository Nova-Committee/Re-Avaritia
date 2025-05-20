package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.CompressorMenu;
import committee.nova.mods.avaritia.common.tile.CompressorTile;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.ChatFormatting;
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
 * Date: 2022/4/2 18:16
 * Version: 1.0
 */
public class CompressorScreen extends BaseContainerScreen<CompressorMenu> {
    private static final ResourceLocation BACKGROUND = Const.rl("textures/gui/compressor.png");

    public CompressorScreen(CompressorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND);
    }

    @Override
    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        if (pMouseX > x + 63 && pMouseX < x + 79 && pMouseY > y + 35 && pMouseY < y + 51) {
            List<Component> tooltip = new ArrayList<>();

            if (this.getMaterialCount() < 1) {
                tooltip.add(ModTooltips.EMPTY.color(ChatFormatting.WHITE).build());
            } else {
                if (this.hasMaterialStack()) {
                    tooltip.add(this.getMaterialStackDisplayName());
                }

                var text = Component.literal(number(this.getMaterialCount()) + " / " + number(this.getMaterialsRequired()));

                tooltip.add(text);
            }

            pGuiGraphics.renderComponentTooltip(font, tooltip, pMouseX, pMouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();
        stack.drawString(font, title, (this.imageWidth / 2 - this.font.width(title) / 2), 6, 4210752, false);
        stack.drawString(font, this.playerInventoryTitle, 8, this.imageHeight - 94, 4210752, false);
    }


    @Override
    protected void renderBgOthers(GuiGraphics pGuiGraphics, int pX, int pY) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        if (this.hasRecipe()) {
            if (this.getMaterialCount() > 0 && this.getMaterialsRequired() > 0) {
                int i2 = this.getMaterialBarScaled(16);
                pGuiGraphics.blit(BACKGROUND, x + 63, y + 35, 176, 18, i2 + 1, 16);
            }

            if (this.getProgress() > 0 && this.getMaterialCount() >= this.getMaterialsRequired()) {
                int i2 = this.getProgressBarScaled(22);
                pGuiGraphics.blit(BACKGROUND, x + 89, y + 35, 176, 0, i2 + 1, 16);
            }
        }
    }

    private Component getMaterialStackDisplayName() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var container = this.getMenu();
            var tile = level.getBlockEntity(container.getBlockPos());

            if (tile instanceof CompressorTile compressor) {
                var materialStack = compressor.getMaterialStack();

                return materialStack.getHoverName();
            }
        }

        return Component.literal("");
    }

    private CompressorTile getTileEntity() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var tile = level.getBlockEntity(this.getMenu().getBlockPos());

            if (tile instanceof CompressorTile compressor)
                return compressor;
        }

        return null;
    }

    public boolean isEjecting() {
        if (this.menu.getTileEntity() == null)
            return false;

        return this.menu.getTileEntity().isEjecting();
    }


    public boolean hasRecipe() {
        if (this.menu.getTileEntity() == null)
            return false;

        return this.menu.getTileEntity().hasRecipe();
    }

    public boolean hasMaterialStack() {
        if (this.menu.getTileEntity() == null)
            return false;

        return this.menu.getTileEntity().hasMaterialStack();
    }

    public int getProgress() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getProgress();
    }

    public int getMaterialCount() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getTileEntity().getMaterialCount();
    }

    public int getMaterialsRequired() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getTileEntity().getMaterialsRequired();
    }

    public int getTimeRequired() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getTileEntity().getTimeRequired();
    }

    public int getMaterialBarScaled(int pixels) {
        int i = Mth.clamp(this.getMaterialCount(), 0, this.getMaterialsRequired());
        int j = this.getMaterialsRequired();
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

    public int getProgressBarScaled(int pixels) {
        int i = Mth.clamp(this.getProgress(), 0, this.getTimeRequired());
        int j = this.getTimeRequired();
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

}
