package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.ball.BurningBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class BurningBallRender extends EntityRenderer<BurningBallEntity, EntityRenderState> {
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(Res.DRAGON_FIREBALL);

    public BurningBallRender(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    private static void vertex(VertexConsumer pConsumer, PoseStack.Pose pPose, int pLightmapUV, float pX, int pY, int pU, int pV) {
        pConsumer.addVertex(pPose, pX - 0.5F, (float) pY - 0.25F, 0.0F).setColor(255, 255, 255, 255).setUv((float) pU, (float) pV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(pLightmapUV).setNormal(pPose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    protected int getBlockLightLevel(@NotNull BurningBallEntity pEntity, @NotNull BlockPos pPos) {
        return 15;
    }

    @Override
    public void submit(@NotNull EntityRenderState pState, @NotNull PoseStack pPoseStack, @NotNull SubmitNodeCollector pOutput, @NotNull CameraRenderState pCameraState) {
        pPoseStack.pushPose();
        pPoseStack.scale(2.0F, 2.0F, 2.0F);
        pPoseStack.mulPose(pCameraState.orientation);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        pOutput.submitCustomGeometry(pPoseStack, RENDER_TYPE, (pose, vertexconsumer) -> {
            vertex(vertexconsumer, pose, pState.lightCoords, 0.0F, 0, 0, 1);
            vertex(vertexconsumer, pose, pState.lightCoords, 1.0F, 0, 1, 1);
            vertex(vertexconsumer, pose, pState.lightCoords, 1.0F, 1, 1, 0);
            vertex(vertexconsumer, pose, pState.lightCoords, 0.0F, 1, 0, 0);
        });
        pPoseStack.popPose();
        super.submit(pState, pPoseStack, pOutput, pCameraState);
    }

    @Override
    public @NotNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    /**
     * Returns the location of an entity's texture.
     */
    public @NotNull Identifier getTextureLocation(@NotNull BurningBallEntity pEntity) {
        return Res.DRAGON_FIREBALL;
    }
}
