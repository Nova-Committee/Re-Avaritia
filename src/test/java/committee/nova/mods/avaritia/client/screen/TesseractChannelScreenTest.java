package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("超立方体频道滚动")
class TesseractChannelScreenTest {
    @Test
    @DisplayName("空列表不产生负偏移，长列表可以滚动到底")
    void scrollRangeReachesLastRow() {
        assertAll(
                () -> assertEquals(0, TesseractChannelScreen.maxScrollOffset(0)),
                () -> assertEquals(0, TesseractChannelScreen.maxScrollOffset(9)),
                () -> assertEquals(5, TesseractChannelScreen.maxScrollOffset(14))
        );
    }
}
