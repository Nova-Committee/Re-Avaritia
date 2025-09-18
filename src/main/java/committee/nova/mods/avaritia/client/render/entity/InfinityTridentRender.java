package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.common.entity.InfinityThrownTrident;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class InfinityTridentRender extends EntityRenderer<InfinityThrownTrident> {
    public static final ResourceLocation TRIDENT_LOCATION = new ResourceLocation("avaritia","textures/entity/infinity_trident_model.png");
    private final TridentModel model;

    public InfinityTridentRender(EntityRendererProvider.Context p_174420_) {
        super(p_174420_);
        this.model = new TridentModel(p_174420_.bakeLayer(ModelLayers.TRIDENT));
    }

    public void render(InfinityThrownTrident p_116111_, float p_116112_, float p_116113_, PoseStack p_116114_, MultiBufferSource p_116115_, int p_116116_) {
        p_116114_.pushPose();
        p_116114_.mulPose(Axis.YP.rotationDegrees(Mth.lerp(p_116113_, p_116111_.yRotO, p_116111_.getYRot()) - 90.0F));
        p_116114_.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(p_116113_, p_116111_.xRotO, p_116111_.getXRot()) + 90.0F));
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(p_116115_, this.model.renderType(this.getTextureLocation(p_116111_)), false, p_116111_.isFoil());
        this.model.renderToBuffer(p_116114_, vertexconsumer, p_116116_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        p_116114_.popPose();
        super.render(p_116111_, p_116112_, p_116113_, p_116114_, p_116115_, p_116116_);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull InfinityThrownTrident thrownTrident) {
        return TRIDENT_LOCATION;
    }
}
