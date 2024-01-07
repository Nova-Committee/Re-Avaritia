package committee.nova.mods.avaritia.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.util.AbilityUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/16 20:40
 * Version: 1.0
 */
@OnlyIn(Dist.CLIENT)
public class EyeInfinityLayer extends EyesLayer<LivingEntity, HumanoidModel<LivingEntity>> {
    private static final RenderType EYES = RenderType.eyes(new ResourceLocation(Static.MOD_ID, "textures/models/armor/infinity_armor_eyes.png"));


    public EyeInfinityLayer(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> p_116981_) {
        super(p_116981_);
    }

    @Override
    public void render(@NotNull PoseStack pMatrixStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, @NotNull LivingEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        VertexConsumer consumer = pBuffer.getBuffer(this.renderType());
        if (AbilityUtil.isInfinite(pLivingEntity))
            this.getParentModel().renderToBuffer(pMatrixStack, consumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public @NotNull RenderType renderType() {
        return EYES;
    }
}
