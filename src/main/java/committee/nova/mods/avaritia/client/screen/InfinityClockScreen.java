package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.menu.InfinityClockMenu;
import committee.nova.mods.avaritia.common.net.C2SSetTimePacket;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class InfinityClockScreen extends BaseContainerScreen<InfinityClockMenu> {

    private EditBox timeInput;

    public InfinityClockScreen(InfinityClockMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.INFINITY_CLOCK_TIME_TEX, 176, 166);
    }

    @Override
    protected void subInit() {
        super.subInit();
        int originX = this.getGuiLeft();
        int originY = this.getGuiTop();
        int buttonW = 22;
        int spacing = 2;
        ScreenRectangle firstPreset = PortableLayout.translate(new ScreenRectangle(17, 22, 22, 24), originX, originY);
        ScreenRectangle input = PortableLayout.translate(new ScreenRectangle(38, 52, 113, 10), originX, originY);

        addRenderableWidget(UiInspector.name(new TimeButton(firstPreset.left() + (buttonW + spacing) * 0, firstPreset.top(), 17, 22, 0, 0), "clock.preset.0"));
        addRenderableWidget(UiInspector.name(new TimeButton(firstPreset.left() + (buttonW + spacing) * 1, firstPreset.top(), 41, 22, 1, 6000), "clock.preset.6000"));
        addRenderableWidget(UiInspector.name(new TimeButton(firstPreset.left() + (buttonW + spacing) * 2, firstPreset.top(), 65, 22, 2, 12000), "clock.preset.12000"));
        addRenderableWidget(UiInspector.name(new TimeButton(firstPreset.left() + (buttonW + spacing) * 3, firstPreset.top(), 89, 22, 3, 14000), "clock.preset.14000"));
        addRenderableWidget(UiInspector.name(new TimeButton(firstPreset.left() + (buttonW + spacing) * 4, firstPreset.top(), 113, 22, 4, 18000), "clock.preset.18000"));
        addRenderableWidget(UiInspector.name(new TimeButton(firstPreset.left() + (buttonW + spacing) * 5, firstPreset.top(), 137, 22, 5, 22000), "clock.preset.22000"));
        this.titleLabelX = 62;
        timeInput = new EditBox(this.font, input.left(), input.top(), input.width(), input.height(), Component.literal(""));
        timeInput.setMaxLength(10);
        addRenderableWidget(UiInspector.name(timeInput, "clock.input"));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (timeInput.isFocused() && keyCode == 257) {
            try {
                int time = Integer.parseInt(timeInput.getValue());
                NetworkHandler.CHANNEL.sendToServer(new C2SSetTimePacket(time));
                return true;
            } catch (NumberFormatException ignored) {
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    static class TimeButton extends AbstractWidget {
        private final int texU, texV;
        private final int index;
        private final int timeValue;
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
            NetworkHandler.CHANNEL.sendToServer(new C2SSetTimePacket(timeValue));
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
