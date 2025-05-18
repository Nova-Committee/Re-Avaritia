package committee.nova.mods.avaritia.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 13:55
 * @Description:
 */
@OnlyIn(Dist.CLIENT)
public final class FluidItemRender {

    private final ResourceLocation texture;
    private final int referenceWidth;
    private final int referenceHeight;
    private int r = 255;
    private int g = 255;
    private int b = 255;
    private int a = 255;
    private Rect2i srcRect;
    private Rect2i destRect = new Rect2i(0, 0, 0, 0);
    private boolean blending = true;

    public FluidItemRender(ResourceLocation texture, int referenceWidth, int referenceHeight) {
        this.texture = texture;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    public static FluidItemRender sprite(TextureAtlasSprite sprite) {
        // We use this convoluted method to convert from UV in the range of [0,1] back to pixel values with a
        // fictitious reference size of a large integer. This is converted back to UV later when we actually blit.
        final int refSize = Integer.MAX_VALUE / 2; // Don't use max int to prevent overflows after inexact conversions.

        return new FluidItemRender(sprite.atlasLocation(), refSize, refSize).src(
                (int) (sprite.getU0() * refSize),
                (int) (sprite.getV0() * refSize),
                (int) ((sprite.getU1() - sprite.getU0()) * refSize),
                (int) ((sprite.getV1() - sprite.getV0()) * refSize)
        );
    }

    /**
     * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
     */
    public FluidItemRender src(int x, int y, int w, int h) {
        this.srcRect = new Rect2i(x, y, w, h);
        return this;
    }

    /**
     * Draw into the rectangle defined by the given coordinates.
     */
    public FluidItemRender dest(int x, int y, int w, int h) {
        this.destRect = new Rect2i(x, y, w, h);
        return this;
    }

    public FluidItemRender color(float r, float g, float b) {
        this.r = (int) (Mth.clamp(r, 0, 1) * 255);
        this.g = (int) (Mth.clamp(g, 0, 1) * 255);
        this.b = (int) (Mth.clamp(b, 0, 1) * 255);
        return this;
    }

    public FluidItemRender opacity(float a) {
        this.a = (int) (Mth.clamp(a, 0, 1) * 255);
        return this;
    }

    /**
     * Enables or disables alpha-blending. If disabled, all pixels of the texture will be drawn as opaque, and the alpha
     * value set using {@link #opacity(float)} will be ignored.
     */
    public FluidItemRender blending(boolean enable) {
        this.blending = enable;
        return this;
    }

    /**
     * Sets the color to the R,G,B values encoded in the lower 24-bit of the given integer.
     */
    public FluidItemRender colorRgb(int packedRgb) {
        float r = (packedRgb >> 16 & 255) / 255.0F;
        float g = (packedRgb >> 8 & 255) / 255.0F;
        float b = (packedRgb & 255) / 255.0F;

        return color(r, g, b);
    }

    public void blit(PoseStack poseStack, int zIndex) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.texture);

        // With no source rectangle, we'll use the entirety of the texture. This happens rarely though.
        float minU, minV, maxU, maxV;
        if (srcRect == null) {
            minU = minV = 0;
            maxU = maxV = 1;
        } else {
            minU = srcRect.getX() / (float) referenceWidth;
            minV = srcRect.getY() / (float) referenceHeight;
            maxU = (srcRect.getX() + srcRect.getWidth()) / (float) referenceWidth;
            maxV = (srcRect.getY() + srcRect.getHeight()) / (float) referenceHeight;
        }

        // It's possible to not set a destination rectangle size, in which case the
        // source rectangle size will be used
        float x1 = destRect.getX();
        float y1 = destRect.getY();
        float x2 = x1, y2 = y1;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            x2 += destRect.getWidth();
            y2 += destRect.getHeight();
        } else if (srcRect != null) {
            x2 += srcRect.getWidth();
            y2 += srcRect.getHeight();
        }

        Matrix4f matrix = poseStack.last().pose();

        var tesselator = Tesselator.getInstance();
        var bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix, x1, y2, zIndex).setUv(minU, maxV).setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x2, y2, zIndex).setUv(maxU, maxV).setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x2, y1, zIndex).setUv(maxU, minV).setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x1, y1, zIndex).setUv(minU, minV).setColor(r, g, b, a);

        if (blending) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            RenderSystem.disableBlend();
        }
       // RenderSystem.enableTexture();
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void renderFluid(FluidStack fluidStack, PoseStack poseStack, int x, int y, int z) {
        IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(attributes.getStillTexture(fluidStack));
        sprite(sprite)
                .colorRgb(attributes.getTintColor(fluidStack))
                .blending(false)
                .dest(x, y, 16, 16)
                .blit(poseStack, z);
    }

}
