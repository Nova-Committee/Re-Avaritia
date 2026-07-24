package committee.nova.mods.avaritia.client.screen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("超立方体旧版频道选择布局")
class TesseractChannelScreenTest {
    @Test
    @DisplayName("视口与滚动范围保持 1.20.1 的九行布局")
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
