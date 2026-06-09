package committee.nova.mods.avaritia.api.client.util.color;

import committee.nova.mods.avaritia.api.utils.java.Copyable;
import committee.nova.mods.avaritia.api.utils.math.MathUtils;

import static java.lang.Math.max;

public abstract class Color implements Copyable<Color> {

    public byte r;
    public byte g;
    public byte b;
    public byte a;

    public Color(int r, int g, int b, int a) {
        this.r = (byte) r;
        this.g = (byte) g;
        this.b = (byte) b;
        this.a = (byte) a;
    }

    public Color(Color color) {
        r = color.r;
        g = color.g;
        b = color.b;
        a = color.a;
    }

    /**
     * Flips a color between ABGR and RGBA.
     *
     * @param colour The input either ABGR or RGBA.
     * @return The flipped color.
     */
    public static int flipABGR(int colour) {
        int a = (colour >> 24) & 0xFF;
        int b = (colour >> 16) & 0xFF;
        int c = (colour >> 8) & 0xFF;
        int d = colour & 0xFF;
        return (d & 0xFF) << 24 | (c & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
    }

    public static int[] unpack(int colour) {
        return new int[]{(colour >> 24) & 0xFF, (colour >> 16) & 0xFF, (colour >> 8) & 0xFF, colour & 0xFF};
    }

    public static int pack(int[] data) {
        return (data[0] & 0xFF) << 24 | (data[1] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | (data[3] & 0xFF);
    }

    public static int packRGBA(byte r, byte g, byte b, byte a) {
        return (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
    }

    public static int packARGB(byte r, byte g, byte b, byte a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int packRGBA(int r, int g, int b, int a) {
        return r << 24 | g << 16 | b << 8 | a;
    }

    public static int packARGB(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int packRGBA(double r, double g, double b, double a) {
        return (int) (r * 255) << 24 | (int) (g * 255) << 16 | (int) (b * 255) << 8 | (int) (a * 255);
    }

    public static int packARGB(double r, double g, double b, double a) {
        return (int) (a * 255) << 24 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
    }

    public static int packRGBA(float[] data) {
        return packRGBA(data[0], data[1], data[2], data[3]);
    }

    public static int packARGB(float[] data) {
        return packARGB(data[0], data[1], data[2], data[3]);
    }

    public abstract int pack();

    public abstract float[] packArray();

    public Color add(Color color2) {
        a += color2.a;
        r += color2.r;
        g += color2.g;
        b += color2.b;
        return this;
    }

    public Color sub(Color color2) {
        int ia = (a & 0xFF) - (color2.a & 0xFF);
        int ir = (r & 0xFF) - (color2.r & 0xFF);
        int ig = (g & 0xFF) - (color2.g & 0xFF);
        int ib = (b & 0xFF) - (color2.b & 0xFF);
        a = (byte) max(ia, 0);
        r = (byte) max(ir, 0);
        g = (byte) max(ig, 0);
        b = (byte) max(ib, 0);
        return this;
    }

    public Color invert() {
        a = (byte) (0xFF - (a & 0xFF));
        r = (byte) (0xFF - (r & 0xFF));
        g = (byte) (0xFF - (g & 0xFF));
        b = (byte) (0xFF - (b & 0xFF));
        return this;
    }

    public Color multiply(Color color2) {
        a = (byte) ((a & 0xFF) * ((color2.a & 0xFF) / 255D));
        r = (byte) ((r & 0xFF) * ((color2.r & 0xFF) / 255D));
        g = (byte) ((g & 0xFF) * ((color2.g & 0xFF) / 255D));
        b = (byte) ((b & 0xFF) * ((color2.b & 0xFF) / 255D));
        return this;
    }

    public Color scale(double d) {
        a = (byte) ((a & 0xFF) * d);
        r = (byte) ((r & 0xFF) * d);
        g = (byte) ((g & 0xFF) * d);
        b = (byte) ((b & 0xFF) * d);
        return this;
    }

    public Color interpolate(Color color2, double d) {
        return this.add(color2.copy().sub(this).scale(d));
    }

    public Color multiplyC(double d) {
        r = (byte) MathUtils.clip((r & 0xFF) * d, 0, 255);
        g = (byte) MathUtils.clip((g & 0xFF) * d, 0, 255);
        b = (byte) MathUtils.clip((b & 0xFF) * d, 0, 255);

        return this;
    }

    public abstract Color copy();

    public int rgb() {
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public int argb() {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public int rgba() {
        return (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
    }

    public abstract Color set(int colour);

    public Color set(Color color) {
        r = color.r;
        g = color.g;
        b = color.b;
        a = color.a;
        return this;
    }

    public Color set(double r, double g, double b, double a) {
        return set((int) (255 * r), (int) (255 * g), (int) (255 * b), (int) (255 * a));
    }

    public Color set(float r, float g, float b, float a) {
        return set((int) (255F * r), (int) (255F * g), (int) (255F * b), (int) (255F * a));
    }

    public Color set(int r, int g, int b, int a) {
        this.r = (byte) r;
        this.g = (byte) g;
        this.b = (byte) b;
        this.a = (byte) a;
        return this;
    }

    public Color set(double[] doubles) {
        return set(doubles[0], doubles[1], doubles[2], doubles[3]);
    }

    public Color set(float[] floats) {
        return set(floats[0], floats[1], floats[2], floats[3]);
    }

    public Color rF(float r) {
        this.r = (byte) (255F * r);
        return this;
    }

    public Color gF(float g) {
        this.g = (byte) (255F * g);
        return this;
    }

    public Color bF(float b) {
        this.b = (byte) (255F * b);
        return this;
    }

    public Color aF(float a) {
        this.a = (byte) (255F * a);
        return this;
    }

    public Color rF(int r) {
        this.r = (byte) r;
        return this;
    }

    public Color gF(int g) {
        this.g = (byte) g;
        return this;
    }

    public Color bF(int b) {
        this.b = (byte) b;
        return this;
    }

    public Color aF(int a) {
        this.a = (byte) a;
        return this;
    }

    public float rF() {
        return r() / 255F;
    }

    public float gF() {
        return g() / 255F;
    }

    public float bF() {
        return b() / 255F;
    }

    public float aF() {
        return a() / 255F;
    }

    public int r() {
        return r & 0xFF;
    }

    public int g() {
        return g & 0xFF;
    }

    public int b() {
        return b & 0xFF;
    }

    public int a() {
        return a & 0xFF;
    }

    public float[] getRGBA() {
        return new float[]{(r & 0xFF) / 255F, (g & 0xFF) / 255F, (b & 0xFF) / 255F, (a & 0xFF) / 255F};
    }

    public float[] getARGB() {
        return new float[]{(a & 0xFF) / 255F, (r & 0xFF) / 255F, (g & 0xFF) / 255F, (b & 0xFF) / 255F};
    }

    public boolean equals(Color color) {
        return color != null && rgba() == color.rgba();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Color color)) return false;

        if (r != color.r) return false;
        if (g != color.g) return false;
        if (b != color.b) return false;
        return a == color.a;
    }

    @Override
    public int hashCode() {
        int result = r;
        result = 31 * result + (int) g;
        result = 31 * result + (int) b;
        result = 31 * result + (int) a;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[0x" + Integer.toHexString(pack()).toUpperCase() + "]";
    }
}
