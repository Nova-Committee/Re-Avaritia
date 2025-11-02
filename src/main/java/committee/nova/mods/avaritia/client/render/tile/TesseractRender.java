package committee.nova.mods.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;

/**
 * @author cnlimiter
 */
public class TesseractRender implements BlockEntityRenderer<BlockEntity> {

    public TesseractRender(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntity t, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.endGateway());
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, 0.75F, 0.75F, 0.75F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.75F, 0.25F, 0.25F, 0.25F, 0.25F, 0.25F);
        renderFace(matrix4f, consumer, 0.75F, 0.75F, 0.75F, 0.25F, 0.25F, 0.75F, 0.75F, 0.25F);
        renderFace(matrix4f, consumer, 0.25F, 0.25F, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, 0.25F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.25F, 0.25F, 0.25F, 0.25F, 0.75F, 0.75F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.75F, 0.75F, 0.75F, 0.75F, 0.25F, 0.25F);
    }

    private void renderFace(Matrix4f matrix4f, VertexConsumer consumer, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8) {
        consumer.vertex(matrix4f, v1, v3, v5).endVertex();
        consumer.vertex(matrix4f, v2, v3, v6).endVertex();
        consumer.vertex(matrix4f, v2, v4, v7).endVertex();
        consumer.vertex(matrix4f, v1, v4, v8).endVertex();
    }

    public int getViewDistance() {
        return 32;
    }
}
