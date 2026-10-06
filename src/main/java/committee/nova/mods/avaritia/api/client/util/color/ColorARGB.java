package committee.nova.mods.avaritia.api.client.util.color;

public class ColorARGB extends Color {

    public ColorARGB(int colour) {
        super((colour >> 16) & 0xFF, (colour >> 8) & 0xFF, colour & 0xFF, (colour >> 24) & 0xFF);
    }

    public ColorARGB(int a, int r, int g, int b) {
        super(r, g, b, a);
    }

    public ColorARGB(ColorARGB colour) {
        super(colour);
    }

    public static int pack(Color color) {
        return (color.a & 0xFF) << 24 | (color.r & 0xFF) << 16 | (color.g & 0xFF) << 8 | (color.b & 0xFF);
    }

    @Override
    public ColorARGB copy() {
        return new ColorARGB(this);
    }

    @Override
    public Color set(int colour) {
        return set(new ColorARGB(colour));
    }

    @Override
    public int pack() {
        return pack(this);
    }

    @Override
    public float[] packArray() {
        return new float[]{(a & 0xFF) / 255f, (r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f};
    }
}
