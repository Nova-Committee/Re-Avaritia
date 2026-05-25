package com.avaritia.client.render.entity;

import com.avaritia.Avaritia;
import com.avaritia.common.entity.arrow.HeavenArrowEntity;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
    private static final Identifier HEAVEN_ARROW_TEXTURE = Identifier.of(Avaritia.MOD_ID, "textures/entity/heaven_arrow.png");

    public HeavenArrowRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull HeavenArrowEntity entity) {
        return HEAVEN_ARROW_TEXTURE;
    }

    @Override
    public void render(@NotNull HeavenArrowEntity arrowEntity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, arrowEntity.yRotO, arrowEntity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, arrowEntity.xRotO, arrowEntity.getXRot())));
        float shake = arrowEntity.shakeTime - partialTicks;
        if (shake > 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3.0F) * shake));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0, 0.0, 0.0);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.create("avaritia_heaven_arrow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderType.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(this.getTextureLocation(arrowEntity), false, false))
                        .setOverlayState(RenderType.OVERLAY)
                        .createCompositeState(true)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, packedLight);
        this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, packedLight);
        this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, packedLight);
        this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, packedLight);
        this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, packedLight);
        this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, packedLight);
        this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, packedLight);
        this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, packedLight);
        for (int j = 0; j < 4; ++j) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, packedLight);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, packedLight);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, packedLight);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, packedLight);
        }
        poseStack.popPose();
    }

    public void drawVertex(Matrix4f poseMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, int offsetX, int offsetY, int offsetZ, float textureX, float textureY, int normalX, int normalY, int normalZ, int packedLight) {
        vertexConsumer.addVertex(poseMatrix, offsetX, offsetY, offsetZ)
                .setColor(255, 255, 255, 255)
                .setUv(textureX, textureY)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(normalX, normalZ, normalY);
    }
}
