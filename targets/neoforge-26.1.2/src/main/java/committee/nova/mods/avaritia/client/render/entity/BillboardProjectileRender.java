package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class BillboardProjectileRender<T extends Entity> extends EntityRenderer<T, EntityRenderState> {
    private final Identifier texture;
    private final RenderType renderType;
    private final float scale;

    public BillboardProjectileRender(EntityRendererProvider.Context context, Identifier texture, float scale) {
        super(context);
        this.texture = texture;
        this.renderType = RenderTypes.entityCutout(texture);
        this.scale = scale;
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light, float x, int y, int u, int v) {
        consumer.addVertex(pose, x - 0.5F, (float) y - 0.25F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv((float) u, (float) v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    protected int getBlockLightLevel(@NotNull T entity, @NotNull BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(@NotNull EntityRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector output, @NotNull CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, this.scale);
        poseStack.mulPose(cameraState.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        output.submitCustomGeometry(poseStack, this.renderType, (pose, vertexConsumer) -> {
            vertex(vertexConsumer, pose, state.lightCoords, 0.0F, 0, 0, 1);
            vertex(vertexConsumer, pose, state.lightCoords, 1.0F, 0, 1, 1);
            vertex(vertexConsumer, pose, state.lightCoords, 1.0F, 1, 1, 0);
            vertex(vertexConsumer, pose, state.lightCoords, 0.0F, 1, 0, 0);
        });
        poseStack.popPose();
        super.submit(state, poseStack, output, cameraState);
    }

    @Override
    public @NotNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    public @NotNull Identifier getTextureLocation(@NotNull T entity) {
        return this.texture;
    }
}
