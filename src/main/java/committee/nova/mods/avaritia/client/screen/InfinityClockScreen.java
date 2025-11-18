package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.InfinityClockMenu;
import committee.nova.mods.avaritia.common.net.C2SSetTimePacket;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class InfinityClockScreen extends BaseContainerScreen<InfinityClockMenu> {
    private int guiLeft, guiTop;
    private final int imageWidth = 176;
    private final int imageHeight = 166;

    private EditBox timeInput;

    public InfinityClockScreen(InfinityClockMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.INFINITY_CLOCK_TIME_TEX, 176, 166);
    }

    @Override
    protected void subInit() {
        super.subInit();
        guiLeft = (this.width - imageWidth) / 2;
        guiTop = (this.height - imageHeight) / 2;

        int buttonW = 22;
        int buttonH = 24;
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
    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        timeInput.render(pGuiGraphics, pX, pY, pPartialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (timeInput.isFocused() && keyCode == 257) { // 回车键
            try {
                int time = Integer.parseInt(timeInput.getValue());
                // 发送网络包到服务端
                PacketDistributor.sendToServer(new C2SSetTimePacket(time));
                return true;
            } catch (NumberFormatException e) {
                // 输入不是有效数字，忽略
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShaderTexture(0, Res.INFINITY_CLOCK_TIME_TEX);
            if (isHovered) {
                int hoverU = 177 + (index % 2) * (w + 1);
                int hoverV = 1 + (index / 2) * (h + 1);
                g.blit(Res.INFINITY_CLOCK_TIME_TEX, getX(), getY(), hoverU, hoverV, w, h);
            } else {
                g.blit(Res.INFINITY_CLOCK_TIME_TEX, getX(), getY(), texU, texV, w, h);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            // 发送网络包到服务端
            PacketDistributor.sendToServer(new C2SSetTimePacket(timeValue));
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
