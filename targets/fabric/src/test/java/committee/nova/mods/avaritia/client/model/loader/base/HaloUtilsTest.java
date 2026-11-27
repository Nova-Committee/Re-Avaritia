package committee.nova.mods.avaritia.client.model.loader.base;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HaloUtilsTest {
    @Test
    void haloBufferRetainsBlackTintAndTransparentOrColoredVertices() {
        int[] colors = {0xFF000000, 0x00FFFFFF, 0x33C08020, 0x99FFFFFF};
        BufferBuilder builder = new BufferBuilder(256);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        HaloUtils.renderHaloQuad(new PoseStack().last(), builder, quad(colors), 0x00F000F0, 0);
        BufferBuilder.RenderedBuffer rendered = builder.end();
        try {
            var bytes = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
            int stride = DefaultVertexFormat.NEW_ENTITY.getVertexSize();
            for (int i = 0; i < 4; i++) {
                int offset = i * stride + 12;
                assertEquals(colors[i] & 255, Byte.toUnsignedInt(bytes.get(offset)), "red");
                assertEquals(colors[i] >>> 8 & 255, Byte.toUnsignedInt(bytes.get(offset + 1)), "green");
                assertEquals(colors[i] >>> 16 & 255, Byte.toUnsignedInt(bytes.get(offset + 2)), "blue");
                assertEquals(colors[i] >>> 24 & 255, Byte.toUnsignedInt(bytes.get(offset + 3)), "alpha");
            }
        } finally {
            rendered.release();
        }
    }

    @Test
    void haloBufferUsesModelPoseAndKeepsAtlasUvOverlayAndLight() {
        PoseStack pose = new PoseStack();
        pose.translate(2, 3, 4);
        pose.scale(2, 3, 1);
        BufferBuilder builder = new BufferBuilder(256);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        HaloUtils.renderHaloQuad(pose.last(), builder,
                quad(new int[]{-1, -1, -1, -1}), 0x00A000B0, 0x00070008);
        BufferBuilder.RenderedBuffer rendered = builder.end();
        try {
            var bytes = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
            int stride = DefaultVertexFormat.NEW_ENTITY.getVertexSize();
            for (int i = 0; i < 4; i++) {
                int offset = i * stride;
                assertEquals(2 + 2 * (i & 1), bytes.getFloat(offset), 0.00001F);
                assertEquals(3 + 3 * (i >>> 1), bytes.getFloat(offset + 4), 0.00001F);
                assertEquals(4, bytes.getFloat(offset + 8), 0.00001F);
                assertEquals(0.25F, bytes.getFloat(offset + 16), 0.00001F);
                assertEquals(0.75F, bytes.getFloat(offset + 20), 0.00001F);
                assertEquals(0x00070008, bytes.getInt(offset + 24));
                assertEquals(0x00A000B0, bytes.getInt(offset + 28));
                assertEquals(0, bytes.get(offset + 32));
                assertEquals(0, bytes.get(offset + 33));
                assertEquals(127, bytes.get(offset + 34));
            }
        } finally {
            rendered.release();
        }
    }

    private static BakedQuad quad(int[] colors) {
        int[] vertices = new int[4 * DefaultVertexFormat.BLOCK.getIntegerSize()];
        for (int i = 0; i < 4; i++) {
            int offset = i * DefaultVertexFormat.BLOCK.getIntegerSize();
            vertices[offset] = Float.floatToRawIntBits(i & 1);
            vertices[offset + 1] = Float.floatToRawIntBits(i >>> 1);
            vertices[offset + 3] = colors[i];
            vertices[offset + 4] = Float.floatToRawIntBits(0.25F);
            vertices[offset + 5] = Float.floatToRawIntBits(0.75F);
        }
        return new BakedQuad(vertices, -1, Direction.SOUTH, null, false);
    }
}
