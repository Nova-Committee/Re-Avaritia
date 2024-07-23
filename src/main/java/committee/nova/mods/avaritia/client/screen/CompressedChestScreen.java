package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Static;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.jetbrains.annotations.NotNull;

/**
 * Project: Avaritia
 * Author: cnlimiter
 * CreateTime: 2024/7/13 上午11:32
 * Description:
 */
public final class CompressedChestScreen extends AbstractContainerScreen<ChestMenu> implements MenuAccess<ChestMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = Static.rl("textures/gui/generic_81.png");

    public CompressedChestScreen(ChestMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        int containerRows = pMenu.getRowCount();
        this.imageHeight = 114 + containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }


    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(CONTAINER_BACKGROUND, i, j, 0, 0, 184, 276, 256, 276);
    }
}
