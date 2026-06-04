package com.avaritia.client.model.loader.base;

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
 * @author cnlimiter
 */
public class HaloUtils {
    public static BakedQuad generateHaloQuad(final TextureAtlasSprite sprite, final int size, final int color) {
        final float[] colors = new ColorARGB(color).getRGBA();
        final double spread = size / 16.0;
        final double min = 0.0 - spread;
        final double max = 1.0 + spread;
        final float minU = sprite.getU0();
        final float maxU = sprite.getU1();
        final float minV = sprite.getV0();
        final float maxV = sprite.getV1();
        final Quad quad = new Quad();
        quad.reset(CachedFormat.BLOCK);
        quad.setTexture(sprite);
        putVertex(quad.vertices[0], max, max, 0.0, maxU, minV);
        putVertex(quad.vertices[1], min, max, 0.0, minU, minV);
        putVertex(quad.vertices[2], min, min, 0.0, minU, maxV);
        putVertex(quad.vertices[3], max, min, 0.0, maxU, maxV);
        for (int i = 0; i < 4; ++i) {
            System.arraycopy(colors, 0, quad.vertices[i].color, 0, 4);
        }
        quad.calculateOrientation(true);
        return quad.bake();
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
