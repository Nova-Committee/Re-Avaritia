package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Infinity Chest legacy sort button")
class InfinityChestScreenTest {
    @Test
    @DisplayName("all eight sort modes use the 1.20.1 texture strip")
    void keepsLegacySortTextureContract() {
        assertAll(
                () -> assertEquals(17, InfinityChestScreenLayout.SORT_BUTTON_WIDTH),
                () -> assertEquals(18, InfinityChestScreenLayout.SORT_BUTTON_HEIGHT),
                () -> assertEquals(303, InfinityChestScreenLayout.sortTextureX(0)),
                () -> assertEquals(422, InfinityChestScreenLayout.sortTextureX(7))
        );
    }
}
