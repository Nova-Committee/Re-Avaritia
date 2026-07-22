package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tesseract selector pagination")
class TesseractChannelScreenTest {
    @Test
    @DisplayName("the maximum page follows the largest of all three channel columns")
    void clampsToLargestColumn() {
        Map<Integer, String> fourteen = new HashMap<>();
        for (int index = 0; index < 14; index++) fourteen.put(index, "channel" + index);
        assertAll(
                () -> assertEquals(0, TesseractChannelScreen.maximumPage(Map.of(), Map.of(), Map.of())),
                () -> assertEquals(0, TesseractChannelScreen.maximumPage(Map.of(1, "one"), Map.of(), Map.of())),
                () -> assertEquals(1, TesseractChannelScreen.maximumPage(Map.of(), fourteen, Map.of())),
                () -> assertEquals(1, TesseractChannelScreen.maximumPage(Map.of(), Map.of(), fourteen))
        );
    }
}
