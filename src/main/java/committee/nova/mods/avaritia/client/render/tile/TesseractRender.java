package committee.nova.mods.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Renders the animated end-gateway core inside the Tesseract frame. */
public final class TesseractRender implements BlockEntityRenderer<TesseractTile, TesseractRender.State> {
    public TesseractRender(BlockEntityRendererProvider.Context context) { }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector output, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.25F, 0.25F, 0.25F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        // 26.1 exposed a static AbstractEndPortalRenderer.submitSpecial helper. 1.21.11 turned
        // that class into a block-entity renderer whose cube geometry is private, so the same
        // unit cube is emitted directly through submitCustomGeometry.
        output.submitCustomGeometry(poseStack, RenderTypes.endGateway(),
                (pose, buffer) -> renderCube(pose, buffer, state.lightCoords));
        poseStack.popPose();
    }

    private static void renderCube(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        quad(pose, buffer, light, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0);
        quad(pose, buffer, light, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, -1, 0);
        quad(pose, buffer, light, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, -1);
        quad(pose, buffer, light, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, 1);
        quad(pose, buffer, light, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, -1, 0, 0);
        quad(pose, buffer, light, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 0);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int light,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float nx, float ny, float nz) {
        vertex(pose, buffer, light, x0, y0, z0, 0.0F, 0.0F, nx, ny, nz);
        vertex(pose, buffer, light, x1, y1, z1, 0.0F, 1.0F, nx, ny, nz);
        vertex(pose, buffer, light, x2, y2, z2, 1.0F, 1.0F, nx, ny, nz);
        vertex(pose, buffer, light, x3, y3, z3, 1.0F, 0.0F, nx, ny, nz);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, int light,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    public static final class State extends BlockEntityRenderState { }
}
