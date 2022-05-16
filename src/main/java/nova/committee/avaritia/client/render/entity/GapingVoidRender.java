package nova.committee.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.client.model.GapingVoidModel;
import nova.committee.avaritia.common.entity.GapingVoidEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 10:34
 * Version: 1.0
 */
public class GapingVoidRender extends EntityRenderer<GapingVoidEntity> {
    private final ResourceLocation fill = new ResourceLocation(Static.MOD_ID, "textures/entity/void.png");


    private final EntityModel<Entity> gapingVoid;

    public GapingVoidRender(EntityRendererProvider.Context context) {
        super(context);
        gapingVoid = new GapingVoidModel<>(context.bakeLayer(GapingVoidModel.LAYER_LOCATION));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GapingVoidEntity p_114482_) {
        return fill;
    }

    @Override
    public void render(@NotNull GapingVoidEntity ent, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLightIn) {
        poseStack.pushPose();
        VertexConsumer builder = bufferSource.getBuffer(this.gapingVoid.renderType(this.getTextureLocation(ent)));
        float scale = (float) GapingVoidEntity.getVoidScale(ent.getAge() + partialTicks);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, -scale * 0.1d, 0);
        this.gapingVoid.renderToBuffer(poseStack, builder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();

    }

}
