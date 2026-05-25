package com.avaritia.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 26.1.2-compatible base screen for Avaritia container GUIs.
 */
public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
    protected final Identifier bgTexture;
    protected final int bgImgWidth;
    protected final int bgImgHeight;

    public BaseContainerScreen(T menu, Inventory inventory, Component title, Identifier bgTexture) {
        this(menu, inventory, title, bgTexture, 176, 166, 256, 256);
    }

    public BaseContainerScreen(T menu, Inventory inventory, Component title, Identifier bgTexture, int bgWidth, int bgHeight) {
        this(menu, inventory, title, bgTexture, bgWidth, bgHeight, 256, 256);
    }

    public BaseContainerScreen(T menu, Inventory inventory, Component title, Identifier bgTexture, int bgWidth, int bgHeight, int bgImgWidth, int bgImgHeight) {
        super(menu, inventory, title, bgWidth, bgHeight);
        this.bgTexture = bgTexture;
        this.bgImgWidth = bgImgWidth;
        this.bgImgHeight = bgImgHeight;
    }

    protected void subInit() {
    }

    @Override
    protected void init() {
        super.init();
        this.subInit();
        this.menu.addSlotListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        if (this.bgTexture != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, this.bgTexture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.bgImgWidth, this.bgImgHeight);
        }
        this.extractBgs(graphics, partialTick, this.leftPos, this.topPos);
    }

    protected void extractBgs(GuiGraphicsExtractor graphics, float partialTick, int x, int y) {
    }

    protected void extractFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        this.extractFg(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void dataChanged(@NotNull AbstractContainerMenu container, int dataSlotIndex, int value) {
    }

    @Override
    public void slotChanged(@NotNull AbstractContainerMenu container, int slotIndex, @NotNull ItemStack stack) {
    }
}
