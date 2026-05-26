package com.avaritia.client.screen.side;

import com.avaritia.Res;
import com.avaritia.core.io.SideConfiguration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class SideButton extends ImageButton {
    private final SideConfigScreen sideConfigScreen;
    private final Direction direction;
    private SideConfiguration.SideMode mode;
    private final List<FormattedCharSequence> tooltip;

    public SideButton(int x, int y, Direction direction, SideConfiguration.SideMode mode, SideConfigScreen sideConfigScreen) {
        super(x, y, 22, 23, new WidgetSprites(Res.SIDE_CONFIG_TEX, Res.SIDE_CONFIG_TEX), button -> {
            sideConfigScreen.cycleModeForDirection(direction);
            sideConfigScreen.updateAllButtons();
            sideConfigScreen.sendConfigUpdate();
        });
        this.sideConfigScreen = sideConfigScreen;
        this.direction = direction;
        this.mode = mode;
        this.tooltip = new ArrayList<>();
        updateTooltip();
    }

    private void updateTooltip() {
        this.tooltip.clear();
        String sideName = Component.translatable("direction.avaritia." + this.direction.getName()).getString();
        String modeName = this.mode.getDisplayName().getString();

        this.tooltip.add(Component.literal(sideName + ": " + modeName).getVisualOrderText());
        this.tooltip.add(Component.translatable("tooltip.avaritia.side.click_to_cycle").getVisualOrderText());
    }

    public void updateMode(SideConfiguration.SideMode newMode) {
        this.mode = newMode;
        updateTooltip();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int texX;
        int texY = 118;

        if (this.mode == SideConfiguration.SideMode.OFF) {
            texX = 0;
        } else if (this.mode == SideConfiguration.SideMode.PASSIVE_OUTPUT || this.mode == SideConfiguration.SideMode.PASSIVE_MIXIN) {
            texX = 22;
        } else if (this.mode == SideConfiguration.SideMode.ACTIVE_INPUT) {
            texX = 2 * 22;
        } else if (this.mode == SideConfiguration.SideMode.ACTIVE_OUTPUT) {
            texX = 3 * 22;
        } else if (this.mode == SideConfiguration.SideMode.ACTIVE_MIXIN) {
            texX = 4 * 22;
        } else {
            texX = 0;
        }

        if (this.isHovered) {
            texY += 23;
        }

        pGuiGraphics.blit(Res.SIDE_CONFIG_TEX, this.getX(), this.getY(), texX, texY, this.width, this.height, 256, 256);

        if (this.isHovered) {
            sideConfigScreen.setTooltipForNextRenderPass(tooltip);
        }
    }
}
