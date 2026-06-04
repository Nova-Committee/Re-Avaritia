package com.avaritia.api.utils.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MathUtils 纯 Java 数学工具（无 Minecraft 依赖）")
class MathUtilsTest {

    // region Constants

    @Test
    @DisplayName("常量与 java.lang.Math 保持一致")
    void constantsMatchStandardMath() {
        assertAll(
                () -> assertEquals(Math.PI, MathUtils.pi, 1.0E-12, "pi"),
                () -> assertEquals(Math.PI, MathUtils.pi, "pi (exact)"),
                () -> assertEquals(180.0 / Math.PI, MathUtils.todeg, 1.0E-10, "todeg"),
                () -> assertEquals(Math.PI / 180.0, MathUtils.torad, 1.0E-10, "torad"),
                () -> assertEquals(Math.sqrt(2), MathUtils.sqrt2, 1.0E-12, "sqrt2"),
                () -> assertEquals(1.618033988749894, MathUtils.phi, 1.0E-15, "phi")
        );
    }

    @Test
    @DisplayName("SIN_TABLE 正确初始化且有 65536 个采样点")
    void sinTableHas65536Entries() {
        assertEquals(65536, MathUtils.SIN_TABLE.length);
    }

    @Test
    @DisplayName("SIN_TABLE 首尾值接近 0")
    void sinTableEdgesAreNearZero() {
        assertEquals(0.0, MathUtils.SIN_TABLE[0], 1.0E-5, "sin(0) should be ~0");
        // Last entry sin(65535/65536 * 2*PI) ≈ very close to 0
        assertEquals(0.0, MathUtils.SIN_TABLE[65535], 1.0E-3,
                "sin(65535/65536 * 2*PI) should be near 0");
    }

    // endregion

    // region sin / cos (lookup-table based)

    @Test
    @DisplayName("sin 和 cos 对常见角度匹配 java.lang.Math")
    void sinCosMatchExpectedTrigValues() {
        assertAll(
                () -> assertEquals(1.0, MathUtils.sin(Math.PI / 2), 0.001, "sin(pi/2)"),
                () -> assertEquals(0.0, MathUtils.sin(0), 0.001, "sin(0)"),
                () -> assertEquals(-1.0, MathUtils.sin(-Math.PI / 2), 0.001, "sin(-pi/2)"),
                () -> assertEquals(0.0, MathUtils.sin(Math.PI), 0.001, "sin(pi)"),
                () -> assertEquals(0.0, MathUtils.cos(Math.PI / 2), 0.001, "cos(pi/2)"),
                () -> assertEquals(1.0, MathUtils.cos(0), 0.001, "cos(0)"),
                () -> assertEquals(-1.0, MathUtils.cos(Math.PI), 0.001, "cos(pi)")
        );
    }

    @Test
    @DisplayName("sin² + cos² ≈ 1 恒等式成立")
    void sinSquaredPlusCosSquaredApproximatelyOne() {
        for (double angle = -2 * Math.PI; angle <= 2 * Math.PI; angle += 0.5) {
            double sampleAngle = angle;
            double s = MathUtils.sin(angle);
            double c = MathUtils.cos(angle);
            assertEquals(1.0, s * s + c * c, 0.01,
                    () -> "sin^2+cos^2 should be ~1 at angle=" + sampleAngle);
        }
    }

    // endregion

    // region approachLinear

    @Test
    @DisplayName("approachLinear 步进超过差值时直接返回目标值")
    void approachLinearSnapsWhenStepExceedsDifference() {
        assertAll(
                () -> assertEquals(7.0, MathUtils.approachLinear(3.0, 7.0, 100.0), 1.0E-9),
                () -> assertEquals(7.0F, MathUtils.approachLinear(3.0F, 7.0F, 100.0F), 1.0E-6F),
                () -> assertEquals(-5.0, MathUtils.approachLinear(3.0, -5.0, 100.0), 1.0E-9)
        );
    }

