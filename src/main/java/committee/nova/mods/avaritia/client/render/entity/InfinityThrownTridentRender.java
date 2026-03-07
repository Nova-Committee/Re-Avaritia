package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.render.CCModel;
import committee.nova.mods.avaritia.api.client.render.CCRenderState;
import committee.nova.mods.avaritia.api.client.render.model.OBJParser;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.common.entity.InfinityThrownTrident;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
//TODO:投掷出的位置(未调整)
@OnlyIn(Dist.CLIENT)
public class InfinityThrownTridentRender extends EntityRenderer<InfinityThrownTrident> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfinityThrownTridentRender.class);
    private final Map<String, CCModel> tridentModel;

    public InfinityThrownTridentRender(EntityRendererProvider.Context context) {
        super(context);
        this.tridentModel = new OBJParser(Const.rl("models/infinity_trident.obj"))
                .swapYZ()
                .ignoreMtl()
                .parse();

        if (tridentModel.isEmpty()) {
            LOGGER.error("Failed to load trident OBJ model!");
        } else {
            LOGGER.info("Loaded trident models: {}", tridentModel.keySet());
        }
    }

    @Override
    public void render(InfinityThrownTrident entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
//
//        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
//        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));


        CCRenderState cc = CCRenderState.instance();
        cc.reset();
        cc.bind(AvaritiaRenderTypes.TRIDENT, buffer, poseStack);
        cc.baseColour = 0xFFFFFFFF;

        for (CCModel model : tridentModel.values()) {
            model.render(cc);
        }

        poseStack.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull InfinityThrownTrident thrownTrident) {
        return Res.TRIDENT_TEX;
    }
}
