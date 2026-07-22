package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tesseract legacy main screen")
class TesseractScreenTest {
    @Test
    @DisplayName("search field and buttons use the 1.20.1 atlas coordinates")
    void keepsLegacyControlTextureContract() {
        assertAll(
                () -> assertEquals(104, TesseractScreenLayout.SEARCH_X),
                () -> assertEquals(90, TesseractScreenLayout.SEARCH_WIDTH),
                () -> assertEquals(219, TesseractScreenLayout.toggleTextureX(false)),
                () -> assertEquals(235, TesseractScreenLayout.toggleTextureX(true)),
                () -> assertEquals(75, TesseractScreenLayout.sortTextureY(0)),
                () -> assertEquals(187, TesseractScreenLayout.sortTextureY(7)),
                () -> assertEquals(203, TesseractScreenLayout.viewTextureY(0)),
                () -> assertEquals(235, TesseractScreenLayout.viewTextureY(2)),
                () -> assertEquals(0, TesseractScreenLayout.CRAFT_TO_CHANNEL_TEXTURE_Y),
                () -> assertEquals(18, TesseractScreenLayout.CRAFT_TO_INVENTORY_TEXTURE_Y),
                () -> assertEquals(9, TesseractScreenLayout.CRAFT_AND_DROP_TEXTURE_Y)
        );
    }

    @Test
    @DisplayName("both background compositions fill the current 256 pixel screen")
    void fillsCurrentScreenHeight() {
        assertAll(
                () -> assertEquals(TesseractScreenLayout.HEIGHT, bottomOfLastSlice(false)),
                () -> assertEquals(TesseractScreenLayout.HEIGHT, bottomOfLastSlice(true)),
                () -> assertEquals(97, TesseractScreenLayout.VIEW_Y + TesseractScreenLayout.CONTROL_SIZE),
                () -> assertEquals(178, TesseractScreenLayout.CRAFT_AND_DROP_Y
                        + TesseractScreenLayout.CRAFT_BUTTON_HEIGHT)
        );
    }

    private static int bottomOfLastSlice(boolean crafting) {
        TesseractScreenLayout.BackgroundSlice[] slices = TesseractScreenLayout.background(crafting);
        TesseractScreenLayout.BackgroundSlice last = slices[slices.length - 1];
        return last.destinationY() + last.height();
    }
}
