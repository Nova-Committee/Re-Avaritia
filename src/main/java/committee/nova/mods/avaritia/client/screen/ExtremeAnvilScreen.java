package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.menu.ExtremeAnvilMenu;
import committee.nova.mods.avaritia.common.net.C2SRenamePack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
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

    public ExtremeAnvilScreen(ExtremeAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, Res.EXTREME_ANVIL_TEX);
        this.titleLabelX = 60;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.name.tick();
    }

    @Override
    protected void subInit() {
        String value = this.name == null ? "" : this.name.getValue();
        this.updateLayout();
        this.name = new EditBox(this.font, this.nameBounds.left(), this.nameBounds.top(), this.nameBounds.width(), this.nameBounds.height(), Component.translatable("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setValue(value);
        this.name.setResponder(this::onNameChanged);
        this.addWidget(UiInspector.name(this.name, "anvil.name"));
        this.setInitialFocus(this.name);
        this.name.setEditable(this.menu.getSlot(0).hasItem());
    }

    private void updateLayout() {
        int originX = this.getGuiLeft();
        int originY = this.getGuiTop();
        this.nameBounds = PortableLayout.translate(new ScreenRectangle(62, 28, 103, 12), originX, originY);
        this.nameBackground = PortableLayout.translate(new ScreenRectangle(59, 23, 110, 16), originX, originY);
        this.errorIcon = PortableLayout.translate(new ScreenRectangle(99, 47, 28, 21), originX, originY);
    }

    @Override
    public void resize(@NotNull Minecraft pMinecraft, int pWidth, int pHeight) {
        String s = this.name.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.name.setValue(s);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256) {
            this.minecraft.player.closeContainer();
        }
        return this.name.keyPressed(pKeyCode, pScanCode, pModifiers) || this.name.canConsumeInput() || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    private void onNameChanged(String name) {
        Slot slot = this.menu.getSlot(0);
        if (slot.hasItem()) {
            String s = name;
            if (!slot.getItem().hasCustomHoverName() && name.equals(slot.getItem().getHoverName().getString())) {
                s = "";
            }
            if (this.menu.setItemName(s)) {
                NetworkHandler.CHANNEL.sendToServer(new C2SRenamePack(s));
            }
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        super.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
        pGuiGraphics.blit(Res.EXTREME_ANVIL_TEX, this.nameBackground.left(), this.nameBackground.top(), 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), this.nameBackground.width(), this.nameBackground.height());
    }

    @Override
    public void renderFg(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.name.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void renderBgs(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(this.menu.getResultSlot()).hasItem()) {
            pGuiGraphics.blit(Res.EXTREME_ANVIL_TEX, this.errorIcon.left(), this.errorIcon.top(), this.imageWidth, 0, this.errorIcon.width(), this.errorIcon.height());
            UiInspector.region("anvil.error", this.errorIcon, null, false);
        }
    }

    @Override
    public void slotChanged(@NotNull AbstractContainerMenu pContainerToSend, int pSlotInd, @NotNull ItemStack pStack) {
        if (pSlotInd == 0) {
            this.name.setValue(pStack.isEmpty() ? "" : pStack.getHoverName().getString());
            this.name.setEditable(!pStack.isEmpty());
            this.setFocused(this.name);
        }
    }
}
