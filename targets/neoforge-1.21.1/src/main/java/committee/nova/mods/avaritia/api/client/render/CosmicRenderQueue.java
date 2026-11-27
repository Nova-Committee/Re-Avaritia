package committee.nova.mods.avaritia.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class CosmicRenderQueue {

    private static final List<CosmicRenderCall> QUEUE = new ArrayList<>();

    public static void enqueue(CosmicRenderCall call) {
        QUEUE.add(call);
    }

    public static void renderAll() {

        if (QUEUE.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        MultiBufferSource.BufferSource source =
                mc.renderBuffers().bufferSource();

        Matrix4f oldProjection =
                new Matrix4f(RenderSystem.getProjectionMatrix());

        Matrix4f oldModelView =
                new Matrix4f(RenderSystem.getModelViewMatrix());

        for (CosmicRenderCall call : QUEUE) {

            RenderSystem.setProjectionMatrix(
                    call.projection,
                    RenderSystem.getVertexSorting()
            );

            RenderSystem.getModelViewStack().set(call.modelView);

            RenderSystem.applyModelViewMatrix();

            PoseStack poseStack = new PoseStack();

            poseStack.last().pose().set(call.pose);
            poseStack.last().normal().set(call.normal);

            call.model.renderCosmicLayer(
                    call.stack,
                    call.context,
                    poseStack,
                    source,
                    call.light,
                    call.overlay
            );
        }

        source.endBatch();

        RenderSystem.setProjectionMatrix(
                oldProjection,
                RenderSystem.getVertexSorting()
        );

        RenderSystem.getModelViewStack().set(oldModelView);

        RenderSystem.applyModelViewMatrix();

        QUEUE.clear();
    }
}