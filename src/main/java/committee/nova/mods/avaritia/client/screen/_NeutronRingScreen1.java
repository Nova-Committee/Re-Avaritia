package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu._NeutronRingMenu;
import committee.nova.mods.avaritia.common.net.C2SChangePagePack;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/17 02:50
 * @Description:
 */
public class _NeutronRingScreen1 extends BaseContainerScreen<_NeutronRingMenu> {
    private static final ResourceLocation MULTI_PAGE_TEXTURE = Const.rl("textures/gui/infinity_chest.png");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");

    private final int inventoryRows;

    public _NeutronRingScreen1(_NeutronRingMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, MULTI_PAGE_TEXTURE);
        this.inventoryRows = ModConfig.inventoryRows.get();
        this.imageHeight = 132 + this.inventoryRows * 18;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    @Override
    protected void subInit() {
        this.addRenderableWidget(new ImageButton(this.leftPos + 121, this.topPos + 6, 11, 11, 187, 22, 11, MULTI_PAGE_TEXTURE, (button) -> NetworkHandler.CHANNEL.sendToServer(new C2SChangePagePack((this.menu).getCurrentPage() - 10))));
        this.addRenderableWidget(new ImageButton(this.leftPos + 134, this.topPos + 6, 7, 11, 183, 0, 11, MULTI_PAGE_TEXTURE, (button) -> NetworkHandler.CHANNEL.sendToServer(new C2SChangePagePack((this.menu).getCurrentPage() - 1))));
        this.addRenderableWidget(new ImageButton(this.leftPos + 149, this.topPos + 6, 7, 11, 176, 0, 11, MULTI_PAGE_TEXTURE, (button) -> NetworkHandler.CHANNEL.sendToServer(new C2SChangePagePack((this.menu).getCurrentPage() + 1))));
        this.addRenderableWidget(new ImageButton(this.leftPos + 158, this.topPos + 6, 11, 11, 176, 22, 11, MULTI_PAGE_TEXTURE, (button) -> NetworkHandler.CHANNEL.sendToServer(new C2SChangePagePack((this.menu).getCurrentPage() + 10))));
    }

    @Override
    protected void renderBgOthers(GuiGraphics pGuiGraphics, int pX, int pY) {
        pGuiGraphics.blit(MULTI_PAGE_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.inventoryRows * 18 + 35);
        pGuiGraphics.blit(MULTI_PAGE_TEXTURE, this.leftPos, this.topPos + this.inventoryRows * 18 + 35, 0, 143, this.imageWidth, 97);
        int index = this.menu.getSwapIndex();
//        Slot slot = index >= 0 && index < this.menu.slots.size() ? this.menu.getSlot(index) : null;
//        if (slot != null) {
//            RenderSystem.disableDepthTest();
//            int xPos = this.leftPos + slot.x;
//            int yPos = this.topPos + slot.y;
//            RenderSystem.colorMask(true, true, true, false);
//            pGuiGraphics.fillGradient(xPos, yPos, xPos + 16, yPos + 16, -2130771968, -2130771968);
//            RenderSystem.colorMask(true, true, true, true);
//            RenderSystem.enableDepthTest();
//        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY);
        String page = (this.menu).getCurrentPage() + 1 + " / ";
        int pageWidth = this.font.width(page);
        pGuiGraphics.drawString(font, page, 169 - 20 - pageWidth, 24, 4210752, false);
        pGuiGraphics.drawString(font, "∞", 169 - 20, 24, 4210752, false);
    }


//    @Override
//    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
//        if (Stream.of(SORT_KEYS).anyMatch(key -> key.matches(pKeyCode, pScanCode))){
//            for (int i = 0; i < SORT_KEYS.length; i++) {
//               if (SORT_KEYS[i].matches(pKeyCode, pScanCode)) {
//                   super.slotClicked(null, 0, i, ClickType.CLONE);
//                   return true;
//               }
//            }
//            return false;
//        }
//        else {
//            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
//        }
//    }

    @Override
    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //this.renderSlotCount(pGuiGraphics);
    }

    public void renderSlotCount(GuiGraphics pGuiGraphics) {
        PoseStack poseStack = pGuiGraphics.pose();
        for (int i = 0; i < menu.slots.size(); i++) {
            long count = menu.getItemCount(i);
            float fontSize = 0.5F;
            if (count != 0L) {
                String stringCount = String.valueOf(count);
                if (count > 1000L) {
                    stringCount = DECIMAL_FORMAT.format(count);
                    stringCount = stringCount.substring(0, 4);
                    if (stringCount.endsWith(",")) stringCount = stringCount.substring(0, 3);
                    stringCount = stringCount.replace(",", ".");
                    if (count < 1000000L) stringCount += "K";
                    else if (count < 1000000000L) stringCount += "M";
                    else if (count < 1000000000000L) stringCount += "G";
                    else if (count < 1000000000000000L) stringCount += "T";
                    else if (count < 1000000000000000000L) stringCount += "P";
                    else stringCount += "E";
                }
                RenderSystem.enableDepthTest();
                poseStack.pushPose();
                poseStack.translate(leftPos + menu.getSlot(i).x, topPos + menu.getSlot(i).y, 300.0D);
                poseStack.scale(fontSize, fontSize, 1.0F);
                pGuiGraphics.drawString(this.font, stringCount,
                        (int) ((16 - this.font.width(stringCount) * fontSize) / fontSize),
                        (int) ((16 - this.font.lineHeight * fontSize) / fontSize),
                        16777215);
                poseStack.popPose();
            }
        }
    }

//    @Override
//    protected void slotClicked(@Nullable Slot slotIn, int slotId, int mouseButton, @NotNull ClickType type) {
//        if (type == ClickType.CLONE) {
//            super.slotClicked(null, slotId, 1, type);
//        } else {
//            if (slotIn != null) {
//                if (type == ClickType.PICKUP) {
//                    if (hasAltDown()) {
//                        super.slotClicked(slotIn, slotId, 2, type);
//                        return;
//
//                    } else if (hasControlDown() && (Objects.equals(slotIn.container, this.menu.getPlayerInventory()))) {
//                        super.slotClicked(slotIn, slotId, 3, type);
//                        return;
//                    }
//                } else if (type == ClickType.QUICK_MOVE && (Objects.equals(slotIn.container, this.menu.getPlayerInventory())) && hasControlDown()) {
//                    super.slotClicked(slotIn, slotId, 2, type);
//                    return;
//                }
//            }
//
//            if (slotIn != null) super.slotClicked(slotIn, slotId, mouseButton, type);
//        }
//    }
}
