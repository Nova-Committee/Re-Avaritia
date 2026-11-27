package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.common.entity.TNTProEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TNTProEntityRender extends EntityRenderer<TNTProEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public TNTProEntityRender(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.shadowRadius = 0.5F;
        this.blockRenderer = pContext.getBlockRenderDispatcher();
    }

    public void render(TNTProEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        pPoseStack.translate(0.0F, 0.5F, 0.0F);
        int $$6 = pEntity.getFuse();
        if ((float) $$6 - pPartialTicks + 1.0F < 10.0F) {
            float $$7 = 1.0F - ((float) $$6 - pPartialTicks + 1.0F) / 10.0F;
            $$7 = Mth.clamp($$7, 0.0F, 1.0F);
            $$7 *= $$7;
            $$7 *= $$7;
            float $$8 = 1.0F + $$7 * 0.3F;
            pPoseStack.scale($$8, $$8, $$8);
        }

        pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        pPoseStack.translate(-0.5F, -0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, net.minecraft.world.level.block.Blocks.TNT.defaultBlockState(), pPoseStack, pBuffer, pPackedLight, $$6 / 5 % 2 == 0);
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    public ResourceLocation getTextureLocation(TNTProEntity pEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

}
