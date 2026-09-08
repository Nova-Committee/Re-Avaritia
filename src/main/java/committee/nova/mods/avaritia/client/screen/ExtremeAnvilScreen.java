package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.menu.ExtremeAnvilMenu;
import committee.nova.mods.avaritia.common.net.C2SRenamePacket;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExtremeAnvilScreen extends BaseContainerScreen<ExtremeAnvilMenu> {
    private EditBox name;
    private ScreenRectangle nameBounds;
    private ScreenRectangle nameBackground;
    private ScreenRectangle errorIcon;


    public ExtremeAnvilScreen(ExtremeAnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ScreenTextures.EXTREME_ANVIL);
        this.titleLabelX = 60;
    }

    @Override
    protected void subInit() {
        String value = this.name == null ? "" : this.name.getValue();
        updateLayout();
        this.name = new EditBox(this.font, this.nameBounds.left(), this.nameBounds.top(), this.nameBounds.width(), this.nameBounds.height(), Component.translatable("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue(value);
        this.addWidget(UiInspector.name(this.name, "anvil.name"));
        this.setInitialFocus(this.name);
        this.name.setEditable(this.menu.getSlot(0).hasItem());
    }

    private void updateLayout() {
        this.nameBounds = PortableLayout.translate(new ScreenRectangle(62, 28, 103, 12), this.leftPos, this.topPos);
        this.nameBackground = PortableLayout.translate(new ScreenRectangle(59, 23, 110, 16), this.leftPos, this.topPos);
        this.errorIcon = PortableLayout.translate(new ScreenRectangle(99, 47, 28, 21), this.leftPos, this.topPos);
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
                NetworkHandler.sendToServer(new C2SRenamePacket(value));
            }
        }
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.EXTREME_ANVIL, this.nameBackground.left(), this.nameBackground.top(), 0.0F, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0.0F : 16.0F), this.nameBackground.width(), this.nameBackground.height(), 256, 256);
    }

    @Override
    protected void extractFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.name.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractBgs(@NotNull GuiGraphicsExtractor graphics, float partialTick, int x, int y) {
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(this.menu.getResultSlot()).hasItem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.EXTREME_ANVIL, this.errorIcon.left(), this.errorIcon.top(), this.imageWidth, 0.0F, this.errorIcon.width(), this.errorIcon.height(), 256, 256);
            UiInspector.region("anvil.error", this.errorIcon, null, false);
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
