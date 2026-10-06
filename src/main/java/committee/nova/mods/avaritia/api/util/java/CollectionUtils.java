package committee.nova.mods.avaritia.api.util.java;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author cnlimiter
 */
public class CollectionUtils {
    public static boolean isNullOrEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNotNullOrEmpty(Collection<?> list) {
        return !isNullOrEmpty(list);
    }

    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotNullOrEmpty(Object[] array) {
        return !isNullOrEmpty(array);
    }

    public static boolean isNullOrEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean equals(Object[] source, Object[] target) {
        if (!isNullOrEmpty(source) && !isNullOrEmpty(target)) {
            Object[] sourceEx = source.clone();
            Object[] targetEx = target.clone();
            Arrays.sort(sourceEx);
            Arrays.sort(targetEx);
            return Arrays.equals(sourceEx, targetEx);
        } else {
            return true;
        }
    }

    public static boolean contains(final String[] keys, String targetValue) {
        if (keys == null || keys.length == 0) {
            return false;
        }
        return Arrays.asList(keys).contains(targetValue);
    }

    public static boolean contains(final String[] source, final String[] target) {
        List<String> sourceList = Arrays.asList(source);
        List<String> targetList = Arrays.asList(target);
        return new HashSet<>(sourceList).containsAll(targetList);
    }

    public static String[] setArrayPrefix(String[] array, String prefix) {
        String[] result = null;
        if (!isNullOrEmpty(array)) {
            result = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = prefix + array[i];
            }
        }
        return result;
    }

    public static String[] setArraySuffix(String[] array, String suffix) {
        String[] result = null;
        if (!isNullOrEmpty(array)) {
            result = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = array[i] + suffix;
            }
        }
        return result;
    }

    public static String getMinLengthChar(String[] array) {
        String result = null;
        if (!CollectionUtils.isNullOrEmpty(array)) {
            result = Arrays.stream(array).min(Comparator.comparing(String::length)).orElse(null);
        }
        return result;
    }

    /**
     * 合并数组
     *
     * @param arrays 数据变量
     * @param <T>    对象类型
     */
    public static <T> T[] mergeArrays(T[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            throw new IllegalArgumentException("Input arrays must not be null or empty.");
        }
        int totalLength = 0;
        for (T[] array : arrays) {
            totalLength += array.length;
        }
        T[] mergedArray = Arrays.copyOf(arrays[0], totalLength);
        int destPos = arrays[0].length;
        for (int i = 1; i < arrays.length; i++) {
            System.arraycopy(arrays[i], 0, mergedArray, destPos, arrays[i].length);
            destPos += arrays[i].length;
        }
        return mergedArray;
    }

    /**
     * 从给定的集合中随机选取一个元素。
     *
     * @param <T>      集合中元素的类型
     * @param elements 要从中选取随机元素的集合
     * @return 随机选取的元素
     */
    public static <T> T getRandomElement(T[] elements) {
        return getRandomElement(elements, ThreadLocalRandom.current());
    }

    /**
     * 从给定的集合中随机选取一个元素。
     *
     * @param <T>      集合中元素的类型
     * @param elements 要从中选取随机元素的集合
     * @param random   用于生成随机数的随机数生成器
     * @return 随机选取的元素
     */
    public static <T> T getRandomElement(T[] elements, Random random) {
        if (elements == null || elements.length == 0) {
            return null;
        }
        int index = random.nextInt(elements.length);
        return elements[index];
    }

    /**
     * 从给定的集合中随机选取一个元素。
     *
     * @param <T>      集合中元素的类型
     * @param elements 要从中选取随机元素的集合
     * @return 随机选取的元素
     */
    public static <T> T getRandomElement(Collection<T> elements) {
        return getRandomElement(elements, ThreadLocalRandom.current());
    }

    /**
     * 从给定的集合中随机选取一个元素。
     *
     * @param <T>      集合中元素的类型
     * @param elements 要从中选取随机元素的集合
     * @param random   用于生成随机数的随机数生成器
     * @return 随机选取的元素
     */
    public static <T> T getRandomElement(Collection<T> elements, Random random) {
        if (elements == null || elements.isEmpty()) {
            return null;
        }

        int index = random.nextInt(elements.size());
        return getNthElement(elements, index);
    }

    /**
     * 获取集合中指定索引位置的元素。
     *
     * @param <T>      集合中元素的类型
     * @param elements 要从中获取元素的集合
     * @param index    要获取的元素的索引位置
     * @return 指定索引位置的元素
     */
    private static <T> T getNthElement(Collection<T> elements, int index) {
        int currentIndex = 0;
        for (T element : elements) {
            if (currentIndex == index) {
                return element;
            }
            currentIndex++;
        }
        // This should never happen due to the size check in getRandomElement.
        throw new IllegalStateException("Could not find element at the specified index.");
    }
}
