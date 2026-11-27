package committee.nova.mods.avaritia.api.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static committee.nova.mods.avaritia.api.utils.DateUtils.DateUnit.DAY;
import static committee.nova.mods.avaritia.api.utils.DateUtils.DateUnit.HOUR;
import static committee.nova.mods.avaritia.api.utils.DateUtils.DateUnit.MILLISECOND;
import static committee.nova.mods.avaritia.api.utils.DateUtils.DateUnit.MINUTE;
import static committee.nova.mods.avaritia.api.utils.DateUtils.DateUnit.SECOND;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("DateUtils 纯 Java 时间格式化工具")
class DateUtilsTest {

    private Locale originalLocale;

    @BeforeEach
    void setUp() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.ROOT);
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(originalLocale);
    }

    @Test
    @DisplayName("toMaxUnitString 保留已经足够短的当前单位")
    void toMaxUnitStringKeepsCurrentUnitWhenIntegerPartFits() {
        assertAll(
                () -> assertEquals("999ms", DateUtils.toMaxUnitString(999, MILLISECOND, 0, 3)),
                () -> assertEquals("59s", DateUtils.toMaxUnitString(59, SECOND, 0, 2)),
                () -> assertEquals("23h", DateUtils.toMaxUnitString(23, HOUR, 0, 2))
        );
    }

    @Test
    @DisplayName("toMaxUnitString 递归提升到更大的时间单位")
    void toMaxUnitStringPromotesToLargestReadableUnit() {
        assertAll(
                () -> assertEquals("1.50s", DateUtils.toMaxUnitString(1500, MILLISECOND, 2, 2)),
                () -> assertEquals("2.78h", DateUtils.toMaxUnitString(10000, SECOND, 2, 2)),
                () -> assertEquals("2.00d", DateUtils.toMaxUnitString(48, HOUR, 2, 1))
        );
    }

    @Test
    @DisplayName("toMaxUnitString 到达最大单位后使用 9+ 截断标记")
    void toMaxUnitStringUsesOverflowMarkerAtLargestUnit() {
        assertAll(
                () -> assertEquals("99+d", DateUtils.toMaxUnitString(1000, DAY, 0, 2)),
                () -> assertEquals("9.99+d", DateUtils.toMaxUnitString(1000, DAY, 2, 1))
        );
    }

    @Test
    @DisplayName("DateUnit 可按 code 查找并拒绝非法 code")
    void dateUnitLookupByCodeValidatesInput() {
        assertAll(
                () -> assertEquals(5, DateUtils.DateUnit.getMaxCode()),
                () -> assertEquals(SECOND, DateUtils.DateUnit.valueOf(2)),
                () -> assertEquals(MINUTE, DateUtils.DateUnit.valueOf(3)),
                () -> assertThrows(IllegalArgumentException.class, () -> DateUtils.DateUnit.valueOf(99))
        );
    }
}
