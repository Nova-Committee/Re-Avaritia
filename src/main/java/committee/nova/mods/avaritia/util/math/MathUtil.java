package committee.nova.mods.avaritia.util.math;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 9:32
 * Version: 1.0
 */
public class MathUtil {
    public static boolean between(double min, double value, double max) {
        return min <= value && value <= max;
    }
    /**
     * @param value The value
     * @param min   The min value
     * @param max   The max value
     * @return The clipped value between min and max
     */
    public static double clip(double value, double min, double max) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        return value;
    }

    /**
     * @param value The value
     * @param min   The min value
     * @param max   The max value
     * @return The clipped value between min and max
     */
    public static float clip(float value, float min, float max) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        return value;
    }

    /**
     * @param value The value
     * @param min   The min value
     * @param max   The max value
     * @return The clipped value between min and max
     */
    public static int clip(int value, int min, int max) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        return value;
    }

    public static int floor(double d) {
        int i = (int) d;
        return d < (double) i ? i - 1 : i;
    }

    public static int floor(float f) {
        int i = (int) f;
        return f < (float) i ? i - 1 : i;
    }

    public static int ceil(double d) {
        int i = (int) d;
        return d > (double) i ? i + 1 : i;
    }

    public static int ceil(float f) {
        int i = (int) f;
        return f > (float) i ? i + 1 : i;
    }
}
