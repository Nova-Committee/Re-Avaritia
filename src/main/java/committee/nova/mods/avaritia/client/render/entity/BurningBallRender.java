package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.ball.BurningBallEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/20 18:54
 * Version: 1.0
 */
@OnlyIn(Dist.CLIENT)
public class BurningBallRender extends EntityRenderer<BurningBallEntity> {
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(Res.DRAGON_FIREBALL);

    public BurningBallRender(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    private static void vertex(VertexConsumer pConsumer, Matrix4f pPose, PoseStack.Pose pNormal, int pLightmapUV, float pX, int pY, int pU, int pV) {
        pConsumer.addVertex(pPose, pX - 0.5F, (float) pY - 0.25F, 0.0F).setColor(255, 255, 255, 255).setUv((float) pU, (float) pV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(pLightmapUV).setNormal(pNormal, 0.0F, 1.0F, 0.0F);
    }

    @Override
    protected int getBlockLightLevel(@NotNull BurningBallEntity pEntity, @NotNull BlockPos pPos) {
        return 15;
    }

    @Override
    public void render(@NotNull BurningBallEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        pPoseStack.scale(2.0F, 2.0F, 2.0F);
        pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RENDER_TYPE);
        vertex(vertexconsumer, matrix4f, posestack$pose, pPackedLight, 0.0F, 0, 0, 1);
        vertex(vertexconsumer, matrix4f, posestack$pose, pPackedLight, 1.0F, 0, 1, 1);
        vertex(vertexconsumer, matrix4f, posestack$pose, pPackedLight, 1.0F, 1, 1, 0);
        vertex(vertexconsumer, matrix4f, posestack$pose, pPackedLight, 0.0F, 1, 0, 0);
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BurningBallEntity pEntity) {
        return Res.DRAGON_FIREBALL;
    }
}