    @Test
    @DisplayName("approachLinear 步进不足差值时按 max 步长移动")
    void approachLinearStepsWhenDifferenceExceedsMax() {
        assertAll(
                () -> assertEquals(6.0, MathUtils.approachLinear(3.0, 13.0, 3.0), 1.0E-9),
                () -> assertEquals(8.0F, MathUtils.approachLinear(10.0F, 5.0F, 2.0F), 1.0E-6F),
                () -> assertEquals(-2.0, MathUtils.approachLinear(-10.0, 2.0, 8.0), 1.0E-9)
        );
    }

    @Test
    @DisplayName("approachLinear 已经到达目标时保持不变")
    void approachLinearNoOpWhenAlreadyAtTarget() {
        assertAll(
                () -> assertEquals(5.0, MathUtils.approachLinear(5.0, 5.0, 10.0), 1.0E-9),
                () -> assertEquals(5.0F, MathUtils.approachLinear(5.0F, 5.0F, 10.0F), 1.0E-6F)
        );
    }

    // endregion

    // region interpolate

    @Test
    @DisplayName("interpolate 执行线性插值 a+(b-a)*d")
    void interpolatePerformsLinearInterpolation() {
        assertAll(
                () -> assertEquals(0.0, MathUtils.interpolate(0.0, 10.0, 0.0), 1.0E-9, "t=0"),
                () -> assertEquals(10.0, MathUtils.interpolate(0.0, 10.0, 1.0), 1.0E-9, "t=1"),
                () -> assertEquals(5.0, MathUtils.interpolate(0.0, 10.0, 0.5), 1.0E-9, "t=0.5"),
                () -> assertEquals(0.0F, MathUtils.interpolate(0.0F, 10.0F, 0.0F), 1.0E-6F, "float t=0"),
                () -> assertEquals(10.0F, MathUtils.interpolate(0.0F, 10.0F, 1.0F), 1.0E-6F, "float t=1"),
                () -> assertEquals(5.0F, MathUtils.interpolate(0.0F, 10.0F, 0.5F), 1.0E-6F, "float t=0.5")
        );
    }

    @Test
    @DisplayName("interpolate 支持外插 (t 超出 [0,1])")
    void interpolateSupportsExtrapolation() {
        assertAll(
                () -> assertEquals(20.0, MathUtils.interpolate(0.0, 10.0, 2.0), 1.0E-9),
                () -> assertEquals(-10.0, MathUtils.interpolate(0.0, 10.0, -1.0), 1.0E-9),
                () -> assertEquals(20.0F, MathUtils.interpolate(0.0F, 10.0F, 2.0F), 1.0E-6F)
        );
    }

    // endregion

    // region approachExp

    @Test
    @DisplayName("approachExp 用比率指数级靠近")
    void approachExpUsesRatioToApproach() {
        assertAll(
                () -> assertEquals(0.0, MathUtils.approachExp(0.0, 0.0, 0.5), 1.0E-9, "already at target"),
                () -> assertEquals(5.0, MathUtils.approachExp(0.0, 10.0, 0.5), 1.0E-9, "halfway to 10"),
                () -> assertEquals(10.0, MathUtils.approachExp(0.0, 10.0, 1.0), 1.0E-9, "ratio=1 snaps")
        );
    }

    @Test
    @DisplayName("approachExp 带 cap 参数限制最大步长")
    void approachExpWithCapLimitsStep() {
        // From 0 to 10 with ratio 0.8: raw step = 8.0, but cap = 2.0 limits it
        assertEquals(2.0, MathUtils.approachExp(0.0, 10.0, 0.8, 2.0), 1.0E-9,
                "step capped at 2.0");
        // From 10 to 0 with ratio 0.8: raw step = -8.0, cap = 2.0
        assertEquals(8.0, MathUtils.approachExp(10.0, 0.0, 0.8, 2.0), 1.0E-9,
                "negative step also capped at -2.0");
    }

    @Test
    @DisplayName("approachExp with cap: small step not affected")
    void approachExpWithCapSmallStepsUnaffected() {
        double result = MathUtils.approachExp(0.0, 10.0, 0.1, 5.0);
        assertEquals(1.0, result, 1.0E-9, "0.1*10=1.0 < cap=5.0 so full step");
    }

