package committee.nova.mods.avaritia.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/16 20:40
 * Version: 1.0
 */
@OnlyIn(Dist.CLIENT)
public class EyeInfinityLayer extends EyesLayer<Player, HumanoidModel<Player>> {
    private static final RenderType EYES = RenderType.eyes(new ResourceLocation(Static.MOD_ID, "textures/models/armor/infinity_armor_eyes.png"));


    public EyeInfinityLayer(RenderLayerParent<Player, HumanoidModel<Player>> p_116981_) {
        super(p_116981_);
    }

    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Player pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (InfinityHandler.isInfinite(pLivingEntity))
            super.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
