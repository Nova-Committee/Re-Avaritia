package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.common.menu.InfinityClockMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InfinityClockScreen extends AbstractContainerScreen<InfinityClockMenu> {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation("avaritia", "textures/gui/infinity_clock_time.png");

    private int guiLeft, guiTop;
    private final int imageWidth = 176;
    private final int imageHeight = 166;

    private EditBox timeInput;

    public InfinityClockScreen(InfinityClockMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (this.width - imageWidth) / 2;
        guiTop = (this.height - imageHeight) / 2;

        int buttonW = 22;
        int buttonH = 24;
        int spacing = 2;
        int startX = guiLeft + 17;
        int startY = guiTop + 22;

        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 0, startY,17, 22, 0, b -> setTime(0)));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 1, startY, 41, 22, 1, b -> setTime(6000)));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 2, startY, 65, 22, 2, b -> setTime(12000)));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 3, startY, 89, 22, 3, b -> setTime(14000)));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 4, startY, 113, 22, 4, b -> setTime(18000)));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 5, startY, 137, 22, 5, b -> setTime(22000)));

        timeInput = new EditBox(this.font, guiLeft + 38, guiTop + 52, 113, 10, Component.literal(""));
        timeInput.setMaxLength(10);
        addRenderableWidget(timeInput);
    }

    private void setTime(int time) {
        if (this.minecraft != null && this.minecraft.level != null) {
            this.minecraft.level.setDayTime(time);
        }
    }

@Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTicks);
        timeInput.render(g, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        // 绘制背景图（左上角位置 leftPos, topPos）
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (timeInput.isFocused() && keyCode == 257) {
            try {
                int customTime = Integer.parseInt(timeInput.getValue());
                if (this.minecraft != null && this.minecraft.level != null) {
                    this.minecraft.level.setDayTime(customTime);
                }
            } catch (NumberFormatException ignored) {}
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // 自定义按钮
    class TimeButton extends AbstractWidget {
        private final int texU, texV;
        private final int index;
        private final OnPress onPress;
        private final int w = 22;
        private final int h = 24;

        public TimeButton(int x, int y, int texU, int texV, int index, OnPress press) {
            super(x, y, 22, 24, Component.empty());
            this.texU = texU;
            this.texV = texV;
            this.index = index;
            this.onPress = press;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            if (isHovered) {
                int hoverU = 177 + (index % 2) * (w + 1);
                int hoverV = 1 + (index / 2) * (h + 1);
                g.blit(TEXTURE, getX(), getY(), hoverU, hoverV, w, h);
            } else {
                g.blit(TEXTURE, getX(), getY(), texU, texV, w, h);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.onPress.onPress(this);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        public interface OnPress {
            void onPress(TimeButton btn);
        }
    }
}