    // endregion

    // region retreatExp

    @Test
    @DisplayName("retreatExp 在有 kick 时从 c 方向撤退")
    void retreatExpRetreatsFromPointCWithKick() {
        double result = MathUtils.retreatExp(0.0, 10.0, 2.0, 0.5, 1.0);
        // d = (|2-0| + 1) * 0.5 = 1.5; 1.5 < |10-0|=10, so a + signum(10-0) * 1.5 = 1.5
        assertEquals(1.5, result, 1.0E-9);
    }

    @Test
    @DisplayName("retreatExp 当计算步长超过差值时返回目标 b")
    void retreatExpReturnsBWhenStepExceedsDifference() {
        // d = (|5-5| + 100) * 10 = 1000; 1000 > |10-5| = 5
        double result = MathUtils.retreatExp(5.0, 10.0, 5.0, 10.0, 100.0);
        assertEquals(10.0, result, 1.0E-9);
    }

    // endregion

    // region clip

    @Test
    @DisplayName("clip 将值限制在 [min, max] 区间")
    void clipClampsValueToRange() {
        assertAll(
                () -> assertEquals(5.0, MathUtils.clip(5.0, 0.0, 10.0), "within range"),
                () -> assertEquals(0.0, MathUtils.clip(-5.0, 0.0, 10.0), "below min"),
                () -> assertEquals(10.0, MathUtils.clip(15.0, 0.0, 10.0), "above max"),
                () -> assertEquals(5.0F, MathUtils.clip(5.0F, 0.0F, 10.0F), "float within"),
                () -> assertEquals(0.0F, MathUtils.clip(-5.0F, 0.0F, 10.0F), "float below"),
                () -> assertEquals(10.0F, MathUtils.clip(15.0F, 0.0F, 10.0F), "float above"),
                () -> assertEquals(5, MathUtils.clip(5, 0, 10), "int within"),
                () -> assertEquals(0, MathUtils.clip(-5, 0, 10), "int below"),
                () -> assertEquals(10, MathUtils.clip(15, 0, 10), "int above")
        );
    }

    @Test
    @DisplayName("clip 边界值等于端点")
    void clipBoundaryValuesEqualEndpoints() {
        assertAll(
                () -> assertEquals(0.0, MathUtils.clip(0.0, 0.0, 10.0)),
                () -> assertEquals(10.0, MathUtils.clip(10.0, 0.0, 10.0)),
                () -> assertEquals(7, MathUtils.clip(7, 7, 7), "single-point range")
        );
    }

    // endregion

    // region map

    @Test
    @DisplayName("map 将值从一个区间线性映射到另一个区间")
    void mapRemapsValueBetweenRanges() {
        assertAll(
                () -> assertEquals(0.0, MathUtils.map(0.0, 0.0, 10.0, 0.0, 100.0), 1.0E-9, "in min"),
                () -> assertEquals(100.0, MathUtils.map(10.0, 0.0, 10.0, 0.0, 100.0), 1.0E-9, "in max"),
                () -> assertEquals(50.0, MathUtils.map(5.0, 0.0, 10.0, 0.0, 100.0), 1.0E-9, "midpoint"),
                () -> assertEquals(0.0F, MathUtils.map(0.0F, 0.0F, 10.0F, 0.0F, 100.0F), 1.0E-6F, "float min"),
                () -> assertEquals(100.0F, MathUtils.map(10.0F, 0.0F, 10.0F, 0.0F, 100.0F), 1.0E-6F, "float max")
        );
    }

    @Test
    @DisplayName("map 支持反向映射和负范围")
    void mapSupportsReverseAndNegativeRanges() {
        assertAll(
                () -> assertEquals(100.0, MathUtils.map(0.0, 0.0, 10.0, 100.0, 0.0), 1.0E-9, "reverse"),
                () -> assertEquals(-50.0, MathUtils.map(5.0, 0.0, 10.0, -100.0, 0.0), 1.0E-9, "negative")
        );
    }

