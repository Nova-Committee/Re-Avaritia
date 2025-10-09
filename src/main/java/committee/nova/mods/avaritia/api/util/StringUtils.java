package committee.nova.mods.avaritia.api.util;

import committee.nova.mods.avaritia.api.client.util.color.ColorMC;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/25 20:25
 * @Description: from <a href="https://github.com/TinyTsuki/SakuraSignIn_MC">...</a>
 */
public class StringUtils {
    public static final String METHOD_SET_PREFIX = "set";
    public static final String METHOD_GET_PREFIX = "get";
    public static final String COMMON_MARK = ",<.>/?;:'\"[{]}\\|`~!@#$%^&*()-_=+，《。》、？；：‘“【】·~！￥…（）—";
    /**
     * NanoId默认随机字符串生成器
     */
    public static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();

    /**
     * NanoId默认随机字符串序列
     */
    public static final char[] DEFAULT_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * NanoId默认随机字符串序列
     */
    public static final char[] NUMBER_ALPHABET = "0123456789".toCharArray();

    /**
     * NanoId默认随机字符串长度
     */
    public static final int DEFAULT_SIZE = 21;

    /**
     * 字符串是否为常用标点符号
     */
    public static boolean isCommonMark(String s) {
        if (s.length() != 1) return false;
        return COMMON_MARK.contains(s);
    }

    /**
     * 根据行号截取字符串
     * <p>(开始堆粪</p>
     *
     * @param suffix 如果结尾还有内容, 是否需要添加的后缀, 例: "后面还有[num]行"
     */
    public static String getByLine(String s, int start, int end, String suffix) {
        if (start > end) return s;
        String code;
        if (s.contains("\r\n")) code = "\r\n";
        else if (s.contains("\r")) code = "\r";
        else if (s.contains("\n")) code = "\n";
        else return s;

        String[] split = s.split(code);
        if (start > split.length) return s;
        if (end >= split.length) {
            StringBuilder back = new StringBuilder();
            for (int i = start - 1; i < split.length; i++) {
                if (i != start - 1) back.append(code);
                back.append(split[i]);
            }
            return back.toString();
        }

        StringBuilder back = new StringBuilder();
        for (int i = start - 1; i < end; i++) {
            if (i != start - 1) back.append(code);
            back.append(split[i]);
        }
        if (!"".equals(suffix))
            back.append(code).append(suffix.replace("[num]", split.length - end + ""));
        return back.toString();
    }

    /**
     * 将数值数组 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     */
    public static String toString(int[] a) {
        return toString(a, ',');
    }

