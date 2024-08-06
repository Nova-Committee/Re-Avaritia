package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.CompressedChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 上午11:32
 * @Description:
 */
public class CompressedChestScreen extends BaseContainerScreen<CompressedChestMenu> implements MenuAccess<CompressedChestMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = Static.rl("textures/gui/generic_243.png");
    protected int imageWidth = 500;

    public CompressedChestScreen(CompressedChestMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, CONTAINER_BACKGROUND, 500, 276, 500, 276);
        int containerRows = pMenu.getRowCount();
        this.imageHeight = 114 + containerRows * 18;
        this.inventoryLabelX = 170;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        renderDefaultBg(pGuiGraphics);
    }
}
