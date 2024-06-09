package committee.nova.mods.avaritia.client.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.api.client.model.CachedFormat;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.model.Quad;
import committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.avaritia.api.client.render.buffer.AlphaOverrideVertexConsumer;
import committee.nova.mods.avaritia.api.client.render.item.IItemRenderer;
import committee.nova.mods.avaritia.util.client.TransformUtils;
import committee.nova.mods.avaritia.util.client.colour.ColourARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Name: Avaritia-forge / HaloBakedModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 22:35
 * Description:
 */

public class HaloBakedModel extends WrappedItemModel implements IItemRenderer {
    private final Random RANDOM;
    private final BakedQuad haloQuad;
    private final boolean pulse;

    public HaloBakedModel(BakedModel wrapped, TextureAtlasSprite sprite, int color, int size, boolean pulse) {
        super(wrapped);
        this.RANDOM = new Random();
        this.haloQuad = generateHaloQuad(sprite, size, color);
        this.pulse = pulse;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        if (transformType == ItemDisplayContext.GUI) {
            Minecraft.getInstance().getItemRenderer()
                    .renderQuadList(pStack, source.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true)), List.of(this.haloQuad),  ItemStack.EMPTY, packedLight, packedOverlay);
            if (this.pulse) {
                pStack.pushPose();
                double scale = RANDOM.nextDouble() * 0.15D + 0.95D;
                double trans = (1.0D - scale) / 2.0D;
                pStack.translate(trans, trans, 0.0D);
                pStack.scale((float)scale, (float)scale, 1.0001F);
                this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true, (e) -> new AlphaOverrideVertexConsumer(e, 0.6000000238418579D));
                pStack.popPose();
            }
        }

        this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);
    }

    @Override
    public PerspectiveModelState getModelState() {
        return TransformUtils.DEFAULT_ITEM;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }

    private static BakedQuad generateHaloQuad(TextureAtlasSprite sprite, int size, int color) {
        float[] colors = (new ColourARGB(color)).getRGBA();
        double spread = (double)size / 16.0D;
        double min = 0.0D - spread;
        double max = 1.0D + spread;
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();
        Quad quad = new Quad();
        quad.reset(CachedFormat.BLOCK);
        quad.setTexture(sprite);
        putVertex(quad.vertices[0], max, max, 0.0D, maxU, minV);
        putVertex(quad.vertices[1], min, max, 0.0D, minU, minV);
        putVertex(quad.vertices[2], min, min, 0.0D, minU, maxV);
        putVertex(quad.vertices[3], max, min, 0.0D, maxU, maxV);

        for(int i = 0; i < 4; ++i) {
            System.arraycopy(colors, 0, quad.vertices[i].color, 0, 4);
        }

        quad.calculateOrientation(true);
        return quad.bake();
    }

    private static void putVertex(Quad.Vertex vertex, double x, double y, double z, double u, double v) {
        vertex.vec[0] = (float)x;
        vertex.vec[1] = (float)y;
        vertex.vec[2] = (float)z;
        vertex.uv[0] = (float)u;
        vertex.uv[1] = (float)v;
    }
}
