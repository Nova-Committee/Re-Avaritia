package committee.nova.mods.avaritia.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.model.WingModel;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/16 18:24
 * Version: 1.0
 */
@OnlyIn(Dist.CLIENT)
public class WingInfinityLayer extends RenderLayer<Player, HumanoidModel<Player>> {


    public WingInfinityLayer(RenderLayerParent<Player, HumanoidModel<Player>> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int pPackedLight, Player player, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

        var loc = new ResourceLocation(Static.MOD_ID, "textures/models/armor/infinity_armor_wing.png");

        if (InfinityHandler.isInfiniteChestplate(player)) {
            var model = new WingModel(WingModel.createBodyLayer().bakeRoot());
            if (!player.isInvisible() && player.getAbilities().flying) {
                VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentCull(loc));
                poseStack.pushPose();


                poseStack.translate(0, -2.8, 0.325);
                poseStack.scale(3F, 3F, 3F);

                model.renderToBuffer(poseStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

                poseStack.popPose();
            }
        }
    }
}
