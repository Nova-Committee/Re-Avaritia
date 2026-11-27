package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tesseract legacy selector layout")
class TesseractChannelScreenTest {
    @Test
    @DisplayName("the viewport and scrolling follow the 1.20.1 nine-row panel")
    void keepsLegacyViewportContract() {
        assertAll(
                () -> assertEquals(88, TesseractChannelScreen.WIDTH),
                () -> assertEquals(154, TesseractChannelScreen.HEIGHT),
                () -> assertEquals(9, TesseractChannelScreen.ROWS),
                () -> assertEquals(0, TesseractChannelScreen.maxScrollOffset(0)),
                () -> assertEquals(0, TesseractChannelScreen.maxScrollOffset(9)),
                () -> assertEquals(5, TesseractChannelScreen.maxScrollOffset(14))
        );
    }
}
