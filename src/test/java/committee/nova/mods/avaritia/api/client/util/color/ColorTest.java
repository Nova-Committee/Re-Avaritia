package committee.nova.mods.avaritia.api.client.util.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ColorTest {

    @Test
    void getRgbaUsesUnsignedComponents() {
        assertArrayEquals(new float[]{1.0F, 1.0F, 1.0F, 1.0F}, new ColorARGB(0xFFFFFFFF).getRGBA(), 0.0001F);
        assertArrayEquals(new float[]{1.0F, 1.0F, 1.0F, 0.6F}, new ColorARGB(0x99FFFFFF).getRGBA(), 0.0001F);
    }

    @Test
    void getArgbUsesUnsignedComponents() {
        assertArrayEquals(new float[]{0.6F, 1.0F, 1.0F, 1.0F}, new ColorARGB(0x99FFFFFF).getARGB(), 0.0001F);
    }
}
