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
    @DisplayName("both background compositions continuously fill the current 256 pixel screen")
    void fillsCurrentScreenHeight() {
        assertAll(
                () -> assertContinuousBackground(false),
                () -> assertContinuousBackground(true),
                () -> assertEquals(240, TesseractScreenLayout.VIEW_Y + TesseractScreenLayout.CONTROL_SIZE),
                () -> assertEquals(TesseractScreenLayout.CONTROL_SIZE,
                        TesseractScreenLayout.LOCK_Y - TesseractScreenLayout.CRAFTING_TOGGLE_Y),
                () -> assertEquals(TesseractScreenLayout.CONTROL_SIZE,
                        TesseractScreenLayout.VIEW_Y - TesseractScreenLayout.SORT_Y),
                () -> assertEquals(178, TesseractScreenLayout.CRAFT_AND_DROP_Y
                        + TesseractScreenLayout.CRAFT_BUTTON_HEIGHT)
        );
    }

    @Test
    @DisplayName("player inventory and hotbar keep the complete legacy texture regions")
    void keepsCompletePlayerInventoryTexture() {
        assertAll(
                () -> assertPlayerInventorySlices(false),
                () -> assertPlayerInventorySlices(true),
                () -> assertEquals(174, TesseractScreenLayout.INVENTORY_LABEL_Y)
        );
    }

    private static void assertContinuousBackground(boolean crafting) {
        TesseractScreenLayout.BackgroundSlice[] slices = TesseractScreenLayout.background(crafting);
        int expectedDestinationY = 0;
        for (TesseractScreenLayout.BackgroundSlice slice : slices) {
            assertEquals(expectedDestinationY, slice.destinationY());
            expectedDestinationY += slice.height();
        }
        assertEquals(TesseractScreenLayout.HEIGHT, expectedDestinationY);
    }

    private static void assertPlayerInventorySlices(boolean crafting) {
        TesseractScreenLayout.BackgroundSlice[] slices = TesseractScreenLayout.background(crafting);
        TesseractScreenLayout.BackgroundSlice inventory = slices[slices.length - 2];
        TesseractScreenLayout.BackgroundSlice hotbar = slices[slices.length - 1];
        assertEquals(new TesseractScreenLayout.BackgroundSlice(182, 125, 54), inventory);
        assertEquals(new TesseractScreenLayout.BackgroundSlice(236, 190, 20), hotbar);
        assertEquals(210, hotbar.sourceY() + hotbar.height());
    }
}
