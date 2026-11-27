package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tesseract background coverage")
class TesseractScreenTest {

    @Test
    @DisplayName("both background modes cover the screen without gaps or overlaps")
    void fillsCurrentScreenHeight() {
        assertContinuousBackground(false);
        assertContinuousBackground(true);
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

}