    // endregion

    // region round

    @Test
    @DisplayName("round 按 multiplier 精度四舍五入")
    void roundToMultiplierPerformsRounding() {
        assertAll(
                () -> assertEquals(17.535, MathUtils.round(17.5345743, 1000.0), 1.0E-9, "3 decimals"),
                () -> assertEquals(17.5, MathUtils.round(17.5345743, 10.0), 1.0E-9, "1 decimal"),
                () -> assertEquals(17.535F, MathUtils.round(17.5345743F, 1000.0F), 1.0E-5F, "float 3 decimals"),
                () -> assertEquals(17.5F, MathUtils.round(17.5345743F, 10.0F), 1.0E-5F, "float 1 decimal"),
                () -> assertEquals(5.0, MathUtils.round(5.0, 100.0), 1.0E-9, "already exact")
        );
    }

    // endregion

    // region between

    @Test
    @DisplayName("between 检查 min <= value <= max")
    void betweenChecksInclusiveBounds() {
        assertAll(
                () -> assertTrue(MathUtils.between(0.0, 5.0, 10.0)),
                () -> assertTrue(MathUtils.between(0.0, 0.0, 10.0), "equal to min"),
                () -> assertTrue(MathUtils.between(0.0, 10.0, 10.0), "equal to max"),
                () -> assertFalse(MathUtils.between(0.0, -1.0, 10.0), "below min"),
                () -> assertFalse(MathUtils.between(0.0, 11.0, 10.0), "above max")
        );
    }

    // endregion

    // region approachExpI / retreatExpI

    @Test
    @DisplayName("approachExpI 在四舍五入后与目标相等时返回目标")
    void approachExpIReturnsTargetWhenRoundingEqual() {
        // a=0, b=10, ratio=0.5 => 5.0 which rounds to 5, not equal to a (0), so not this case
        // a=0, b=1, ratio=0.1 => 0.1 rounds to 0 == a, so returns b=1
        assertEquals(1, MathUtils.approachExpI(0, 1, 0.1),
                "rounded step equals a, so should return b");
    }

    @Test
    @DisplayName("retreatExpI 在四舍五入后与当前值相等时返回目标")
    void retreatExpIReturnsTargetWhenRoundingEqual() {
        // a=0, b=5, c=0, ratio=0.001, kick=0 => d = (|0-0|+0)*0.001 = 0, rounds to 0 == a, return b=5
        assertEquals(5, MathUtils.retreatExpI(0, 5, 0, 0.001, 0));
    }

    // endregion

    // region floor / ceil

    @Test
    @DisplayName("floor 返回不大于输入的最大整数")
    void floorReturnsLargestIntegerNotGreaterThanInput() {
        assertAll(
                () -> assertEquals(3, MathUtils.floor(3.14)),
                () -> assertEquals(3, MathUtils.floor(3.0)),
                () -> assertEquals(-1, MathUtils.floor(-0.2)),
                () -> assertEquals(-4, MathUtils.floor(-3.14)),
                () -> assertEquals(3, MathUtils.floor(3.14F)),
                () -> assertEquals(-4, MathUtils.floor(-3.14F))
        );
    }

    @Test
    @DisplayName("ceil 返回不小于输入的最小整数")
    void ceilReturnsSmallestIntegerNotLessThanInput() {
        assertAll(
                () -> assertEquals(4, MathUtils.ceil(3.14)),
                () -> assertEquals(3, MathUtils.ceil(3.0)),
                () -> assertEquals(0, MathUtils.ceil(-0.2)),
                () -> assertEquals(-3, MathUtils.ceil(-3.14)),
                () -> assertEquals(4, MathUtils.ceil(3.14F)),
                () -> assertEquals(-3, MathUtils.ceil(-3.14F))
        );
    }

    @Test
    @DisplayName("floor 和 ceil 对整数不变")
    void floorAndCeilIdentityForIntegers() {
        assertAll(
                () -> assertEquals(42, MathUtils.floor(42.0)),
                () -> assertEquals(42, MathUtils.ceil(42.0)),
                () -> assertEquals(-7, MathUtils.floor(-7.0)),
                () -> assertEquals(-7, MathUtils.ceil(-7.0))
        );
    }

