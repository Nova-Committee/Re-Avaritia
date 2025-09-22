package committee.nova.mods.avaritia.api.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * @author: cnlimiter
 */
public class GsonUtils {
    /**
     * 从 JsonElement 中获取浮点数数组
     *
     * @param element JSON 元素
     * @param expectedLength 期望的数组长度
     * @return 浮点数数组
     * @throws IllegalArgumentException 如果元素不是数组或长度不匹配
     */
    public static float[] getAsFloatArray(JsonElement element, int expectedLength) {
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Expected JSON array, got " + element.getClass().getSimpleName());
        }

        // 将 JsonArray 转换为浮点数数组
        JsonArray array = element.getAsJsonArray();
        if (array.size() != expectedLength) {
            throw new IllegalArgumentException("Expected array of length " + expectedLength + ", got " + array.size());
        }

        float[] result = new float[expectedLength];
        for (int i = 0; i < expectedLength; i++) {
            result[i] = array.get(i).getAsFloat();
        }

        return result;
    }
}
