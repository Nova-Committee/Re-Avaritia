package com.avaritia.api.client.util.color;

public class ColorRGBA extends Color {

    public ColorRGBA(int colour) {
        super((colour >> 24) & 0xFF, (colour >> 16) & 0xFF, (colour >> 8) & 0xFF, colour & 0xFF);
    }

    public ColorRGBA(double r, double g, double b, double a) {
        super((int) (255 * r), (int) (255 * g), (int) (255 * b), (int) (255 * a));
    }

    public ColorRGBA(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public ColorRGBA(float[] data) {
        this(data[0], data[1], data[2], data[3]);
    }

    public ColorRGBA(ColorRGBA colour) {
        super(colour);
    }

    public static int pack(Color color) {
        return (color.r & 0xFF) << 24 | (color.g & 0xFF) << 16 | (color.b & 0xFF) << 8 | (color.a & 0xFF);
    }

    public static int multiply(int c1, int c2) {
        if (c1 == -1) {
            return c2;
        }
        if (c2 == -1) {
            return c1;
        }
        int r = (((c1 >>> 24) * (c2 >>> 24)) & 0xFF00) << 16;
        int g = (((c1 >> 16 & 0xFF) * (c2 >> 16 & 0xFF)) & 0xFF00) << 8;
        int b = ((c1 >> 8 & 0xFF) * (c2 >> 8 & 0xFF)) & 0xFF00;
        int a = ((c1 & 0xFF) * (c2 & 0xFF)) >> 8;
        return r | g | b | a;
    }

    public static int multiplyC(int c, float f) {
        int r = (int) ((c >>> 24) * f);
        int g = (int) ((c >> 16 & 0xFF) * f);
        int b = (int) ((c >> 8 & 0xFF) * f);
        return r << 24 | g << 16 | b << 8 | c & 0xFF;
    }

    @Override
    public int pack() {
        return pack(this);
    }

    @Override
    public float[] packArray() {
        return new float[]{(r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f};
    }

    @Override
    public Color copy() {
        return new ColorRGBA(this);
    }

    @Override
    public Color set(int colour) {
        return set(new ColorRGBA(colour));
    }
}
