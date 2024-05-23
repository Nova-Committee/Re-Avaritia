package committee.nova.mods.avaritia.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.model.WingModel;
import committee.nova.mods.avaritia.util.ToolUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/16 18:24
 * Version: 1.0
 */
@Environment(EnvType.CLIENT)
public class WingInfinityLayer extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {


    public WingInfinityLayer(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferIn, int pPackedLight, @NotNull LivingEntity livingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

        var loc = new ResourceLocation(Static.MOD_ID, "textures/models/armor/infinity_armor_wing.png");

        if (ToolUtil.isInfiniteChest(livingEntity)) {
            var model = new WingModel(WingModel.createBodyLayer().bakeRoot());
            if (!livingEntity.isInvisible()) {
                VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityCutout(loc));
                poseStack.pushPose();

                poseStack.translate(0, -2.8, 0.325);
                poseStack.scale(3F, 3F, 3F);
                model.renderToBuffer(poseStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

                poseStack.popPose();
            }
        }
    }


}
