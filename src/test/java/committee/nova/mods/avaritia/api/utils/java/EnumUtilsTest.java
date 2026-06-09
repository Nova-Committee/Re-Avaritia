package committee.nova.mods.avaritia.api.utils.java;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EnumUtils 纯 Java 枚举解析工具")
class EnumUtilsTest {

    @Test
    @DisplayName("getEnumFromString 支持多种大小写形式")
    void getEnumFromStringAcceptsCommonCaseStyles() {
        assertAll(
                () -> assertEquals(Optional.of(Sample.EXACT), EnumUtils.getEnumFromString(Sample.class, "EXACT")),
                () -> assertEquals(Optional.of(Sample.EXACT), EnumUtils.getEnumFromString(Sample.class, "exact")),
                () -> assertEquals(Optional.of(Sample.Mixed), EnumUtils.getEnumFromString(Sample.class, "mixed")),
                () -> assertEquals(Optional.of(Sample.lower), EnumUtils.getEnumFromString(Sample.class, "LOWER"))
        );
    }

    @Test
    @DisplayName("getEnumFromString 会 trim 输入并在无匹配时返回 Optional.empty")
    void getEnumFromStringTrimsInputAndReturnsEmptyForInvalidValues() {
        assertAll(
                () -> assertEquals(Optional.of(Sample.EXACT), EnumUtils.getEnumFromString(Sample.class, "  EXACT  ")),
                () -> assertTrue(EnumUtils.getEnumFromString(Sample.class, "").isEmpty()),
                () -> assertTrue(EnumUtils.getEnumFromString(Sample.class, "missing").isEmpty()),
                () -> assertTrue(EnumUtils.getEnumFromString(Sample.class, null).isEmpty()),
                () -> assertTrue(EnumUtils.getEnumFromString(null, "EXACT").isEmpty())
        );
    }

    private enum Sample {
        EXACT,
        Mixed,
        lower
    }
}
