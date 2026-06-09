package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.util.color.Color;
import committee.nova.mods.avaritia.api.client.util.color.ColorRGBA;
import committee.nova.mods.avaritia.client.render.mesh.SimpleMesh;
import committee.nova.mods.avaritia.client.render.mesh.SimpleObjMeshLoader;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.common.entity.GapingVoidEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;


public class GapingVoidRender extends EntityRenderer<GapingVoidEntity, GapingVoidRender.State> {
    private SimpleMesh hemisphereModel;

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
        int rgba = color.rgba();
        SimpleMesh mesh = this.hemisphereModel();
        output.submitCustomGeometry(stack, AvaritiaRenderTypes.VOID,
                (poseState, vertexConsumer) -> mesh.render(poseState, vertexConsumer, rgba, 0, OverlayTexture.NO_OVERLAY));

        stack.popPose();
        super.submit(state, stack, output, cameraState);
    }

    private SimpleMesh hemisphereModel() {
        if (this.hemisphereModel == null) {
            this.hemisphereModel = SimpleObjMeshLoader.load(Const.rl("models/hemisphere.obj")).get("model");
            if (this.hemisphereModel == null) {
                throw new IllegalStateException("Missing hemisphere OBJ part: model");
            }
        }
        return this.hemisphereModel;
    }

    public static class State extends EntityRenderState {
        public float voidAge;
    }

}
