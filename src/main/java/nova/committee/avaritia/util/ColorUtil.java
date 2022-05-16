package nova.committee.avaritia.util;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:53
 * Version: 1.0
 */
public class ColorUtil {
    public ColorUtil() {
    }

    public static int intColor(int r, int g, int b) {
        return r * 65536 + g * 256 + b;
    }

    public static int[] hexToRGB(int hex) {
        int[] colors = new int[]{hex >> 16 & 255, hex >> 8 & 255, hex & 255};
        return colors;
    }

    private static float interpolate(float a, float b, float proportion) {
        return a + (b - a) * proportion;
    }

    public static int interpolateColor(int a, int b, float proportion) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];
        rgbToHSB(a >> 16 & 255, a >> 8 & 255, a & 255, hsva);
        rgbToHSB(b >> 16 & 255, b >> 8 & 255, b & 255, hsvb);

        for (int i = 0; i < 3; ++i) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }

        float alpha = interpolate((float) (a >> 24 & 255), (float) (b >> 24 & 255), proportion);
        return hsbToRGB(hsvb[0], hsvb[1], hsvb[2]) | (int) (alpha * 255.0F) & 255;
    }

    public static int saturate(int color, float saturation) {
        float[] hsv = new float[3];
        rgbToHSB(color >> 16 & 255, color >> 8 & 255, color & 255, hsv);
        hsv[1] *= saturation;
        return hsbToRGB(hsv[0], hsv[1], hsv[2]);
    }

    public static int hexToIntWithAlpha(int hex, int alpha) {
        return alpha << 24 | hex & 16777215;
    }

    public static int calcAlpha(double current, double max) {
        return (int) ((max - current) / max) * 255;
    }

    public static int hsbToRGB(float hue, float saturation, float brightness) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (saturation == 0.0F) {
            r = g = b = (int) (brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor((double) hue)) * 6.0F;
            float f = h - (float) Math.floor((double) h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - saturation * (1.0F - f));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (t * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                    break;
                case 1:
                    r = (int) (q * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                    break;
                case 2:
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (t * 255.0F + 0.5F);
                    break;
                case 3:
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (q * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                    break;
                case 4:
                    r = (int) (t * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                    break;
                case 5:
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (q * 255.0F + 0.5F);
            }
        }

        return -16777216 | r << 16 | g << 8 | b;
    }

    public static float[] rgbToHSB(int r, int g, int b, float[] hsbvals) {
        if (hsbvals == null) {
            hsbvals = new float[3];
        }

        int cmax = Math.max(r, g);
        if (b > cmax) {
            cmax = b;
        }

        int cmin = Math.min(r, g);
        if (b < cmin) {
            cmin = b;
        }

        float brightness = (float) cmax / 255.0F;
        float saturation;
        if (cmax != 0) {
            saturation = (float) (cmax - cmin) / (float) cmax;
        } else {
            saturation = 0.0F;
        }

        float hue;
        if (saturation == 0.0F) {
            hue = 0.0F;
        } else {
            float redc = (float) (cmax - r) / (float) (cmax - cmin);
            float greenc = (float) (cmax - g) / (float) (cmax - cmin);
            float bluec = (float) (cmax - b) / (float) (cmax - cmin);
            if (r == cmax) {
                hue = bluec - greenc;
            } else if (g == cmax) {
                hue = 2.0F + redc - bluec;
            } else {
                hue = 4.0F + greenc - redc;
            }

            hue /= 6.0F;
            if (hue < 0.0F) {
                ++hue;
            }
        }

        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }
}
