package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.Avaritia;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 18:54
 * Version: 1.0
 */
public class HeavenArrowRender extends ArrowRenderer<HeavenArrowEntity, ArrowRenderState> {
    private static final Identifier HEAVEN_ARROW_TEXTURE = Identifier.fromNamespaceAndPath(Const.MOD_ID, "textures/entity/heaven_arrow.png");

    public HeavenArrowRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected @NotNull Identifier getTextureLocation(@NotNull ArrowRenderState state) {
        return HEAVEN_ARROW_TEXTURE;
    }

    @Override
    public void submit(@NotNull ArrowRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector output, @NotNull CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        float shake = state.shake;
        if (shake > 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3.0F) * shake));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0, 0.0, 0.0);
        RenderType renderType = RenderTypes.entityCutout(this.getTextureLocation(state));
        output.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            Matrix4f poseMatrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, state.lightCoords);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, state.lightCoords);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, state.lightCoords);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, state.lightCoords);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, state.lightCoords);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, state.lightCoords);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, state.lightCoords);
            this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, state.lightCoords);
            for (int j = 0; j < 4; ++j) {
                this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, state.lightCoords);
                this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, state.lightCoords);
                this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, state.lightCoords);
                this.drawVertex(poseMatrix, normalMatrix, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, state.lightCoords);
            }
        });
        poseStack.popPose();
        super.submit(state, poseStack, output, cameraState);
    }

    @Override
    public @NotNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
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
