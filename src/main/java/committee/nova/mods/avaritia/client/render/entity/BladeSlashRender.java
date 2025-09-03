package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.common.entity.BladeSlashEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/16 01:41
 * @Description:
 */
@Environment(EnvType.CLIENT)
public class BladeSlashRender extends EntityRenderer<BladeSlashEntity> {


    public BladeSlashRender(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(BladeSlashEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource buffer, int packedLight) {

        matrixStackIn.pushPose();

        matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 10));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(entity.zRot));
        matrixStackIn.scale(0.6F, 0.6F, 1.8F);
        PoseStack.Pose matrixStackEntry = matrixStackIn.last();
        Matrix4f pose = matrixStackEntry.pose();
        Matrix3f normal = matrixStackEntry.normal();
        VertexConsumer builder = buffer.getBuffer(AvaritiaRenderTypes.BLADE_SLASH);

        packedLight = 0x00F000F0;
        this.vertex(pose, normal, builder, 1, 0, 1, 1, 0, 0, 1, 0, packedLight);
        this.vertex(pose, normal, builder, 1, 0, -1, 0, 0, 0, 1, 0, packedLight);
        this.vertex(pose, normal, builder, -1, 0, -1, 0, 1, 0, 1, 0, packedLight);
        this.vertex(pose, normal, builder, -1, 0, 1, 1, 1, 0, 1, 0, packedLight);

        matrixStackIn.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStackIn, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BladeSlashEntity entity) {
        return Res.BLADE_SLASH;
    }

    public void vertex(Matrix4f pose, Matrix3f normal, VertexConsumer builder, float x, float y, float z, float u, float v, int nx, int nz, int ny, int packedLight) {

        builder.vertex(pose, x, y, z).color(255, 255, 255, 200).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, nx, ny, nz).endVertex();
    }

}
