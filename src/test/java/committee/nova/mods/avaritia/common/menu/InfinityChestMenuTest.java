package committee.nova.mods.avaritia.common.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("无尽箱子转移容量")
class InfinityChestMenuTest {

    @Test
    @DisplayName("单个无尽槽位允许存储到 int 最大值")
    void infinitySlotCapacityReachesIntegerMaxValue() {
        assertAll(
                () -> assertEquals(Integer.MAX_VALUE, InfinityChestMenu.availableTransferSpace(0, Integer.MAX_VALUE)),
                () -> assertEquals(1, InfinityChestMenu.availableTransferSpace(Integer.MAX_VALUE - 1, Integer.MAX_VALUE)),
                () -> assertEquals(0, InfinityChestMenu.availableTransferSpace(Integer.MAX_VALUE, Integer.MAX_VALUE))
        );
    }

    @Test
    @DisplayName("转移数量不会超过槽位剩余容量")
    void transferAmountIsClampedByRemainingCapacity() {
        assertAll(
                () -> assertEquals(64, InfinityChestMenu.transferAmount(64, 0, Integer.MAX_VALUE)),
                () -> assertEquals(Integer.MAX_VALUE, InfinityChestMenu.transferAmount(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)),
                () -> assertEquals(5, InfinityChestMenu.transferAmount(64, Integer.MAX_VALUE - 5, Integer.MAX_VALUE)),
                () -> assertEquals(0, InfinityChestMenu.transferAmount(64, Integer.MAX_VALUE, Integer.MAX_VALUE))
        );
    }
}
