package committee.nova.mods.avaritia.common.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tesseract player inventory layout")
class TesseractMenuLayoutTest {
    @Test
    @DisplayName("player slots align with the compact complete background")
    void alignsPlayerSlotsWithBackground() {
        assertAll(
                () -> assertEquals(185, TesseractMenu.PLAYER_INVENTORY_Y),
                () -> assertEquals(237, TesseractMenu.PLAYER_HOTBAR_Y),
                () -> assertEquals(TesseractMenu.PLAYER_INVENTORY_Y + 3 * 17 + 1,
                        TesseractMenu.PLAYER_HOTBAR_Y)
        );
    }
}
