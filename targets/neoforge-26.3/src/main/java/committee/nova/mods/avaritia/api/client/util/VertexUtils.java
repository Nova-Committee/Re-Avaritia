package committee.nova.mods.avaritia.api.client.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import committee.nova.mods.avaritia.api.client.model.IVertexConsumer;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3fc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by covers1624 on 9/7/22.
 */
public class VertexUtils {

    private static final ConcurrentMap<Pair<VertexFormat, VertexFormat>, int[]> formatMaps = new ConcurrentHashMap<>();

    private static final int[] DEFAULT_MAPPING = generateMapping(DefaultVertexFormat.BLOCK, DefaultVertexFormat.BLOCK);

    public static int[] mapFormats(VertexFormat from, VertexFormat to) {
        if (from.equals(DefaultVertexFormat.BLOCK) && to.equals(DefaultVertexFormat.BLOCK)) return DEFAULT_MAPPING;

        return formatMaps.computeIfAbsent(Pair.of(from, to), pair -> generateMapping(pair.getLeft(), pair.getRight()));
    }

    public static void putQuad(IVertexConsumer consumer, BakedQuad quad) {
        BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
        consumer.setTexture(materialInfo.sprite());
        consumer.setQuadOrientation(quad.direction());
        if (materialInfo.isTinted()) {
            consumer.setQuadTint(materialInfo.tintIndex());
        }
        // 26.3 用 shadeDirectionOverride(Direction) 取代了旧的 shade 布尔：
        // null = 按实际朝向着色(旧 shade=true)，Direction.UP = 恒定最亮(旧 shade=false，等价 cardinalLighting.up())。
        consumer.setApplyDiffuseLighting(materialInfo.shadeDirectionOverride() == null);
        VertexFormat formatFrom = consumer.getVertexFormat();
        int countFrom = formatFrom.getElements().size();
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < countFrom; e++) {
                putQuadElement(consumer, quad, v, e);
            }
        }
    }

    private static void putQuadElement(IVertexConsumer consumer, BakedQuad quad, int vertex, int elementIndex) {
        VertexFormatElement element = consumer.getVertexFormat().getElements().get(elementIndex);
        // 26.3 起顶点元素不再有常量标识，统一按 name() 字符串识别语义角色。
        String name = element.name();
        if (DefaultVertexFormat.POSITION_SEMANTIC_NAME.equals(name)) {
            Vector3fc position = quad.position(vertex);
            consumer.put(elementIndex, position.x(), position.y(), position.z(), 1.0F);
        } else if (DefaultVertexFormat.COLOR_SEMANTIC_NAME.equals(name)) {
            int color = quad.bakedColors().color(vertex);
            consumer.put(
                    elementIndex,
                    (color >> 16 & 0xFF) / 255.0F,
                    (color >> 8 & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F,
                    (color >>> 24) / 255.0F
            );
        } else if (DefaultVertexFormat.UV0_SEMANTIC_NAME.equals(name)) {
            long packedUv = quad.packedUV(vertex);
            consumer.put(elementIndex, UVPair.unpackU(packedUv), UVPair.unpackV(packedUv));
        } else if (DefaultVertexFormat.NORMAL_SEMANTIC_NAME.equals(name)) {
            int normal = quad.bakedNormals().normal(vertex);
            if (BakedNormals.isUnspecified(normal)) {
                consumer.put(elementIndex);
            } else {
                consumer.put(
                        elementIndex,
                        BakedNormals.unpackX(normal),
                        BakedNormals.unpackY(normal),
                        BakedNormals.unpackZ(normal),
                        0.0F
                );
            }
        } else {
            consumer.put(elementIndex);
        }
    }

    public static void unpack(int[] from, float[] to, VertexFormat formatFrom, int v, int e) {
        int length = Math.min(4, to.length);
        VertexFormatElement element = formatFrom.getElements().get(e);
        int vertexStart = v * formatFrom.getVertexSize() + element.offset();
        GpuFormat elementFormat = element.format();
        int count = elementFormat.componentCount();
        GpuFormat.ComponentType type = elementFormat.componentType();
        boolean position = DefaultVertexFormat.POSITION_SEMANTIC_NAME.equals(element.name());
        int size = type.byteSize();
        int mask = (256 << (8 * (size - 1))) - 1;
        for (int i = 0; i < length; i++) {
            if (i < count) {
                int pos = vertexStart + size * i;
                int index = pos >> 2;
                int offset = pos & 3;
                int bits = from[index];
                bits = bits >>> (offset * 8);
                if ((pos + size - 1) / 4 != index) {
                    bits |= from[index + 1] << ((4 - offset) * 8);
                }
                bits &= mask;
                // 26.3 以 GpuFormat.ComponentType 取代旧的 VertexFormatElement.Type，按字节宽度/符号性分组保持原有解码数学不变。
                if (type == GpuFormat.ComponentType.FLOAT_32) {
                    to[i] = Float.intBitsToFloat(bits);
                } else if (
                        type == GpuFormat.ComponentType.UNORM_8 ||
                                type == GpuFormat.ComponentType.UINT_8 ||
                                type == GpuFormat.ComponentType.UNORM_16 ||
                                type == GpuFormat.ComponentType.UINT_16
                ) {
                    to[i] = (float) bits / mask;
                } else if (type == GpuFormat.ComponentType.UINT_32) {
                    to[i] = (float) ((double) (bits & 0xFFFFFFFFL) / 0xFFFFFFFFL);
                } else if (type == GpuFormat.ComponentType.SNORM_8 || type == GpuFormat.ComponentType.SINT_8) {
                    to[i] = ((float) (byte) bits) / (mask >> 1);
                } else if (type == GpuFormat.ComponentType.SNORM_16 || type == GpuFormat.ComponentType.SINT_16) {
                    to[i] = ((float) (short) bits) / (mask >> 1);
                } else if (type == GpuFormat.ComponentType.SINT_32) {
                    to[i] = (float) ((double) (bits & 0xFFFFFFFFL) / (0xFFFFFFFFL >> 1));
                }
            } else {
                to[i] = (i == 3 && position) ? 1 : 0;
            }
        }
    }

    public static void pack(float[] from, int[] to, VertexFormat formatTo, int v, int e) {
        VertexFormatElement element = formatTo.getElements().get(e);
        int vertexStart = v * formatTo.getVertexSize() + element.offset();
        GpuFormat elementFormat = element.format();
        int count = elementFormat.componentCount();
        GpuFormat.ComponentType type = elementFormat.componentType();
        int size = type.byteSize();
        int mask = (256 << (8 * (size - 1))) - 1;
        for (int i = 0; i < 4; i++) {
            if (i < count) {
                int pos = vertexStart + size * i;
                int index = pos >> 2;
                int offset = pos & 3;
                int bits = 0;
                float f = i < from.length ? from[i] : 0;
                if (type == GpuFormat.ComponentType.FLOAT_32) {
                    bits = Float.floatToRawIntBits(f);
                } else if (
                        type == GpuFormat.ComponentType.UNORM_8 ||
                                type == GpuFormat.ComponentType.UINT_8 ||
                                type == GpuFormat.ComponentType.UNORM_16 ||
                                type == GpuFormat.ComponentType.UINT_16 ||
                                type == GpuFormat.ComponentType.UINT_32
                ) {
                    bits = Math.round(f * mask);
                } else {
                    bits = Math.round(f * (mask >> 1));
                }
                to[index] &= ~(mask << (offset * 8));
                to[index] |= (((bits & mask) << (offset * 8)));
                // TODO handle overflow into to[index + 1]
            }
        }
    }

    private static int[] generateMapping(VertexFormat from, VertexFormat to) {
        int fromCount = from.getElements().size();
        int toCount = to.getElements().size();
        int[] eMap = new int[fromCount];

        for (int e = 0; e < fromCount; e++) {
            VertexFormatElement expected = from.getElements().get(e);
            int e2;
            for (e2 = 0; e2 < toCount; e2++) {
                VertexFormatElement current = to.getElements().get(e2);
                if (sameElementRole(expected, current)) {
                    break;
                }
            }
            eMap[e] = e2;
        }
        return eMap;
    }

    private static boolean sameElementRole(VertexFormatElement expected, VertexFormatElement current) {
        // 26.3 起元素按 name() 标识语义角色(含 UV 序号)；旧版 UV/UV0 别名在 26.1.2 已是同一常量，无需特判。
        return expected.name().equals(current.name());
    }
}