    // endregion

    // region sqrt

    @Test
    @DisplayName("sqrt 返回 Math.sqrt 的 float 转换")
    void sqrtReturnsFloatCastOfMathSqrt() {
        assertAll(
                () -> assertEquals((float) Math.sqrt(4.0), MathUtils.sqrt(4.0), 1.0E-6F),
                () -> assertEquals((float) Math.sqrt(2.0), MathUtils.sqrt(2.0), 1.0E-6F),
                () -> assertEquals((float) Math.sqrt(9.0F), MathUtils.sqrt(9.0F), 1.0E-6F)
        );
    }

    // endregion

    // region roundAway

    @Test
    @DisplayName("roundAway 向远离零的方向入整")
    void roundAwayFromZero() {
        assertAll(
                () -> assertEquals(4, MathUtils.roundAway(3.14), "positive fraction → ceil"),
                () -> assertEquals(-4, MathUtils.roundAway(-3.14), "negative fraction → floor"),
                () -> assertEquals(3, MathUtils.roundAway(3.0), "exact positive"),
                () -> assertEquals(-3, MathUtils.roundAway(-3.0), "exact negative"),
                () -> assertEquals(1, MathUtils.roundAway(0.01)),
                () -> assertEquals(-1, MathUtils.roundAway(-0.01))
        );
    }

    // endregion

    // region compare

    @Test
    @DisplayName("compare 委托给 Integer.compare / Double.compare")
    void compareDelegatesToStandardLibrary() {
        assertAll(
                () -> assertEquals(Integer.compare(3, 7), MathUtils.compare(3, 7)),
                () -> assertEquals(Integer.compare(7, 3), MathUtils.compare(7, 3)),
                () -> assertEquals(0, MathUtils.compare(5, 5)),
                () -> assertEquals(Double.compare(2.5, 3.1), MathUtils.compare(2.5, 3.1)),
                () -> assertEquals(Double.compare(3.1, 2.5), MathUtils.compare(3.1, 2.5)),
                () -> assertEquals(0, MathUtils.compare(1.5, 1.5))
        );
    }

    // endregion

    // region Combined / integration scenarios

    @Nested
    @DisplayName("组合场景")
    class CombinedScenarios {

        @Test
        @DisplayName("clip → map 管道处理传感器归一化")
        void clipThenMapPipeline() {
            double raw = 12.0;  // beyond [0, 10] range
            double clamped = MathUtils.clip(raw, 0.0, 10.0);
            assertEquals(10.0, clamped, 1.0E-9);

            double normalized = MathUtils.map(clamped, 0.0, 10.0, 0.0, 1.0);
            assertEquals(1.0, normalized, 1.0E-9);
        }

        @Test
        @DisplayName("approachLinear 迭代多次后到达目标")
        void approachLinearIterationsConverge() {
            double value = 0.0;
            for (int i = 0; i < 5; i++) {
                value = MathUtils.approachLinear(value, 10.0, 2.0);
            }
            assertEquals(10.0, value, 1.0E-9, "after 5 steps of 2, 0→10 should reach 10");
        }

        @Test
        @DisplayName("interpolate + clip 确保插值结果不越界")
        void interpolateWithClipStaysInBounds() {
            float result = MathUtils.interpolate(0.0F, 100.0F, -0.5F);
            float safe = MathUtils.clip(result, 0.0F, 100.0F);
            assertEquals(0.0F, safe, 1.0E-6F, "negative extrapolation clipped to 0");
        }

        @Test
        @DisplayName("between + map 验证阈值触发")
        void betweenWithMapThresholdTrigger() {
            double raw = 75.0;
            if (MathUtils.between(0.0, raw, 100.0)) {
                double percent = MathUtils.map(raw, 0.0, 100.0, 0.0, 100.0);
                assertEquals(75.0, percent, 1.0E-9);
            }
        }
    }
    // endregion
}
