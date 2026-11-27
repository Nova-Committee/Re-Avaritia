package committee.nova.mods.avaritia.api.utils;

import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/26 00:49
 * @Description: from <a href="https://github.com/TinyTsuki/SakuraSignIn_MC">...</a>
 */
public class DateUtils {


    /**
     * 将时间转换为最大单位
     *
     * @param time    时间长度
     * @param curUnit 当前单位
     */
    public static String toMaxUnitString(double time, DateUnit curUnit) {
        return toMaxUnitString(time, curUnit, 0, 2);
    }

    /**
     * 将时间转换为最大单位, 最小数值
     * <p>
     * 10000s显示出来太长了, 不如将单位放大, 数值缩小, decimalPlaces = 2, maxNineCount = 2, -> 166.67m<p>
     * 166.67m这也太长了, 继续缩小数值 -> 2.78h<p>
     * 还是长, 但是不能缩小单位了, 那就省略小数部分 decimalPlaces = 0, -> 3h<p>
     *
     * @param time          时间长度
     * @param curUnit       当前单位
     * @param decimalPlaces 小数位数, 不能小于0哦
     * @param maxNineCount  最大整数位数, 不能小于1哦
     */
    public static String toMaxUnitString(double time, DateUnit curUnit, int decimalPlaces, int maxNineCount) {
        String formatPattern = "%." + decimalPlaces + "f";
        String result = String.format(formatPattern, time) + curUnit.getUnit();
        if (decimalPlaces < 0) decimalPlaces = 0;
        if (maxNineCount <= 0) maxNineCount = 1;
        if (String.valueOf((int) time).length() > maxNineCount) {
            int code = curUnit.getCode() + 1;
            if (code <= DateUnit.getMaxCode() && time > curUnit.getBase()) {
                result = toMaxUnitString(time / curUnit.getBase(), DateUnit.valueOf(code), decimalPlaces, maxNineCount);
            } else {
                // 当到达最大单位后，将整数与小数部分填充为指定数量的9
                StringBuilder ninePart = new StringBuilder();
                StringBuilder decimal = new StringBuilder();
                for (int i = 0; i < maxNineCount; i++) {
                    ninePart.append("9");
                }
                for (int i = 0; i < decimalPlaces; i++) {
                    decimal.append("9");
                }
                if (decimalPlaces > 0) {
                    result = ninePart + "." + decimal + "+" + curUnit.getUnit();
                } else {
                    result = ninePart + "+" + curUnit.getUnit();
                }

            }
        }
        return result;
    }


    @Getter
    public enum DateUnit {
        MILLISECOND(1, 1000, "ms"),
        SECOND(2, 60, "s"),
        MINUTE(3, 60, "m"),
        HOUR(4, 24, "h"),
        DAY(5, 30, "d");

        private final int code;
        private final int base;
        private final String unit;

        DateUnit(int code, int base, String unit) {
            this.code = code;
            this.base = base;
            this.unit = unit;
        }

        public static DateUnit valueOf(int code) {
            for (DateUnit status : DateUnit.values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid code: " + code);
        }

        public static int getMaxCode() {
            return Arrays.stream(DateUnit.values()).max(Comparator.comparingInt(DateUnit::getCode)).orElse(DateUnit.values()[DateUnit.values().length - 1]).getCode();
        }
    }
}
