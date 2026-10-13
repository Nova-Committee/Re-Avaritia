package committee.nova.mods.avaritia.client.model.loader.base;

import com.mojang.blaze3d.platform.NativeImage;
import committee.nova.mods.avaritia.api.client.model.CachedFormat;
import committee.nova.mods.avaritia.api.client.model.Quad;
import committee.nova.mods.avaritia.client.model.loader.utils.halo.HaloUtils;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 26.3 起 halo 改为直接生成圆形扇面 BakedQuad（旧 renderHaloQuad/BufferBuilder 路径已删除），
 * 原"顶点颜色保留"断言无对应目标：颜色写入 Quad.Vertex.color 但 bake() 生成的 BakedQuad 不再携带颜色。
 * 这里改为校验新 API 的分段数、半径、putVertex 写入以及扇面几何/UV 语义。
 */
@DisplayName("Halo utils")
class HaloUtilsTest {
    private static final float EPSILON = 0.0001F;

    @Test
    @DisplayName("circle segment count grows with size and stays clamped")
    void circleSegmentsClamped() {
        assertAll(
                () -> assertEquals(24, HaloUtils.circleSegments(1)),
                () -> assertEquals(24, HaloUtils.circleSegments(4)),
                () -> assertEquals(32, HaloUtils.circleSegments(8)),
                () -> assertEquals(64, HaloUtils.circleSegments(16)),
                () -> assertEquals(64, HaloUtils.circleSegments(1024))
        );
    }

    @Test
    @DisplayName("halo radius keeps legacy diameter formula")
    void haloRadiusKeepsLegacyFormula() {
        assertAll(
                () -> assertEquals(0.5, HaloUtils.haloRadius(0), 1.0E-6),
                () -> assertEquals(1.0, HaloUtils.haloRadius(8), 1.0E-6),
                () -> assertEquals(1.5, HaloUtils.haloRadius(16), 1.0E-6)
        );
    }

    @Test
    @DisplayName("putVertex writes position and uv into the vertex")
    void putVertexWritesPositionAndUv() {
        Quad.Vertex vertex = new Quad.Vertex(CachedFormat.BLOCK);
        HaloUtils.putVertex(vertex, 1.0, 2.0, 3.0, 0.25, 0.75);
        assertAll(
                () -> assertEquals(1.0F, vertex.vec[0], EPSILON),
                () -> assertEquals(2.0F, vertex.vec[1], EPSILON),
                () -> assertEquals(3.0F, vertex.vec[2], EPSILON),
                () -> assertEquals(0.25F, vertex.uv[0], EPSILON),
                () -> assertEquals(0.75F, vertex.uv[1], EPSILON)
        );
    }

    @Test
    @DisplayName("generateHaloQuads bakes one fan quad per segment with center and rim vertices")
    void generateHaloQuadsBakesCircleFan() {
        int size = 8;
        int color = 0x33C08020; // 颜色写入顶点但 26.3 的 BakedQuad 不再保留，这里只验证几何与 UV
        NativeImage image = new NativeImage(16, 16, false);
        SpriteContents contents = new SpriteContents(
                Identifier.withDefaultNamespace("halo_utils_test"),
                new FrameSize(16, 16),
                image
        );
        try (TextureAtlasSprite sprite = new TestSprite(contents)) {
            List<BakedQuad> quads = HaloUtils.generateHaloQuads(sprite, size, color);
            int segments = HaloUtils.circleSegments(size);
            double radius = HaloUtils.haloRadius(size);
            assertEquals(segments, quads.size());

            float midU = (sprite.getU0() + sprite.getU1()) / 2.0F;
            float midV = (sprite.getV0() + sprite.getV1()) / 2.0F;
            for (int i = 0; i < segments; i++) {
                BakedQuad quad = quads.get(i);
                double current = Math.PI * 2.0 * i / segments;
                double next = Math.PI * 2.0 * (i + 1) / segments;
                int index = i;
                assertAll(
                        // 顶点 0/3 是圆心
                        () -> assertPosition(quad, 0, 0.5, 0.5, 0.0, "center0 of quad " + index),
                        () -> assertPosition(quad, 3, 0.5, 0.5, 0.0, "center3 of quad " + index),
                        // 顶点 1/2 落在圆周上
                        () -> assertPosition(quad, 1, 0.5 + Math.cos(current) * radius, 0.5 + Math.sin(current) * radius, 0.0, "rim1 of quad " + index),
                        () -> assertPosition(quad, 2, 0.5 + Math.cos(next) * radius, 0.5 + Math.sin(next) * radius, 0.0, "rim2 of quad " + index),
                        // 圆心 UV 是贴图 UV 区间中点
                        () -> assertEquals(midU, UVPair.unpackU(quad.packedUV0()), EPSILON, "center u of quad " + index),
                        () -> assertEquals(midV, UVPair.unpackV(quad.packedUV0()), EPSILON, "center v of quad " + index)
                );
            }
        }
    }

    private static void assertPosition(BakedQuad quad, int vertex, double x, double y, double z, String label) {
        assertAll(
                () -> assertEquals((float) x, quad.position(vertex).x(), EPSILON, label + " x"),
                () -> assertEquals((float) y, quad.position(vertex).y(), EPSILON, label + " y"),
                () -> assertEquals((float) z, quad.position(vertex).z(), EPSILON, label + " z")
        );
    }

    private static final class TestSprite extends TextureAtlasSprite {
        private TestSprite(SpriteContents contents) {
            super(TextureAtlas.LOCATION_BLOCKS, contents, 256, 256, 32, 64, 0);
        }
    }
}
