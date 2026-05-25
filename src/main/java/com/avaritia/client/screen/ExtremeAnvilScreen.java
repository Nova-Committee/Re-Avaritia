package com.avaritia.client.screen;

import com.avaritia.common.menu.ExtremeAnvilMenu;
import com.avaritia.network.C2SRenamePack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * 极限铁砧界面。
 */
public class ExtremeAnvilScreen extends BaseContainerScreen<ExtremeAnvilMenu> {
    private EditBox name;

    public ExtremeAnvilScreen(ExtremeAnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ScreenTextures.EXTREME_ANVIL);
        this.titleLabelX = 60;
    }

    @Override
    protected void subInit() {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.name = new EditBox(this.font, i + 62, j + 28, 103, 12, Component.translatable("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addWidget(this.name);
        this.setInitialFocus(this.name);
        this.name.setEditable(false);
    }

    @Override
    public void resize(int width, int height) {
        String value = this.name != null ? this.name.getValue() : "";
        super.resize(width, height);
        if (this.name != null) {
            this.name.setValue(value);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            this.minecraft.player.closeContainer();
        }

        return this.name.keyPressed(event) || this.name.canConsumeInput() || super.keyPressed(event);
    }

    private void onNameChanged(String name) {
        Slot slot = this.menu.getSlot(0);
        if (slot.hasItem()) {
            String value = name;
            if (name.equals(slot.getItem().getHoverName().getString())) {
                value = "";
            }

            if (this.menu.setItemName(value)) {
                PacketDistributor.sendToServer(new C2SRenamePack(value));
            }
        }
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.EXTREME_ANVIL, this.leftPos + 59, this.topPos + 23, 0.0F, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0.0F : 16.0F), 110, 16, 256, 256);
    }

    @Override
    protected void extractFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.name.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractBgs(@NotNull GuiGraphicsExtractor graphics, float partialTick, int x, int y) {
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(this.menu.getResultSlot()).hasItem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.EXTREME_ANVIL, x + 99, y + 47, this.imageWidth, 0.0F, 28, 21, 256, 256);
        }
    }

    @Override
    public void slotChanged(@NotNull AbstractContainerMenu container, int slotIndex, @NotNull ItemStack stack) {
        if (slotIndex == 0) {
            this.name.setValue(stack.isEmpty() ? "" : stack.getHoverName().getString());
            this.name.setEditable(!stack.isEmpty());
            this.setFocused(this.name);
        }
    }
}
