package com.avaritia.client.render.entity;

import com.avaritia.Avaritia;
import com.avaritia.Res;
import com.avaritia.api.client.render.CCModel;
import com.avaritia.api.client.render.CCRenderState;
import com.avaritia.api.client.render.model.OBJParser;
import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.common.entity.InfinityThrownTrident;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class InfinityThrownTridentRender extends EntityRenderer<InfinityThrownTrident> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfinityThrownTridentRender.class);
    private final Map<String, CCModel> tridentModel;

    public InfinityThrownTridentRender(EntityRendererProvider.Context context) {
        super(context);
        this.tridentModel = new OBJParser(Identifier.of(Avaritia.MOD_ID, "models/infinity_trident.obj"))
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

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));

        poseStack.mulPose(Axis.XP.rotationDegrees(0));

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
    public @NotNull Identifier getTextureLocation(@NotNull InfinityThrownTrident thrownTrident) {
        return Res.TRIDENT_TEX;
    }
}
