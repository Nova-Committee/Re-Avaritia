package com.avaritia.api.utils.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("InterpHelper 纯 Java 双线性插值")
class InterpHelperTest {

    private InterpHelper helper;

    @BeforeEach
    void setUp() {
        helper = new InterpHelper();
        helper.reset(
                0.0F, 0.0F,
                10.0F, 0.0F,
                10.0F, 10.0F,
                0.0F, 10.0F
        );
        helper.setup();
    }

    @Test
    @DisplayName("locate 后 interpolate 在中心点返回四角均值")
    void interpolateReturnsAverageAtCenter() {
        helper.locate(5.0F, 5.0F);

        assertEquals(15.0F, helper.interpolate(0.0F, 10.0F, 20.0F, 30.0F), 1.0E-5F);
    }

    @Test
    @DisplayName("locate 支持边角和边上位置插值")
    void interpolateHandlesCornersAndEdges() {
        assertAll(
                () -> {
                    helper.locate(0.0F, 0.0F);
                    assertEquals(2.0F, helper.interpolate(2.0F, 4.0F, 8.0F, 6.0F), 1.0E-5F);
                },
                () -> {
                    helper.locate(10.0F, 10.0F);
                    assertEquals(8.0F, helper.interpolate(2.0F, 4.0F, 8.0F, 6.0F), 1.0E-5F);
                },
                () -> {
                    helper.locate(5.0F, 0.0F);
                    assertEquals(3.0F, helper.interpolate(2.0F, 4.0F, 8.0F, 6.0F), 1.0E-5F);
                }
        );
    }

    @Test
    @DisplayName("reset 后可复用同一个 helper 计算不同矩形")
    void resetAllowsReusingHelperWithDifferentBounds() {
        helper.reset(
                -2.0F, -1.0F,
                2.0F, -1.0F,
                2.0F, 3.0F,
                -2.0F, 3.0F
        );
        helper.setup();
        helper.locate(0.0F, 1.0F);

        assertEquals(25.0F, helper.interpolate(10.0F, 20.0F, 40.0F, 30.0F), 1.0E-5F);
    }
}
