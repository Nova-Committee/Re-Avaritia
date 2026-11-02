package committee.nova.mods.avaritia.api.util;

public class StringUtils {

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotNullOrEmpty(String s) {
        return !isNullOrEmpty(s);
    }

    public static int toInt(String s) {
        return Integer.parseInt(s);
    }
}
