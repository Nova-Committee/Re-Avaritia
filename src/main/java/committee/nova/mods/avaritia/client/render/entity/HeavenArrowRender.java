package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 18:54
 * Version: 1.0
 */
@OnlyIn(Dist.CLIENT)
public class HeavenArrowRender extends ArrowRenderer<HeavenArrowEntity> {

    private static final ResourceLocation tex = new ResourceLocation(Static.MOD_ID, "textures/entity/heavenarrow.png");


    public HeavenArrowRender(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull HeavenArrowEntity entity) {
        return tex;
    }

    @Override
    public void render(@NotNull HeavenArrowEntity arrowEntity, float pEntityYaw, float partialTicks, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, arrowEntity.yRotO, arrowEntity.getYRot()) - 90.0f));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, arrowEntity.xRotO, arrowEntity.getXRot())));
        final float f9 = arrowEntity.shakeTime - partialTicks;
        if (f9 > 0.0f) {
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(f9 * 3.0f) * f9));
        }
        pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0f));
        pPoseStack.scale(0.05625f, 0.05625f, 0.05625f);
        pPoseStack.translate(-4.0, 0.0, 0.0);
        final VertexConsumer ivertexbuilder = pBuffer.getBuffer(RenderType.create("entity_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, RenderType.CompositeState.builder()
                .setShaderState(RenderType.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(this.getTextureLocation(arrowEntity), false, false))
                .setOverlayState(RenderType.OVERLAY).createCompositeState(true)));
        final PoseStack.Pose matrixstack$entry = pPoseStack.last();
        final Matrix4f matrix4f = matrixstack$entry.pose();
        final Matrix3f matrix3f = matrixstack$entry.normal();
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, -2, -2, 0.0f, 0.15625f, -1, 0, 0, pPackedLight);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, -2, 2, 0.15625f, 0.15625f, -1, 0, 0, pPackedLight);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, 2, 2, 0.15625f, 0.3125f, -1, 0, 0, pPackedLight);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, 2, -2, 0.0f, 0.3125f, -1, 0, 0, pPackedLight);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, 2, -2, 0.0f, 0.15625f, 1, 0, 0, pPackedLight);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, 2, 2, 0.15625f, 0.15625f, 1, 0, 0, pPackedLight);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, -2, 2, 0.15625f, 0.3125f, 1, 0, 0, pPackedLight);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, -2, -2, 0.0f, 0.3125f, 1, 0, 0, pPackedLight);
        for (int j = 0; j < 4; ++j) {
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -8, -2, 0, 0.0f, 0.0f, 0, 1, 0, pPackedLight);
            this.drawVertex(matrix4f, matrix3f, ivertexbuilder, 8, -2, 0, 0.5f, 0.0f, 0, 1, 0, pPackedLight);
            this.drawVertex(matrix4f, matrix3f, ivertexbuilder, 8, 2, 0, 0.5f, 0.15625f, 0, 1, 0, pPackedLight);
            this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -8, 2, 0, 0.0f, 0.15625f, 0, 1, 0, pPackedLight);
        }
        pPoseStack.popPose();
    }
    public void drawVertex(final Matrix4f m1, final Matrix3f m2, final VertexConsumer v, final int offsetX, final int offsetY, final int offsetZ, final float textureX, final float textureY, final int pContext9_, final int pContext10_, final int pContext11_, final int packedLightIn) {
        v.vertex(m1, (float)offsetX, (float)offsetY, (float)offsetZ).color(255, 255, 255, 255).uv(textureX, textureY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(m2, (float)pContext9_, (float)pContext11_, (float)pContext10_).endVertex();
    }

}
