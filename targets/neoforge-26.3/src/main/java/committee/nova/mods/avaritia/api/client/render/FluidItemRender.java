package committee.nova.mods.avaritia.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.fluids.FluidStack;


/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 13:55
 * @Description:
 */
public final class FluidItemRender {

    private final TextureAtlasSprite sprite;
    private final int referenceWidth;
    private final int referenceHeight;
    private int r = 255;
    private int g = 255;
    private int b = 255;
    private int a = 255;
    private Rect2i srcRect;
    private Rect2i destRect = new Rect2i(0, 0, 0, 0);
    private boolean blending = true;

    public FluidItemRender(TextureAtlasSprite sprite, int referenceWidth, int referenceHeight) {
        this.sprite = sprite;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    public static FluidItemRender sprite(TextureAtlasSprite sprite) {
        return new FluidItemRender(sprite, 1, 1).src(
                0,
                0,
                1,
                1
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

    public void blit(PoseStack poseStack, SubmitNodeCollector source, int zIndex) {
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
        float width = 0, height = 0;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            width = destRect.getWidth();
            height = destRect.getHeight();
        } else if (srcRect != null) {
            width = srcRect.getWidth();
            height = srcRect.getHeight();
        }
        float x1 = destRect.getX();
        float y1 = destRect.getY();
        float x2 = x1 + width;
        float y2 = y1 + height;

        // 26.3 移除了 MeshData/RenderType#draw 立即绘制，改走提交节点几何路径。
        // 顶点仍写入位置/UV/颜色；itemTranslucent 的顶点格式额外要求光照/覆盖层/法线，
        // 用全亮(FULL_BRIGHT)、无覆盖层(NO_OVERLAY)与朝向观察者的法线补齐，保持图标始终可见。
        source.submitCustomGeometry(poseStack, RenderTypes.itemTranslucent(sprite.atlasLocation()), (pose, buffer) -> {
            VertexConsumer consumer = sprite.wrap(buffer);
            consumer.addVertex(pose, x1, y2, zIndex).setUv(minU, maxV).setColor(r, g, b, blending ? a : 255).setLight(LightCoordsUtil.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0.0F, 0.0F, 1.0F);
            consumer.addVertex(pose, x2, y2, zIndex).setUv(maxU, maxV).setColor(r, g, b, blending ? a : 255).setLight(LightCoordsUtil.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0.0F, 0.0F, 1.0F);
            consumer.addVertex(pose, x2, y1, zIndex).setUv(maxU, minV).setColor(r, g, b, blending ? a : 255).setLight(LightCoordsUtil.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0.0F, 0.0F, 1.0F);
            consumer.addVertex(pose, x1, y1, zIndex).setUv(minU, minV).setColor(r, g, b, blending ? a : 255).setLight(LightCoordsUtil.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0.0F, 0.0F, 1.0F);
        });
    }

    public static void renderFluid(FluidStack fluidStack, PoseStack poseStack, SubmitNodeCollector source, int x, int y, int z) {
        FluidState fluidState = fluidStack.getFluid().defaultFluidState();
        FluidModel fluidModel = Minecraft.getInstance()
                .getModelManager()
                .getFluidStateModelSet()
                .get(fluidState);
        TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();
        FluidTintSource tintSource = FluidTintSources.of(fluidModel.tintSource());
        int color = tintSource != null ? tintSource.colorAsStack(fluidStack) : -1;
        sprite(sprite)
                .colorRgb(color)
                .blending(false)
                .dest(x, y, 16, 16)
                .blit(poseStack, source, z);
    }

}
