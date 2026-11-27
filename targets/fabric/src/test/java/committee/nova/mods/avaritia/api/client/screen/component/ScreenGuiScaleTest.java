package committee.nova.mods.avaritia.api.client.screen.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("固定尺寸容器临时GUI缩放")
class ScreenGuiScaleTest {
    @Test
    @DisplayName("当前小窗口的scale2不足以显示完整超立方体时降为scale1")
    void smallTesseractViewportUsesScaleOne() {
        assertEquals(1, ScreenGuiScale.targetScale(425, 240, 2, 218, 283, 4));
    }

    @Test
    @DisplayName("大窗口保留当前缩放，自动模式的实际缩放可逐级降低")
    void largeViewportPreservesScaleAndSmallViewportFitsStepwise() {
        assertAll(
                () -> assertEquals(2, ScreenGuiScale.targetScale(960, 540, 2, 218, 283, 4)),
                () -> assertEquals(3, ScreenGuiScale.targetScale(480, 270, 4, 218, 283, 4)),
                () -> assertEquals(1, ScreenGuiScale.targetScale(425, 240, 1, 218, 283, 4))
        );
    }

    @Test
    @DisplayName("宽度也约束缩放，恰好容纳面板和边距时不再缩小")
    void horizontalFitHonorsThePanelBoundary() {
        assertAll(
                () -> assertEquals(1, ScreenGuiScale.targetScale(309, 400, 2, 302, 274, 4)),
                () -> assertEquals(2, ScreenGuiScale.targetScale(310, 400, 2, 302, 274, 4))
        );
    }

}
