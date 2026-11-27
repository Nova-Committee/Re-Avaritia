package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static committee.nova.mods.avaritia.client.screen.TesseractScreenLayout.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("超立方体标题与完整背景")
class TesseractScreenLayoutTest {

    @Test
    @DisplayName("普通和合成模式连续绘制到完整底边，背包与快捷栏均保留原图")
    void bothModesRenderCompleteInventoryAndHotbarAtlas() throws IOException {
        BufferedImage atlas = atlas();
        for (boolean crafting : new boolean[]{false, true}) {
            BufferedImage panel = compose(atlas, crafting);
            // Menu inventory rows start at 195; hotbar starts at 258. Both use atlas offset +67.
            for (int y = 192; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    assertEquals(atlas.getRGB(x, y - 67), panel.getRGB(x, y),
                            "Inventory atlas mismatch at " + x + "," + y + ", crafting=" + crafting);
                }
            }
            assertEquals(0xFF8B8B8B, panel.getRGB(24, 259));
            assertEquals(0xFF000000, panel.getRGB(100, HEIGHT - 1));
            assertTrue(258 + 16 <= HEIGHT, "Hotbar must be inside the clickable panel");
            // The offhand frame ends at x=23; the text area to its right remains blank.
            for (int y = INVENTORY_TITLE_Y; y < INVENTORY_TITLE_Y + 9; y++) {
                for (int x = INVENTORY_TITLE_X + 1; x < INVENTORY_TITLE_X + 49; x++) {
                    assertEquals(0xFFC6C6C6, panel.getRGB(x, y), "Inventory title background must remain blank");
                }
            }
        }
    }

    @Test
    @DisplayName("合成模块材质连续绘制且不裁掉最下方合成格")
    void craftingModuleKeepsOriginalTextureCoordinates() throws IOException {
        BufferedImage atlas = atlas();
        BufferedImage panel = compose(atlas, true);
        for (int y = 136; y < 192; y++) {
            for (int x = 0; x < WIDTH; x++) {
                assertEquals(atlas.getRGB(x, y - 67), panel.getRGB(x, y));
            }
        }
    }

    private static BufferedImage atlas() throws IOException {
        try (InputStream input = TesseractScreenLayoutTest.class.getResourceAsStream(
                "/assets/avaritia/textures/gui/chest/channel_panel.png")) {
            assertNotNull(input);
            return ImageIO.read(input);
        }
    }

    private static BufferedImage compose(BufferedImage atlas, boolean crafting) {
        BufferedImage panel = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int nextY = 0;
        for (BackgroundSlice slice : background(crafting)) {
            assertEquals(nextY, slice.destinationY(), "Background slices must not overlap or leave gaps");
            assertTrue(slice.sourceY() >= 0 && slice.sourceY() + slice.height() <= atlas.getHeight());
            assertTrue(slice.destinationY() + slice.height() <= HEIGHT);
            for (int y = 0; y < slice.height(); y++) {
                for (int x = 0; x < WIDTH; x++) {
                    panel.setRGB(x, slice.destinationY() + y, atlas.getRGB(x, slice.sourceY() + y));
                }
            }
            nextY += slice.height();
        }
        assertEquals(HEIGHT, nextY, "Background must include its bottom border");
        return panel;
    }
}
