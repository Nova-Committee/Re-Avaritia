package committee.nova.mods.avaritia.api.client.screen.component;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class TextureButton extends Button {
    protected final ResourceLocation resourceLocation;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int xDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;
    private final int xOffset;
    private final int yOffset;
    private final int usedTextureWidth;
    private final int usedTextureHeight;

    TextureButton(Component message, int x, int y, int width, int height, int xTexStart, int yTexStart, int xOffset, int yOffset, int xDiffTex, int textureWidth, int textureHeight, ResourceLocation resourceLocation, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.xDiffTex = xDiffTex;
        this.resourceLocation = resourceLocation;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.usedTextureWidth = width;
        this.usedTextureHeight = height;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.renderXDiffTexture(guiGraphics, this.resourceLocation, this.getXOffset(), this.getYOffset(), this.xTexStart, this.yTexStart, this.xDiffTex, this.usedTextureWidth, this.usedTextureHeight, this.textureWidth, this.textureHeight);
    }

    public void renderXDiffTexture(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int uOffset, int vOffset, int textureDifference, int width, int height, int textureWidth, int textureHeight) {
        int i = uOffset;
        if (!this.isActive()) {
            i = uOffset + textureDifference * 2;
        } else if (this.isHoveredOrFocused()) {
            i = uOffset + textureDifference;
        }

        RenderSystem.enableDepthTest();
        guiGraphics.blit(texture, x, y, (float)i, (float)vOffset, width, height, textureWidth, textureHeight);
    }

    @Override
    public void renderString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int color) {
        int i = this.getX() + 2;
        int j = this.getX() + this.getWidth() - this.usedTextureWidth - 6;
        renderScrollingString(guiGraphics, font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), color);
    }

    private int getXOffset() {
        return this.getX() + (this.width / 2 - this.usedTextureWidth / 2) + this.xOffset;
    }

    private int getYOffset() {
        return this.getY() + this.yOffset;
    }

    public static Builder builder(Component message, ResourceLocation resourceLocation, Button.OnPress onPress) {
        return new Builder(message, resourceLocation, onPress);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final ResourceLocation resourceLocation;
        private final Button.OnPress onPress;
        private int xTexStart;
        private int yTexStart;
        private int xDiffTex;
        private int x = 0;
        private int y = 0;
        private int width = 20;
        private int height = 20;
        private int textureWidth;
        private int textureHeight;
        private int xOffset;
        private int yOffset;

        public Builder(Component message, ResourceLocation resourceLocation, Button.OnPress onPress) {
            this.message = message;
            this.resourceLocation = resourceLocation;
            this.onPress = onPress;
        }

        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder texStart(int x, int y) {
            this.xTexStart = x;
            this.yTexStart = y;
            return this;
        }

        public Builder offset(int x, int y) {
            this.xOffset = x;
            this.yOffset = y;
            return this;
        }

        public Builder xDiffTex(int xDiffTex) {
            this.xDiffTex = xDiffTex;
            return this;
        }

        public Builder textureSize(int width, int height) {
            this.textureWidth = width;
            this.textureHeight = height;
            return this;
        }

        public TextureButton build() {
            return new TextureButton(this.message, this.x, this.y, this.width, this.height, this.xTexStart, this.yTexStart, this.xOffset, this.yOffset, this.xDiffTex, this.textureWidth, this.textureHeight, this.resourceLocation, this.onPress);
        }
    }
}
