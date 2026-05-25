package com.avaritia.client.model.loader.base;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.avaritia.api.client.model.CachedFormat;
import com.avaritia.api.client.model.IVertexConsumer;
import com.avaritia.api.client.model.Quad;
import com.avaritia.api.client.util.color.ColorARGB;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.RenderTypeGroup;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author cnlimiter
 */
public class HaloUtils {
    private static final int[] DEFAULT_MAPPING = generateMapping(DefaultVertexFormat.BLOCK, DefaultVertexFormat.BLOCK);
    private static final ConcurrentMap<Pair<VertexFormat, VertexFormat>, int[]> formatMaps = new ConcurrentHashMap<>();

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

    public static BakedModel tintLayers(final BakedModel model, final IntList layerColors) {
        if (layerColors.isEmpty()) {
            return model;
        }
        final Map<Direction, List<BakedQuad>> faceQuads = new HashMap<>();
        for (final Direction face : Direction.values()) {
            faceQuads.put(face, transformQuads(model.getQuads(null, face, RandomSource.create()), layerColors));
        }
        final List<BakedQuad> unculled = transformQuads(model.getQuads(null, null, RandomSource.create()), layerColors);
        return new SimpleBakedModel(unculled, faceQuads, model.useAmbientOcclusion(), model.usesBlockLight(), model.isGui3d(), model.getParticleIcon(), model.getTransforms(), ItemOverrides.EMPTY, RenderTypeGroup.EMPTY);
    }

    public static List<BakedQuad> transformQuads(final List<BakedQuad> quads, final IntList layerColors) {
        final ArrayList<BakedQuad> newQuads = new ArrayList<>(quads.size());
        for (final BakedQuad quad : quads) {
            newQuads.add(transformQuad(quad, layerColors));
        }
        return newQuads;
    }

    public static int[] mapFormats(final VertexFormat from, final VertexFormat to) {
        if (from.equals(DefaultVertexFormat.BLOCK) && to.equals(DefaultVertexFormat.BLOCK)) {
            return DEFAULT_MAPPING;
        }
        return formatMaps.computeIfAbsent(Pair.of(from, to), pair -> generateMapping(pair.getLeft(), pair.getRight()));
    }

    public static void unpack(final int[] from, final float[] to, final VertexFormat formatFrom, final int v, final int e) {
        final int length = Math.min(4, to.length);
        final VertexFormatElement element = formatFrom.getElements().get(e);
        final int vertexStart = v * formatFrom.getVertexSize() + formatFrom.getOffset(element);
        final int count = element.count();
        final VertexFormatElement.Type type = element.type();
        final VertexFormatElement.Usage usage = element.usage();
        final int size = type.size();
        final int mask = (256 << 8 * (size - 1)) - 1;
        for (int i = 0; i < length; ++i) {
            if (i < count) {
                final int pos = vertexStart + size * i;
                final int index = pos >> 2;
                final int offset = pos & 0x3;
                int bits = from[index];
                bits >>>= offset * 8;
                if ((pos + size - 1) / 4 != index) {
                    bits |= from[index + 1] << (4 - offset) * 8;
                }
                bits &= mask;
                if (type == VertexFormatElement.Type.FLOAT) {
                    to[i] = Float.intBitsToFloat(bits);
                } else if (type == VertexFormatElement.Type.UBYTE || type == VertexFormatElement.Type.USHORT) {
                    to[i] = bits / (float) mask;
                } else if (type == VertexFormatElement.Type.UINT) {
                    to[i] = (float) (((long) bits & 0xFFFFFFFFL) / 4.294967295E9);
                } else if (type == VertexFormatElement.Type.BYTE) {
                    to[i] = (byte) bits / (float) (mask >> 1);
                } else if (type == VertexFormatElement.Type.SHORT) {
                    to[i] = (short) bits / (float) (mask >> 1);
                } else if (type == VertexFormatElement.Type.INT) {
                    to[i] = (float) (((long) bits & 0xFFFFFFFFL) / 2.147483647E9);
                }
            } else {
                to[i] = ((i == 3 && usage == VertexFormatElement.Usage.POSITION) ? 1.0f : 0.0f);
            }
        }
    }

    private static int[] generateMapping(final VertexFormat from, final VertexFormat to) {
        final int fromCount = from.getElements().size();
        final int toCount = to.getElements().size();
        final int[] eMap = new int[fromCount];
        for (int e = 0; e < fromCount; ++e) {
            final VertexFormatElement expected = from.getElements().get(e);
            int e2;
            for (e2 = 0; e2 < toCount; ++e2) {
                final VertexFormatElement current = to.getElements().get(e2);
                if (expected.usage() == current.usage() && expected.index() == current.index()) {
                    break;
                }
            }
            eMap[e] = e2;
        }
        return eMap;
    }

    public static void putBakedQuad(final IVertexConsumer consumer, final BakedQuad quad) {
        consumer.setTexture(quad.getSprite());
        consumer.setQuadOrientation(quad.getDirection());
        if (quad.isTinted()) {
            consumer.setQuadTint(quad.getTintIndex());
        }
        consumer.setApplyDiffuseLighting(quad.isShade());
        final float[] data = new float[4];
        final VertexFormat formatFrom = consumer.getVertexFormat();
        final VertexFormat formatTo = DefaultVertexFormat.BLOCK;
        final int countFrom = formatFrom.getElements().size();
        final int countTo = formatTo.getElements().size();
        final int[] eMap = mapFormats(formatFrom, formatTo);
        for (int v = 0; v < 4; ++v) {
            for (int e = 0; e < countFrom; ++e) {
                if (eMap[e] != countTo) {
                    unpack(quad.getVertices(), data, formatTo, v, eMap[e]);
                    consumer.put(e, data);
                } else {
                    consumer.put(e);
                }
            }
        }
    }

    public static BakedQuad transformQuad(final BakedQuad quad, final IntList layerColors) {
        final int tintIndex = quad.getTintIndex();
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
