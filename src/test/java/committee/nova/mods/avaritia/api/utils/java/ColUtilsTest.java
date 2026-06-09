package committee.nova.mods.avaritia.api.utils.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ColUtils 纯 Java 集合工具")
class ColUtilsTest {

    private List<String> names;

    @BeforeEach
    void setUp() {
        names = List.of("neutron", "infinity", "crystal");
    }

    @Test
    @DisplayName("slice 会钳制边界并返回同类型数组")
    void sliceClampsBoundsAndKeepsArrayType() {
        String[] values = {"a", "b", "c", "d"};

        assertAll(
                () -> assertArrayEquals(new String[]{"a", "b"}, ColUtils.slice(values, -3, 2)),
                () -> assertArrayEquals(new String[]{"c", "d"}, ColUtils.slice(values, 2, 99)),
                () -> assertArrayEquals(new String[0], ColUtils.slice(values, 3, 1))
        );
    }

    @Test
    @DisplayName("max/head/tail/findFirst 返回预期集合元素")
    void elementSelectionHelpersReturnExpectedValues() {
        assertAll(
                () -> assertEquals("infinity", ColUtils.requireMaxBy(names, String::length)),
                () -> assertEquals("neutron", ColUtils.head(names)),
                () -> assertEquals("crystal", ColUtils.tail(names)),
                () -> assertEquals(Optional.of("infinity"), ColUtils.findFirst(names, value -> value.startsWith("inf"))),
                () -> assertEquals(Optional.empty(), ColUtils.findFirst(names, value -> value.startsWith("missing")))
        );
    }

    @Test
    @DisplayName("match 与 containsKeys 正确处理全匹配、任意匹配和缺失键")
    void predicateAndMapHelpersValidateCollections() {
        Map<String, Integer> counts = Map.of("neutron", 1, "infinity", 2);

        assertAll(
                () -> assertTrue(ColUtils.allMatch(names, value -> value.length() >= 7)),
                () -> assertFalse(ColUtils.allMatch(names, value -> value.contains("i"))),
                () -> assertTrue(ColUtils.anyMatch(names, value -> value.equals("crystal"))),
                () -> assertFalse(ColUtils.anyMatch(names, value -> value.equals("singularity"))),
                () -> assertTrue(ColUtils.containsKeys(counts, "neutron", "infinity")),
                () -> assertFalse(ColUtils.containsKeys(counts, "neutron", "crystal"))
        );
    }

    @Test
    @DisplayName("only 要求 Iterable 恰好包含一个元素")
    void onlyRequiresExactlyOneElement() {
        assertAll(
                () -> assertEquals("single", ColUtils.only(List.of("single"))),
                () -> assertThrows(IllegalArgumentException.class, () -> ColUtils.only(List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> ColUtils.only(List.of("a", "b"))),
                () -> assertEquals("fallback", ColUtils.onlyOrDefault(List.of(), "fallback")),
                () -> assertEquals("fallback", ColUtils.onlyOrDefault(List.of("a", "b"), "fallback"))
        );
    }

    @Test
    @DisplayName("iterator 支持数组切片遍历")
    void iteratorWalksRequestedArraySlice() {
        Iterator<String> iterator = ColUtils.iterator(new String[]{"a", "b", "c", "d"}, 1, 3);
        List<String> seen = new ArrayList<>();
        iterator.forEachRemaining(seen::add);

        assertEquals(List.of("b", "c"), seen);
    }

    @Test
    @DisplayName("reverse 原地反转数组")
    void reverseMutatesArrayInPlace() {
        Integer[] values = {1, 2, 3, 4};

        ColUtils.reverse(values);

        assertArrayEquals(new Integer[]{4, 3, 2, 1}, values);
    }
}
