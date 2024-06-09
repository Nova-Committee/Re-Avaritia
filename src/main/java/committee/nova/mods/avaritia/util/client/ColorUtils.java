package committee.nova.mods.avaritia.util.client;

/**
 * ColorUtils
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/9 下午11:28
 */
public class ColorUtils {
    public static float[] HSVtoRGB(float h, float s, float v) {
        float[] hsv = new float[3];
        float[] rgb = new float[3];
        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = v;
        if (hsv[0] == -1.0F) {
            rgb[0] = rgb[1] = rgb[2] = hsv[2];
            return rgb;
        } else {
            int i = (int)Math.floor((double)hsv[0]);
            float f = hsv[0] - (float)i;
            if (i % 2 == 0) {
                f = 1.0F - f;
            }

            float m = hsv[2] * (1.0F - hsv[1]);
            float n = hsv[2] * (1.0F - hsv[1] * f);
            switch (i) {
                case 0:
                case 6:
                    rgb[0] = hsv[2];
                    rgb[1] = n;
                    rgb[2] = m;
                    break;
                case 1:
                    rgb[0] = n;
                    rgb[1] = hsv[2];
                    rgb[2] = m;
                    break;
                case 2:
                    rgb[0] = m;
                    rgb[1] = hsv[2];
                    rgb[2] = n;
                    break;
                case 3:
                    rgb[0] = m;
                    rgb[1] = n;
                    rgb[2] = hsv[2];
                    break;
                case 4:
                    rgb[0] = n;
                    rgb[1] = m;
                    rgb[2] = hsv[2];
                    break;
                case 5:
                    rgb[0] = hsv[2];
                    rgb[1] = m;
                    rgb[2] = n;
            }

            return rgb;
        }
    }
}
