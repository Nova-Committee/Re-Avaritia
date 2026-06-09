package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.common.menu.InfinityClockMenu;
import committee.nova.mods.avaritia.common.net.C2SSetTimePacket;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class InfinityClockScreen extends BaseContainerScreen<InfinityClockMenu> {
    private int guiLeft, guiTop;
    private static final int CLOCK_IMAGE_WIDTH = 176;
    private static final int CLOCK_IMAGE_HEIGHT = 166;

    private EditBox timeInput;

    public InfinityClockScreen(InfinityClockMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, ScreenTextures.INFINITY_CLOCK_TIME, 176, 166);
    }

    @Override
    protected void subInit() {
        super.subInit();
        guiLeft = (this.width - CLOCK_IMAGE_WIDTH) / 2;
        guiTop = (this.height - CLOCK_IMAGE_HEIGHT) / 2;

        int buttonW = 22;
        int spacing = 2;
        int startX = guiLeft + 17;
        int startY = guiTop + 22;

        // 修复按钮点击事件，添加玩家参数
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 0, startY, 17, 22, 0, 0));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 1, startY, 41, 22, 1, 6000));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 2, startY, 65, 22, 2, 12000));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 3, startY, 89, 22, 3, 14000));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 4, startY, 113, 22, 4, 18000));
        addRenderableWidget(new TimeButton(startX + (buttonW + spacing) * 5, startY, 137, 22, 5, 22000));
        this.titleLabelX = 62;
        timeInput = new EditBox(this.font, guiLeft + 38, guiTop + 52, 113, 10, Component.literal(""));
        timeInput.setMaxLength(10);
        addRenderableWidget(timeInput);
    }

    @Override
    protected void extractBgs(GuiGraphicsExtractor graphics, float partialTick, int x, int y) {
        timeInput.extractRenderState(graphics, x, y, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (timeInput.isFocused() && event.isConfirmation()) {
            try {
                int time = Integer.parseInt(timeInput.getValue());
                // 发送网络包到服务端
                NetworkHandler.sendToServer(new C2SSetTimePacket(time));
                return true;
            } catch (NumberFormatException ignored) {
                // 输入不是有效数字，忽略
            }
        }
        return super.keyPressed(event);
    }

    // 自定义按钮
    static class TimeButton extends AbstractWidget {
        private final int texU, texV;
        private final int index;
        private final int timeValue; // 存储时间值
        private final int w = 22;
        private final int h = 24;

        public TimeButton(int x, int y, int texU, int texV, int index, int timeValue) {
            super(x, y, 22, 24, Component.empty());
            this.texU = texU;
            this.texV = texV;
            this.index = index;
            this.timeValue = timeValue;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            if (isHovered()) {
                int hoverU = 177 + (index % 2) * (w + 1);
                int hoverV = 1 + (index / 2) * (h + 1);
                graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.INFINITY_CLOCK_TIME, getX(), getY(), hoverU, hoverV, w, h, 256, 256);
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.INFINITY_CLOCK_TIME, getX(), getY(), texU, texV, w, h, 256, 256);
            }
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            // 发送网络包到服务端
            NetworkHandler.sendToServer(new C2SSetTimePacket(timeValue));
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
