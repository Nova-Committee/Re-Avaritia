package com.avaritia.api.client.util.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ColorTest {

    @Test
    void getRgbaTreatsStoredBytesAsUnsignedComponents() {
        assertArrayEquals(new float[]{1.0F, 1.0F, 1.0F, 0.6F}, new ColorARGB(0x99FFFFFF).getRGBA(), 1.0E-6F);
        assertArrayEquals(new float[]{0.0F, 0.0F, 0.0F, 1.0F}, new ColorARGB(0xFF000000).getRGBA(), 1.0E-6F);
    }

    @Test
    void getArgbTreatsStoredBytesAsUnsignedComponents() {
        assertArrayEquals(new float[]{0.6F, 1.0F, 1.0F, 1.0F}, new ColorARGB(0x99FFFFFF).getARGB(), 1.0E-6F);
        assertArrayEquals(new float[]{1.0F, 0.0F, 0.0F, 0.0F}, new ColorARGB(0xFF000000).getARGB(), 1.0E-6F);
    }
}
