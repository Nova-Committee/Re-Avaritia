package com.avaritia.client.render.entity;

import com.avaritia.Const;

import com.avaritia.Avaritia;
import com.avaritia.Res;
import com.avaritia.api.client.render.CCRenderState;
import com.avaritia.api.client.render.buffer.TransformingVertexConsumer;
import com.avaritia.api.client.render.model.OBJParser;
import com.avaritia.api.utils.vec.Matrix4;
import com.avaritia.api.client.util.color.Color;
import com.avaritia.api.client.util.color.ColorRGBA;
import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.common.entity.GapingVoidEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;


@OnlyIn(Dist.CLIENT)
public class GapingVoidRender extends EntityRenderer<GapingVoidEntity, GapingVoidRender.State> {
    public GapingVoidRender(EntityRendererProvider.Context context) {
        super(context);
    }

    public static Color getColour(final double age, final double a) {
        final double l = age / 186.0;
        double f = Math.max(0.0, (l - 0.95) / 0.050000000000000044);
        f = Math.max(f, 1.0 - l * 30.0);
        return new ColorRGBA(f, f, f, a);
    }

    @Override
    public @NotNull State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@NotNull GapingVoidEntity entity, @NotNull State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.voidAge = entity.getAge() + partialTicks;
    }

    public @NotNull Identifier getTextureLocation(@NotNull GapingVoidEntity p_114482_) {
        return Res.VOID;
    }

    @Override
    public void submit(@NotNull State state, @NotNull PoseStack stack, @NotNull SubmitNodeCollector output, @NotNull CameraRenderState cameraState) {
        final float age = state.voidAge;
        final Color color = getColour(age, 1.0);
        final double scale = GapingVoidEntity.getVoidScale(age);
        double halocoord = 0.58 * scale;
        final double haloScaleDist = 2.2 * scale;
        final Vec3 cam = cameraState.pos;
        final double dx = state.x - cam.x();
        final double dy = state.y - cam.y();
        final double dz = state.z - cam.z();
        final double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (len <= haloScaleDist) {
            final double close = (haloScaleDist - len) / haloScaleDist;
            halocoord *= 1.0 + close * close * close * close * 1.5;
        }

        stack.pushPose();

        stack.mulPose(Axis.YP.rotationDegrees((float) (Math.atan2(dx, dz) * 57.29577951308232)));
        stack.mulPose(Axis.XP.rotationDegrees((float) (Math.atan2(Math.sqrt(dx * dx + dz * dz), dy) * 57.29577951308232 + 90.0)));

        stack.pushPose();
        stack.mulPose(Axis.XP.rotationDegrees(90.0f));

        stack.translate(0, 0, 0);

//        buffer.addVertex(stack.last().pose(), negCoord, 0.0f, negCoord)
//                .setColor(r, g, b, a)
//                .setUv(0.0f, 0.0f)
//                .setOverlay(0)
//                .setLight(packedLightIn)
//                .setNormal(stack.last(), 0.0f, 1.0f, 0.0f);
//
//        buffer.addVertex(stack.last().pose(), negCoord, 0.0f, posCoord)
//                .setColor(r, g, b, a)
//                .setUv(0.0f, 1.0f)
//                .setOverlay(0)
//                .setLight(packedLightIn)
//                .setNormal(stack.last(), 0.0f, 1.0f, 0.0f);
//
//        buffer.addVertex(stack.last().pose(), posCoord, 0.0f, posCoord)
//                .setColor(r, g, b, a)
//                .setUv(1.0f, 1.0f)
//                .setOverlay(0)
//                .setLight(packedLightIn)
//                .setNormal(stack.last(), 0.0f, 1.0f, 0.0f);
//
//        buffer.addVertex(stack.last().pose(), posCoord, 0.0f, negCoord)
//                .setColor(r, g, b, a)
//                .setUv(1.0f, 0.0f)
//                .setOverlay(0)
//                .setLight(packedLightIn)
//                .setNormal(stack.last(), 0.0f, 1.0f, 0.0f);

        stack.popPose();

        stack.scale((float) scale, (float) scale, (float) scale);
        Matrix4 pose = new Matrix4(stack);
        int rgba = color.rgba();
        output.submitCustomGeometry(stack, AvaritiaRenderTypes.VOID, (poseState, vertexConsumer) -> {
            final CCRenderState cc = CCRenderState.instance();
            cc.reset();
            cc.bind(new TransformingVertexConsumer(vertexConsumer, pose), AvaritiaRenderTypes.VOID.format());
            cc.baseColour = rgba;
            new OBJParser(Identifier.fromNamespaceAndPath(Const.MOD_ID, "models/hemisphere.obj")).parse().get("model").render(cc);
        });

        stack.popPose();
        super.submit(state, stack, output, cameraState);
    }

    public static class State extends EntityRenderState {
        public float voidAge;
    }

}
