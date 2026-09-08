package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Infinity Chest legacy buttons")
class InfinityChestScreenTest {
    @Test
    @DisplayName("sort and lock buttons use the 1.21.1 atlas strip")
    void keepsLegacyButtonTextureContract() {
        assertAll(
                () -> assertEquals(17, InfinityChestScreenLayout.SORT_BUTTON_WIDTH),
                () -> assertEquals(18, InfinityChestScreenLayout.SORT_BUTTON_HEIGHT),
                () -> assertEquals(231, InfinityChestScreenLayout.LOCK_BUTTON_X),
                () -> assertEquals(151, InfinityChestScreenLayout.LOCK_BUTTON_Y),
                () -> assertEquals(303, InfinityChestScreenLayout.sortTextureX(0)),
                () -> assertEquals(422, InfinityChestScreenLayout.sortTextureX(7)),
                () -> assertEquals(36, InfinityChestScreenLayout.LOCK_TEXTURE_Y),
                () -> assertEquals(303, InfinityChestScreenLayout.lockTextureX(true)),
                () -> assertEquals(320, InfinityChestScreenLayout.lockTextureX(false))
        );
    }
}
