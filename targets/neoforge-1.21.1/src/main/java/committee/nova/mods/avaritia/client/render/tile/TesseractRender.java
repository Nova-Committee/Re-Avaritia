package committee.nova.mods.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

/**
 * Renders the animated end-gateway core inside a tesseract.
 */
public class TesseractRender implements BlockEntityRenderer<TesseractTile> {

    public TesseractRender(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TesseractTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.endGateway());
        renderFace(pose, consumer, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, 0.75F, 0.75F, 0.75F);
        renderFace(pose, consumer, 0.25F, 0.75F, 0.75F, 0.25F, 0.25F, 0.25F, 0.25F, 0.25F);
        renderFace(pose, consumer, 0.75F, 0.75F, 0.75F, 0.25F, 0.25F, 0.75F, 0.75F, 0.25F);
        renderFace(pose, consumer, 0.25F, 0.25F, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, 0.25F);
        renderFace(pose, consumer, 0.25F, 0.75F, 0.25F, 0.25F, 0.25F, 0.25F, 0.75F, 0.75F);
        renderFace(pose, consumer, 0.25F, 0.75F, 0.75F, 0.75F, 0.75F, 0.75F, 0.25F, 0.25F);
    }

    private static void renderFace(Matrix4f pose, VertexConsumer consumer,
                                   float x1, float x2, float y1, float y2,
                                   float z1, float z2, float z3, float z4) {
        consumer.addVertex(pose, x1, y1, z1);
        consumer.addVertex(pose, x2, y1, z2);
        consumer.addVertex(pose, x2, y2, z3);
        consumer.addVertex(pose, x1, y2, z4);
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
