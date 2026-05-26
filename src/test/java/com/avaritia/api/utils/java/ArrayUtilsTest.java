package com.avaritia.api.utils.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ArrayUtils 纯 Java 数组工具")
class ArrayUtilsTest {

    private String[] words;

    @BeforeEach
    void setUp() {
        words = new String[]{"Alpha", "BETA", "gamma"};
    }

    @Test
    @DisplayName("arrayToLowercase 返回小写副本且不修改源数组")
    void arrayToLowercaseReturnsLowercaseCopy() {
        String[] lower = ArrayUtils.arrayToLowercase(words);

        assertAll(
                () -> assertArrayEquals(new String[]{"alpha", "beta", "gamma"}, lower),
                () -> assertArrayEquals(new String[]{"Alpha", "BETA", "gamma"}, words),
                () -> assertNotSame(words, lower)
        );
    }

    @Test
    @DisplayName("convertKeyValueArrayToMap 解析 key=value 数组")
    void convertKeyValueArrayToMapParsesEntries() {
        Map<String, String> result = ArrayUtils.convertKeyValueArrayToMap(new String[]{"tier=extreme", "count=64"});

        assertAll(
                () -> assertEquals("extreme", result.get("tier")),
                () -> assertEquals("64", result.get("count")),
                () -> assertEquals(2, result.size())
        );
    }

    @Test
    @DisplayName("addToArrayFirstNull 优先填充 null 槽位，必要时扩容")
    void addToArrayFirstNullFillsGapBeforeGrowing() {
        String[] sparse = {"a", null, "c"};
        String[] sameArray = ArrayUtils.addToArrayFirstNull(sparse, "b");
        String[] grown = ArrayUtils.addToArrayFirstNull(new String[]{"a", "b"}, "c");

        assertAll(
                () -> assertSame(sparse, sameArray),
                () -> assertArrayEquals(new String[]{"a", "b", "c"}, sameArray),
                () -> assertArrayEquals(new String[]{"a", "b", "c"}, grown),
                () -> assertEquals(3, grown.length)
        );
    }

    @Test
    @DisplayName("fill 对 Copyable 值逐格复制而不是复用同一实例")
    void fillCopiesCopyableValuesPerSlot() {
        MutableBox prototype = new MutableBox(7);
        MutableBox[] boxes = ArrayUtils.fill(new MutableBox[3], prototype);

        assertAll(
                () -> assertEquals(3, boxes.length),
                () -> assertEquals(7, boxes[0].value),
                () -> assertEquals(7, boxes[1].value),
                () -> assertEquals(7, boxes[2].value),
                () -> assertNotSame(prototype, boxes[0]),
                () -> assertNotSame(boxes[0], boxes[1]),
                () -> assertNotSame(boxes[1], boxes[2])
        );
    }

    @Test
    @DisplayName("集合辅助方法过滤 null 并统计匹配项")
    void collectionHelpersSkipNullAndCountMatches() {
        List<String> target = new ArrayList<>(List.of("seed"));
        List<String> result = ArrayUtils.addAllNoNull(new String[]{"a", null, "b"}, target);

        assertAll(
                () -> assertSame(target, result),
                () -> assertEquals(List.of("seed", "a", "b"), result),
                () -> assertEquals(2, ArrayUtils.countNoNull(new String[]{"a", null, "b", null})),
                () -> assertEquals(2, ArrayUtils.count(new Integer[]{1, 2, 3, 4}, value -> value % 2 == 0)),
                () -> assertTrue(ArrayUtils.isEmpty(new String[]{null, null})),
                () -> assertFalse(ArrayUtils.isEmpty(new String[]{null, "value"}))
        );
    }

    @Test
    @DisplayName("rollArray 支持正负位移并保留原数组")
    void rollArraySupportsPositiveAndNegativeShifts() {
        Integer[] input = {1, 2, 3, 4};

        assertAll(
                () -> assertArrayEquals(new Integer[]{4, 1, 2, 3}, ArrayUtils.rollArray(input, 1)),
                () -> assertArrayEquals(new Integer[]{2, 3, 4, 1}, ArrayUtils.rollArray(input, -1)),
                () -> assertArrayEquals(new Integer[]{1, 2, 3, 4}, input)
        );
    }

    @Test
    @DisplayName("inverse 返回全集中未包含在输入数组里的元素")
    void inverseReturnsElementsMissingFromInput() {
        assertArrayEquals(
                new String[]{"b", "d"},
                ArrayUtils.inverse(new String[]{"a", "c"}, new String[]{"a", "b", "c", "d"})
        );
    }

    private static final class MutableBox implements Copyable<MutableBox> {
        private final int value;

        private MutableBox(int value) {
            this.value = value;
        }

        @Override
        public MutableBox copy() {
            return new MutableBox(value);
        }
    }
}
