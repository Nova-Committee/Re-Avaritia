package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.client.render.mesh.SimpleMesh;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
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

import java.util.List;


public class GapingVoidRender extends EntityRenderer<GapingVoidEntity, GapingVoidRender.State> {
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final int WHITE_RGBA = 0xFFFFFFFF;

    private SimpleMesh blackHoleModel;

    public GapingVoidRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@NotNull GapingVoidEntity entity, @NotNull State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.voidAge = entity.getAge() + partialTicks;
        state.absorptionProgress = entity.getAbsorptionProgress();
        state.evaporationProgress = GapingVoidEntity.getEvaporationProgress(state.voidAge);
        state.evaporating = entity.isEvaporating();
    }

    public @NotNull Identifier getTextureLocation(@NotNull GapingVoidEntity p_114482_) {
        return Res.VOID;
    }

    @Override
    public void submit(@NotNull State state, @NotNull PoseStack stack, @NotNull SubmitNodeCollector output, @NotNull CameraRenderState cameraState) {
        final float age = state.voidAge;
        final float scale = (float) (GapingVoidEntity.getVoidScale(age) * 1.35);
        final Vec3 cam = cameraState.pos;
        final double dx = state.x - cam.x();
        final double dy = state.y - cam.y();
        final double dz = state.z - cam.z();
        final float yaw = (float) Math.atan2(dx, dz);
        final float pitch = (float) Math.atan2(Math.sqrt(dx * dx + dz * dz), dy);
        final float absorption = Math.max(0.08F, state.absorptionProgress);
        final float evaporation = Math.max(state.evaporationProgress, state.evaporating ? 0.08F : 0.0F);

        stack.pushPose();
        stack.scale(scale, scale, scale);
        stack.mulPose(cameraState.orientation);
        stack.mulPose(Axis.YP.rotationDegrees(180.0F));

        AvaritiaShaderUniforms.set(AvaritiaRenderTypes.BLACK_HOLE, AvaritiaShaderUniforms.Effect.BLACK_HOLE,
                age, yaw, pitch, absorption, evaporation, null);
        SimpleMesh mesh = this.blackHoleModel();
        output.submitCustomGeometry(stack, AvaritiaRenderTypes.BLACK_HOLE,
                (poseState, vertexConsumer) -> mesh.render(poseState, vertexConsumer, WHITE_RGBA, FULL_BRIGHT, OverlayTexture.NO_OVERLAY));

        stack.popPose();
        super.submit(state, stack, output, cameraState);
    }

    private SimpleMesh blackHoleModel() {
        if (this.blackHoleModel == null) {
            this.blackHoleModel = new SimpleMesh(List.of(
                    new SimpleMesh.Vertex(-1.0F, -1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F),
                    new SimpleMesh.Vertex(1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F),
                    new SimpleMesh.Vertex(1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F),
                    new SimpleMesh.Vertex(-1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F)
            ));
        }
        return this.blackHoleModel;
    }

    public static class State extends EntityRenderState {
        public float voidAge;
        public float absorptionProgress;
        public float evaporationProgress;
        public boolean evaporating;
    }

}
