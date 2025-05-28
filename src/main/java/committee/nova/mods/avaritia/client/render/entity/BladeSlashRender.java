package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.BladeSlashEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.client.renderer.RenderStateShard.*;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/16 01:41
 * @Description:
 */
@OnlyIn(Dist.CLIENT)
public class BladeSlashRender extends EntityRenderer<BladeSlashEntity> {

    private static final RenderType RENDER_TYPE = RenderType.create("blade_projectile",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
            RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(Res.BLADE_SLASH, false, false))
                    .setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .createCompositeState(true));

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
        VertexConsumer builder = buffer.getBuffer(RENDER_TYPE);

        packedLight = 0x00F000F0;
        this.vertex(matrixStackEntry, builder, 1, 0, 1, 1, 0, 0, 1, 0, packedLight);
        this.vertex(matrixStackEntry, builder, 1, 0, -1, 0, 0, 0, 1, 0, packedLight);
        this.vertex(matrixStackEntry, builder, -1, 0, -1, 0, 1, 0, 1, 0, packedLight);
        this.vertex(matrixStackEntry, builder, -1, 0, 1, 1, 1, 0, 1, 0, packedLight);

        matrixStackIn.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStackIn, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BladeSlashEntity entity) {
        return Res.BLADE_SLASH;
    }

    public void vertex(PoseStack.Pose pose, VertexConsumer builder, float x, float y, float z, float u, float v, int nx, int nz, int ny, int packedLight) {

        builder.addVertex(x, y, z).setColor(255, 255, 255, 200).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(nx, ny, nz);
    }

}
