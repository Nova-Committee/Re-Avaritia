package com.avaritia.client.model.loader.utils.halo;

import com.avaritia.api.client.model.CachedFormat;
import com.avaritia.api.client.model.IVertexConsumer;
import com.avaritia.api.client.model.Quad;
import com.avaritia.api.client.util.VertexUtils;
import com.avaritia.api.client.util.color.ColorARGB;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;

import java.util.ArrayList;
import java.util.List;

/**
 * halo 几何和颜色处理工具。
 * <p>
 * 当前 halo 需要生成圆形扇面 quad，而不是依赖一张方形透明贴图；这样不同大小的 halo 在 GUI/JEI 中不会露出方形边界。
 *
 * @author cnlimiter
 */
public class HaloUtils {
    private static final int MIN_CIRCLE_SEGMENTS = 24;
    private static final int MAX_CIRCLE_SEGMENTS = 64;

    /**
     * 按 halo size 生成近似圆形的 quad 列表，并把颜色直接写入顶点。
     */
    public static List<BakedQuad> generateHaloQuads(final TextureAtlasSprite sprite, final int size, final int color) {
        final float[] colors = new ColorARGB(color).getRGBA();
        final int segments = circleSegments(size);
        final double radius = haloRadius(size);
        final double min = 0.5 - radius;
        final double diameter = radius * 2.0;
        final float minU = sprite.getU0();
        final float maxU = sprite.getU1();
        final float minV = sprite.getV0();
        final float maxV = sprite.getV1();
        final ArrayList<BakedQuad> quads = new ArrayList<>(segments);

        for (int i = 0; i < segments; i++) {
            final double current = Math.PI * 2.0 * i / segments;
            final double next = Math.PI * 2.0 * (i + 1) / segments;
            final double currentX = 0.5 + Math.cos(current) * radius;
            final double currentY = 0.5 + Math.sin(current) * radius;
            final double nextX = 0.5 + Math.cos(next) * radius;
            final double nextY = 0.5 + Math.sin(next) * radius;

            final Quad quad = new Quad();
            quad.reset(CachedFormat.BLOCK);
            quad.setTexture(sprite);
            putHaloVertex(quad.vertices[0], 0.5, 0.5, min, diameter, minU, maxU, minV, maxV);
            putHaloVertex(quad.vertices[1], currentX, currentY, min, diameter, minU, maxU, minV, maxV);
            putHaloVertex(quad.vertices[2], nextX, nextY, min, diameter, minU, maxU, minV, maxV);
            putHaloVertex(quad.vertices[3], 0.5, 0.5, min, diameter, minU, maxU, minV, maxV);
            for (int v = 0; v < 4; ++v) {
                System.arraycopy(colors, 0, quad.vertices[v].color, 0, 4);
            }
            quad.calculateOrientation(true);
            quads.add(quad.bake());
        }

        return quads;
    }

    /**
     * 分段数随 size 增长，但限制上下界，避免小 halo 太粗糙或大 halo 生成过多 quad。
     */
    public static int circleSegments(final int size) {
        return Math.max(MIN_CIRCLE_SEGMENTS, Math.min(MAX_CIRCLE_SEGMENTS, Math.max(1, size) * 4));
    }

    /**
     * 保留旧行为：直径为 1 + 2 * size / 16。
     */
    public static double haloRadius(final int size) {
        return 0.5 + Math.max(0, size) / 16.0;
    }

    private static void putHaloVertex(final Quad.Vertex vx, final double x, final double y, final double min,
                                      final double diameter, final float minU, final float maxU,
                                      final float minV, final float maxV) {
        final double u = (x - min) / diameter;
        final double v = (y - min) / diameter;
        putVertex(vx, x, y, 0.0, minU + u * (maxU - minU), maxV - v * (maxV - minV));
    }

    public static void putVertex(final Quad.Vertex vx, final double x, final double y, final double z, final double u, final double v) {
        vx.vec[0] = (float) x;
        vx.vec[1] = (float) y;
        vx.vec[2] = (float) z;
        vx.uv[0] = (float) u;
        vx.uv[1] = (float) v;
    }

    public static List<BakedQuad> transformQuads(final List<BakedQuad> quads, final IntList layerColors) {
        final ArrayList<BakedQuad> newQuads = new ArrayList<>(quads.size());
        for (final BakedQuad quad : quads) {
            newQuads.add(transformQuad(quad, layerColors));
        }
        return newQuads;
    }

    public static void putBakedQuad(final IVertexConsumer consumer, final BakedQuad quad) {
        VertexUtils.putQuad(consumer, quad);
    }

    public static BakedQuad transformQuad(final BakedQuad quad, final IntList layerColors) {
        final int tintIndex = quad.materialInfo().tintIndex();
        if (tintIndex == -1 || tintIndex >= layerColors.size()) {
            return quad;
        }
        final int tint = layerColors.getInt(tintIndex);
        if (tint == -1) {
            return quad;
        }
        final Quad newQuad = new Quad();
        newQuad.reset(CachedFormat.BLOCK);
        putBakedQuad(newQuad, quad);
        final float r = (tint >> 16 & 255) / 255.0f;
        final float g = (tint >> 8 & 255) / 255.0f;
        final float b = (tint & 255) / 255.0f;
        for (final Quad.Vertex v : newQuad.vertices) {
            v.color[0] *= r;
            v.color[1] *= g;
            v.color[2] *= b;
        }
        newQuad.tintIndex = -1;
        return newQuad.bake();
    }
}
