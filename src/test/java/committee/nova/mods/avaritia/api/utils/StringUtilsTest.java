package committee.nova.mods.avaritia.api.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StringUtils 纯 Java 字符串工具")
class StringUtilsTest {

    @Test
    @DisplayName("空值与空白字符串判断保持区分")
    void nullEmptyAndBlankChecksKeepTheirDocumentedSemantics() {
        assertAll(
                () -> assertTrue(StringUtils.isNullOrEmpty(null)),
                () -> assertTrue(StringUtils.isNullOrEmpty("")),
                () -> assertFalse(StringUtils.isNullOrEmpty("   ")),
                () -> assertTrue(StringUtils.isNullOrEmptyEx("   ")),
                () -> assertFalse(StringUtils.isNotNullOrEmpty(null)),
                () -> assertFalse(StringUtils.isNotNullOrEmpty("")),
                () -> assertTrue(StringUtils.isNotNullOrEmpty(" avaritia "))
        );
    }

    @Test
    @DisplayName("replaceLine 支持 HTML 与转义换行符标准化")
    void replaceLineNormalizesSupportedLineBreakMarkers() {
        assertAll(
                () -> assertEquals("", StringUtils.replaceLine(null)),
                () -> assertEquals("alpha\nbeta", StringUtils.replaceLine("alpha<br>beta")),
                () -> assertEquals("alpha\nbeta", StringUtils.replaceLine("alpha\\nbeta")),
                () -> assertEquals("alpha\nbeta", StringUtils.replaceLine("alpha\r\nbeta"))
        );
    }

    @Test
    @DisplayName("intToRoman 处理常见罗马数字减法规则")
    void intToRomanHandlesSubtractiveNotation() {
        assertAll(
                () -> assertEquals("I", StringUtils.intToRoman(1)),
                () -> assertEquals("IV", StringUtils.intToRoman(4)),
                () -> assertEquals("IX", StringUtils.intToRoman(9)),
                () -> assertEquals("LVIII", StringUtils.intToRoman(58)),
                () -> assertEquals("MCMXCIV", StringUtils.intToRoman(1994)),
                () -> assertEquals("", StringUtils.intToRoman(0))
        );
    }

    @Test
    @DisplayName("toInt 对非法输入安全回退为 0")
    void toIntFallsBackToZeroForInvalidInput() {
        assertAll(
                () -> assertEquals(42, StringUtils.toInt(" 42 ")),
                () -> assertEquals(-7, StringUtils.toInt("-7")),
                () -> assertEquals(0, StringUtils.toInt(null)),
                () -> assertEquals(0, StringUtils.toInt("")),
                () -> assertEquals(0, StringUtils.toInt("not-a-number"))
        );
    }
}
