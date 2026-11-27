package committee.nova.mods.avaritia.client.model.loader;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Matrix3f;
import org.joml.Vector3f;

/** Keeps baked normals unit length after the pulse layer's non-uniform transform. */
final class NormalizedPulseVertexConsumer extends VertexConsumerWrapper {
    NormalizedPulseVertexConsumer(VertexConsumer parent) {
        super(parent);
    }

    @Override
    public void applyBakedNormals(Vector3f generated, BakedNormals data, int vertex, Matrix3f normalTransform) {
        int packed = data.normal(vertex);
        if (!BakedNormals.isUnspecified(packed)) {
            BakedNormals.unpack(packed, generated);
            generated.mul(normalTransform);
            // NeoForge's baked-normal path bypasses PoseStack.Pose.transformNormal's normalization.
            if (generated.lengthSquared() > 0.0F) {
                generated.normalize();
            }
        }
    }
}
