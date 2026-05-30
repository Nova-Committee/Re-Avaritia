package com.avaritia.client.render.entity;

import com.avaritia.Avaritia;
import com.avaritia.Res;
import com.avaritia.api.client.render.CCModel;
import com.avaritia.api.client.render.CCRenderState;
import com.avaritia.api.client.render.model.OBJParser;
import com.avaritia.api.client.render.buffer.TransformingVertexConsumer;
import com.avaritia.api.utils.vec.Matrix4;
import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.common.entity.InfinityThrownTrident;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class InfinityThrownTridentRender extends EntityRenderer<InfinityThrownTrident, ThrownTridentRenderState> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfinityThrownTridentRender.class);
    private final Map<String, CCModel> tridentModel;

    public InfinityThrownTridentRender(EntityRendererProvider.Context context) {
        super(context);
        this.tridentModel = new OBJParser(Identifier.of(Const.MOD_ID, "models/infinity_trident.obj"))
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
    public void submit(ThrownTridentRenderState state, PoseStack poseStack, SubmitNodeCollector output, CameraRenderState cameraState) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

        poseStack.mulPose(Axis.XP.rotationDegrees(0));
        Matrix4 pose = new Matrix4(poseStack);
        output.submitCustomGeometry(poseStack, AvaritiaRenderTypes.TRIDENT, (poseState, vertexConsumer) -> {
            CCRenderState cc = CCRenderState.instance();
            cc.reset();
            cc.bind(new TransformingVertexConsumer(vertexConsumer, pose), AvaritiaRenderTypes.TRIDENT.format());
            cc.baseColour = 0xFFFFFFFF;

            for (CCModel model : tridentModel.values()) {
                model.render(cc);
            }
        });

        poseStack.popPose();
        super.submit(state, poseStack, output, cameraState);
    }

    @Override
    public ThrownTridentRenderState createRenderState() {
        return new ThrownTridentRenderState();
    }

    @Override
    public void extractRenderState(InfinityThrownTrident entity, ThrownTridentRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.isFoil = false;
    }

    public @NotNull Identifier getTextureLocation(@NotNull InfinityThrownTrident thrownTrident) {
        return Res.TRIDENT_TEX;
    }
}
