package committee.nova.mods.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;

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
        AbstractEndPortalRenderer.submitSpecial(RenderTypes.endGateway(), poseStack, output);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    public static final class State extends BlockEntityRenderState { }
}
