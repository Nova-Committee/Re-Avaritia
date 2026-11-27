package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.ball.FireBallEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/7 下午8:49
 * @Description:
 */
public class FireBallRender extends EntityRenderer<FireBallEntity> {
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(Res.DRAGON_FIREBALL);

    public FireBallRender(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int packedLight, float x, int y, int u, int v) {
        consumer.addVertex(pose, x - 0.5F, (float)y - 0.25F, 0.0F)
                .setColor(-1)
                .setUv((float)u, (float)v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    protected int getBlockLightLevel(@NotNull FireBallEntity pEntity, @NotNull BlockPos pPos) {
        return 15;
    }

    @Override
    public void render(@NotNull FireBallEntity pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        pPoseStack.scale(2.0F, 2.0F, 2.0F);
        pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose posestack$pose = pPoseStack.last();
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RENDER_TYPE);
        vertex(vertexconsumer, posestack$pose, pPackedLight, 0.0F, 0, 0, 1);
        vertex(vertexconsumer, posestack$pose, pPackedLight, 1.0F, 0, 1, 1);
        vertex(vertexconsumer, posestack$pose, pPackedLight, 1.0F, 1, 1, 0);
        vertex(vertexconsumer, posestack$pose, pPackedLight, 0.0F, 1, 0, 0);
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FireBallEntity pEntity) {
        return Res.DRAGON_FIREBALL;
    }
}
