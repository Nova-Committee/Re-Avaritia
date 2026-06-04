package com.avaritia.client.render.entity;

import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.common.entity.BladeSlashEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BladeSlashRender extends EntityRenderer<BladeSlashEntity, BladeSlashRender.BladeSlashRenderState> {
    public BladeSlashRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(@NotNull BladeSlashEntity entity, @NotNull BladeSlashRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        state.zRot = entity.zRot;
        state.tickCount = entity.tickCount;
        state.duration = entity.duration;
    }

    @Override
    public void submit(@NotNull BladeSlashRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector output, @NotNull CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 10.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.zRot));
        poseStack.scale(0.6F, 0.6F, 1.8F);

        int alpha = calculateAlpha(state);
        output.submitCustomGeometry(poseStack, AvaritiaRenderTypes.BLADE_SLASH, (pose, vertexConsumer) -> {
            Matrix4f poseMatrix = pose.pose();
            int light = 0x00F000F0;
            vertex(poseMatrix, vertexConsumer, 1, 0, 1, 1, 0, light, alpha);
            vertex(poseMatrix, vertexConsumer, 1, 0, -1, 0, 0, light, alpha);
            vertex(poseMatrix, vertexConsumer, -1, 0, -1, 0, 1, light, alpha);
            vertex(poseMatrix, vertexConsumer, -1, 0, 1, 1, 1, light, alpha);
        });
        poseStack.popPose();
        super.submit(state, poseStack, output, cameraState);
    }

    private static int calculateAlpha(BladeSlashRenderState state) {
        int baseAlpha = 200;
        if (state.tickCount > state.duration - 10) {
            int remainingTicks = state.duration - state.tickCount;
            return Math.max(0, baseAlpha * remainingTicks / 10);
        }
        return baseAlpha;
    }

    private static void vertex(Matrix4f pose, VertexConsumer consumer, float x, float y, float z, float u, float v, int light, int alpha) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public @NotNull BladeSlashRenderState createRenderState() {
        return new BladeSlashRenderState();
    }

    public @NotNull Identifier getTextureLocation(@NotNull BladeSlashEntity entity) {
        return com.avaritia.Res.BLADE_SLASH;
    }

    public static class BladeSlashRenderState extends EntityRenderState {
        public float xRot;
        public float yRot;
        public float zRot;
        public int tickCount;
        public int duration;
    }
}
