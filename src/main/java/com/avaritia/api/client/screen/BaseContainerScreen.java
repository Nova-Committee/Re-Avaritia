package com.avaritia.api.client.screen;

import com.avaritia.api.iface.IDataReceiver;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements ContainerListener, IDataReceiver {
    protected final Identifier bgTexture;
    protected final int bgImgWidth;
    protected final int bgImgHeight;

    public BaseContainerScreen(T container, Inventory inventory, Component title) {
        this(container, inventory, title, null);
    }

    public BaseContainerScreen(T container, Inventory inventory, Component title, Identifier bgTexture) {
        this(container, inventory, title, bgTexture, 176, 166, 256, 256);
    }

    public BaseContainerScreen(T container, Inventory inventory, Component title, Identifier bgTexture, int bgWidth, int bgHeight) {
        this(container, inventory, title, bgTexture, bgWidth, bgHeight, 256, 256);
    }

    public BaseContainerScreen(T container, Inventory inventory, Component title, Identifier bgTexture, int bgWidth, int bgHeight, int bgImgWidth, int bgImgHeight) {
        super(container, inventory, title, bgWidth, bgHeight);
        this.bgTexture = bgTexture;
        this.bgImgWidth = bgImgWidth;
        this.bgImgHeight = bgImgHeight;
    }

    protected static String number(Object number) {
        return NumberFormat.getInstance().format(number);
    }

    protected static String fraction(Object number) {
        DecimalFormat df = new DecimalFormat("0.00%");
        return df.format(number);
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
    public void extractContents(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractContents(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderFg(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    protected void renderFg(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY) {
        this.renderLabels(pGuiGraphics, pMouseX, pMouseY);
    }

    protected void renderLabels(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY) {
        super.extractLabels(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.bgTexture != null) pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.bgTexture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.bgImgWidth, this.bgImgHeight);
        this.renderBgs(pGuiGraphics, pPartialTick, this.leftPos, this.topPos);
    }

    protected void renderBgs(GuiGraphicsExtractor pGuiGraphics, float pPartialTick, int pX, int pY){
    }

    public void dataChanged(@NotNull AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
    }

    public void slotChanged(@NotNull AbstractContainerMenu pContainerToSend, int pSlotInd, @NotNull ItemStack pStack) {
    }

    @Override
    public void receive(CompoundTag tag) {
    }
}
