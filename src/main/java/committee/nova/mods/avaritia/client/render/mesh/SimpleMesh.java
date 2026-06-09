package committee.nova.mods.avaritia.client.render.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

public final class SimpleMesh {
    private final List<Vertex> vertices;

    public SimpleMesh(List<Vertex> vertices) {
        this.vertices = List.copyOf(vertices);
    }

    public List<Vertex> vertices() {
        return this.vertices;
    }

    public int vertexCount() {
        return this.vertices.size();
    }

    public void render(PoseStack.Pose pose, VertexConsumer consumer, int rgba, int packedLight, int packedOverlay) {
        int red = rgba >>> 24 & 0xFF;
        int green = rgba >> 16 & 0xFF;
        int blue = rgba >> 8 & 0xFF;
        int alpha = rgba & 0xFF;

        for (Vertex vertex : this.vertices) {
            consumer.addVertex(pose, vertex.x(), vertex.y(), vertex.z())
                    .setColor(red, green, blue, alpha)
                    .setUv(vertex.u(), vertex.v())
                    .setOverlay(packedOverlay)
                    .setLight(packedLight)
                    .setNormal(pose, vertex.normalX(), vertex.normalY(), vertex.normalZ());
        }
    }

    public record Vertex(float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ) {
    }
}
