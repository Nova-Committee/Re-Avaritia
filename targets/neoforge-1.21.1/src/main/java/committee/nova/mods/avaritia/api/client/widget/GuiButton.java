package committee.nova.mods.avaritia.api.client.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Const;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/23 02:03
 * @Description:
 */
public class GuiButton extends Button{
    public static final ResourceLocation BUTTON_TEXTURES = Const.rl( "textures/gui/buttons.png");

    public ResourceLocation texture;
    public int tile;
    private int state;
    public int texX = 0;
    public int texY = 0;
    public Int2ObjectFunction<Tooltip> tooltipFactory;

    public GuiButton(int x, int y, int tile, Button.OnPress pressable) {
        this (x, y, 16, 16, Component.empty(), pressable);
        this.tile = tile;
        this.texture = BUTTON_TEXTURES;
    }

    public GuiButton(int x, int y, int w, int h, Component text, OnPress onPress) {
        super(x, y, w, h, text, onPress, Supplier::get);
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void renderWidget(GuiGraphics st, int mouseX, int mouseY, float pt) {
        if (this.visible) {
            int x = getX();
            int y = getY();
            this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            st.blit(texture, x, y, texX + state * 16, texY + tile * 16, this.width, this.height);
        }
    }

    public static class CompositeButton extends GuiButton {
        public int texY_button = 16;
        public CompositeButton(int x, int y, int tile, Button.OnPress pressable) {
            super(x, y, tile, pressable);
        }

        /**
         * Draws this button to the screen.
         */
        @Override
        public void renderWidget(GuiGraphics st, int mouseX, int mouseY, float pt) {
            if (this.visible) {
                int x = getX();
                int y = getY();
                st.setColor(1.0f, 1.0f, 1.0f, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
                int i = this.getYImage(this.isHoveredOrFocused());
                st.blit(texture, x, y, texX + i * 16, this.texY_button, this.width, this.height);
                st.blit(texture, x, y, texX + tile * 16 + getState() * 16, texY, this.width, this.height);
                st.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    public int getYImage(boolean hov) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }
        return i;
    }

    public void setState(int state) {
        this.state = state;
        if(tooltipFactory != null)setTooltip(tooltipFactory.apply(state));
    }

    public int getState() {
        return state;
    }
}