    /**
     * 将数值数组 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     *
     * @param separator 分隔符
     */
    public static String toString(int[] a, char separator) {
        if (a == null)
            return "null";
        // a = Arrays.stream(a).sorted().toArray();
        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(separator);
        }
    }

    /**
     * 将数值数组 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     */
    public static String toString(long[] a) {
        return toString(a, ',');
    }

    /**
     * 将数值数组 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     *
     * @param separator 分隔符
     */
    public static String toString(long[] a, char separator) {
        if (a == null)
            return "null";
        // a = Arrays.stream(a).sorted().toArray();
        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(separator);
        }
    }

    /**
     * 将数值集合 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     */
    public static String toString(Collection<?> a) {
        return toString(a, ',');
    }

    /**
     * 将数值集合 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     *
     * @param separator 分隔符
     */
    public static String toString(Collection<?> a, char separator) {
        if (a == null)
            return "null";
        // a = Arrays.stream(a).sorted().toArray();
        int iMax = a.size() - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        int i = 0;
        for (Object o : a) {
            b.append(o);
            i++;
            if (i <= iMax)
                b.append(separator);
        }
        return b.toString();
    }

    /**
     * 转义正则特殊字符  $()*+.[]?\^{},|
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (!StringUtils.isNullOrEmpty(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 将字符串转为逻辑真假
     *
     * @param s 0|1|真|假|是|否|true|false|y|n|t|f
     */
    public static boolean stringToBoolean(String s) {
        if (null == s) return false;
        switch (s.toLowerCase().trim()) {
            case "1":
            case "真":
            case "是":
            case "true":
            case "y":
            case "t":
                return true;
            case "0":
            case "假":
            case "否":
            case "false":
            case "n":
            case "f":
            default:
                return false;
        }
    }

    /**
     * 将String[][] 格式化为 String
     */
    public static String convertToString(String[][] stringArray, String x, String y) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            for (int j = 0; j < stringArray[i].length; j++) {
                sb.append(stringArray[i][j]);
                if (j < stringArray[i].length - 1) {
                    sb.append(x);
                }
            }
            if (i < stringArray.length - 1) {
                sb.append(y);
            }
        }
        return sb.toString();
    }

    public static boolean isNullOrEmpty(String s) {
        return null == s || s.isEmpty();
    }

    public static boolean isNullOrEmptyEx(String s) {
        return null == s || s.trim().isEmpty();
    }

    public static boolean isNotNullOrEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isNotNull(Object s) {
        return s != null;
    }

    /**
     * @param s 字符串
     * @return 空字符串or本身
     */
    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static String substring(String s, int start, int end) {
        if (isNullOrEmpty(s)) {
            return "";
        }
        int length = s.length();
        if (end < start) {
            return s;
        }
        if (length >= start && length >= end) {
            return s.substring(start, end);
        }
        return s;
    }

    public static String substring(String s, int start) {
        if (isNullOrEmpty(s)) {
            return "";
        }
        int length = s.length();
        if (start > length) {
            return s;
        }
        return s.substring(start);
    }

    public static String substringEnd(String s, int len) {
        if (isNullOrEmpty(s)) {
            return "";
        }
        int length = s.length();
        if (len > length) {
            return s;
        }
        return s.substring(0, length - len);
    }

    public static String toString(String s, String emptyDefault) {
        return StringUtils.isNullOrEmpty(s) ? emptyDefault : s;
    }

    /**
     * 替换换行符
     */
    @NonNull
    public static String replaceLine(String s) {
        if (s == null) return "";
        return s.replaceAll("<br>", "\n")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\r", "\r")
                .replaceAll("\\n", "\n")
                .replaceAll("\\r", "\r")
                .replaceAll("\r\n", "\n");
    }

    public static int getLineCount(String s) {
        if (StringUtils.isNullOrEmpty(s)) return 0;
        return StringUtils.replaceLine(s).split("\n").length;
    }

    public static String getAvatarUrl(long qq, int size) {
        return "http://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=" + size;
    }

    private static final String[] NUM = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] UNIT = {"", "拾", "佰", "仟"
            , "万", "拾万", "佰万", "仟万"
            , "亿", "拾亿", "佰亿", "仟亿"
            , "兆", "拾兆", "佰兆", "仟兆"
            , "京", "拾京", "佰京", "仟京"
            , "垓", "拾垓", "佰垓", "仟垓"
            , "秭", "拾秭", "佰秭", "仟秭"
            , "穰", "拾穰", "佰穰", "仟穰"
            , "沟", "拾沟", "佰沟", "仟沟"
            , "涧", "拾涧", "佰涧", "仟涧"
            , "正", "拾正", "佰正", "仟正"
            , "载", "拾载", "佰载", "仟载"};
    private static final String[] DECIMAL = {"角", "分"};

    /**
     * 将金额转换为大写
     */
    public static String toChineseCapitalized(BigDecimal amount) {
        StringBuilder sb = new StringBuilder();
        int scale = amount.scale();
        if (scale > 2) {
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
        String str = amount.toString();
        String[] parts = str.split("\\.");
        String integerPart = parts[0];
        String decimalPart = "00";
        if (parts.length > 1) {
            decimalPart = parts[1];
        }
        int integerLen = integerPart.length();
        int decimalLen = decimalPart.length();
        if (integerLen == 1 && integerPart.charAt(0) == '0') {
            sb.append(NUM[0]);
        } else {
            for (int i = 0; i < integerLen; i++) {
                int digit = integerPart.charAt(i) - '0';
                int unitIndex = integerLen - i - 1;
                int unit = unitIndex % 4;
                if (digit == 0) {
                    if (unit != 0 && sb.length() > 0 && sb.charAt(sb.length() - 1) != '零') {
                        sb.append(NUM[0]);
                    }
                } else {
                    sb.append(NUM[digit]);
                    sb.append(UNIT[unit]);
                }
                if (unit == 0 && unitIndex > 0 && sb.charAt(sb.length() - 1) != '亿') {
                    sb.append(UNIT[unitIndex]);
                }
            }
        }

        sb.append("元");
        if (decimalLen == 1) {
            decimalPart += "0";
        }

        // 若小数部分不为0
        if (!decimalPart.equals("00")) {
            for (int i = 0; i < decimalLen; i++) {
                int digit = decimalPart.charAt(i) - '0';
                // 若小数位不为0
                if (digit != 0) {
                    sb.append(NUM[digit]);
                    sb.append(DECIMAL[i]);
                }
            }
        }

        if (decimalPart.equals("00")) {
            sb.append("整");
        }
        return sb.toString();
    }

    public static int toInt(String s) {
        return toInt(s, 0);
    }

    public static int toInt(String s, int defaultValue) {
        int result = defaultValue;
        if (StringUtils.isNotNullOrEmpty(s)) {
            try {
                result = Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static long toLong(String s) {
        return toLong(s, 0);
    }

    public static long toLong(String s, long defaultValue) {
        long result = defaultValue;
        if (StringUtils.isNotNullOrEmpty(s)) {
            try {
                result = Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static float toFloat(String s) {
        return toFloat(s, 0);
    }

    public static float toFloat(String s, float defaultValue) {
        float result = defaultValue;
        if (StringUtils.isNotNullOrEmpty(s)) {
            try {
                result = Float.parseFloat(s.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static double toDouble(String s) {
        return toDouble(s, 0);
    }

    public static double toDouble(String s, double defaultValue) {
        double result = defaultValue;
        if (StringUtils.isNotNullOrEmpty(s)) {
            try {
                result = Double.parseDouble(s.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static BigDecimal toBigDecimal(String s) {
        return toBigDecimal(s, BigDecimal.ZERO);
    }

    public static BigDecimal toBigDecimal(String s, BigDecimal defaultValue) {
        BigDecimal result = defaultValue;
        if (StringUtils.isNotNullOrEmpty(s)) {
            try {
                result = new BigDecimal(s.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    /**
     * 整数转罗马数字
     */
    public static String intToRoman(int num) {
        StringBuilder roman = new StringBuilder();
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                roman.append(symbols[i]);
                num -= values[i];
            }
        }
        return roman.toString();
    }

    /**
     * 转百分数
     */
    public static String toPercent(double num) {
        return toPercent(num, 2);
    }

    /**
     * 转百分数
     */
    public static String toPercent(double num, int scale) {
        return String.format(String.format("%%.%df%%%%", scale), num * 100);
    }

    /**
     * 转百分数
     */
    public static String toPercent(BigDecimal num) {
        return toPercent(num.doubleValue());
    }

    /**
     * 转百分数
     */
    public static String toPercent(BigDecimal num, int scale) {
        return toPercent(num.doubleValue(), scale);
    }

    public static String toFixed(double d, int scale) {
        return new BigDecimal(d).setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    public static String toFixedEx(double d, int scale) {
        return toFixed(d, scale).replaceAll("(\\.\\d*?)0+$", "$1").replaceAll("[.]$", "");
    }

    public static String toFixedEx(BigDecimal d, int scale) {
        return d.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replaceAll("(\\.\\d*?)0+$", "$1").replaceAll("[.]$", "");
    }

    /**
     * 获取指定数量的某个字符串
     */
    public static String getString(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static final String FORMAT_REGEX = "%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";

    /**
     * 自定义格式化方法，支持位置重排
     *
     * @param string 格式化字符串
     * @param args   参数
     * @return 格式化后的字符串
     */
    public static String format(String string, Object... args) {
        StringBuilder result = new StringBuilder();
        // 使用正则匹配格式化占位符
        Pattern pattern = Pattern.compile(FORMAT_REGEX);
        Matcher matcher = pattern.matcher(string);
        int i = 0;
        while (matcher.find()) {
            // 获取当前占位符
            String placeholder = matcher.group();

            // 获取位置标识符，如 %1$s 中的 1
            int index = placeholder.contains("$") ? toInt(placeholder.split("\\$")[0].substring(1)) - 1 : -1;
            // 如果占位符中没有显式的数字索引，则默认按顺序处理
            if (index == -1) {
                index = i;
            }
            // 检查是否有足够的参数
            String formattedArg = placeholder;
            if (index < args.length) {
                formattedArg = formatArgument(placeholder, args[index]);
            }
            // 替换占位符为对应的参数
            string = string.replaceFirst(Pattern.quote(placeholder), formattedArg.replaceAll("\\$", "\\\\\\$"));
            i++;
        }
        return string;
    }

    /**
     * 根据占位符的类型格式化参数
     *
     * @param placeholder 占位符
     * @param arg         参数
     */
    private static String formatArgument(String placeholder, Object arg) {
        if (arg == null) return "null";  // 如果参数是 null，直接返回 null
        try {
            return String.format(placeholder.replaceAll("^%\\d+\\$", "%"), arg);  // 默认处理
        } catch (Exception e) {
            // 如果出现异常，直接转换为字符串
            return arg.toString();
        }
    }

    public static int argbToHex(String argb) {
        try {
            if (argb.startsWith("#")) {
                return (int) Long.parseLong(argb.substring(1), 16);
            } else if (argb.startsWith("0x")) {
                return (int) Long.parseLong(argb.substring(2), 16);
            } else {
                return (int) Long.parseLong(argb, 16);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * RGB颜色转换为Minecraft颜色代码
     *
     * @param color 颜色值 (ARGB: 0xAARRGGBB 或 RGB: 0xRRGGBB)
     * @return 颜色代码
     */
    public static String argbToMinecraftColorString(int color) {
        return "§" + argbToMinecraftColor(color).getCode();
    }

    public static ColorMC argbToMinecraftColor(int color) {
        // 获取 RGB 分量
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        // 颜色匹配
        double closestDistance = Double.MAX_VALUE;
        // 默认为白色
        ColorMC result = ColorMC.WHITE;
        for (ColorMC mcColor : ColorMC.values()) {
            int colorRGB = mcColor.getColor();
            int r = (colorRGB >> 16) & 0xFF;
            int g = (colorRGB >> 8) & 0xFF;
            int b = colorRGB & 0xFF;
            // 加权欧几里得距离计算
            double distance = Math.sqrt(2 * Math.pow(red - r, 2) + 4 * Math.pow(green - g, 2) + 3 * Math.pow(blue - b, 2));
            if (distance < closestDistance) {
                closestDistance = distance;
                result = mcColor;
            }
        }
        return result;
    }

    /**
     * 将字符串转换为驼峰命名
     */
    public static String toPascalCase(String input) {
        if (isNullOrEmptyEx(input)) return "";

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            } else {
                capitalizeNext = true;
            }
        }
        return result.toString();
    }

}
