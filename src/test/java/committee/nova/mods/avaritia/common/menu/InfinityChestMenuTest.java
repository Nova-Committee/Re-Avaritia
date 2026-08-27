package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.common.container.InfinityChestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("无尽箱长计数显示")
class InfinityChestMenuTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/committee/nova/mods/avaritia/client/screen/InfinityChestScreen.java");
    private static final Path PACKET = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/net/chest/C2SInfinityChestActionPacket.java");
    private static final Path MENU = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/menu/InfinityChestMenu.java");

    @Test
    @DisplayName("显示值直接使用 long 且覆盖所有数量级")
    void formatsLongAmountsWithoutItemStackTruncation() {
        assertAll(
                () -> assertEquals("999", InfinityChestContainer.formatAmount(999L)),
                () -> assertEquals("1K", InfinityChestContainer.formatAmount(1_000L)),
                () -> assertEquals("12.3K", InfinityChestContainer.formatAmount(12_345L)),
                () -> assertEquals("1G", InfinityChestContainer.formatAmount(1_000_000_000L)),
                () -> assertEquals("MAX", InfinityChestContainer.formatAmount(Long.MAX_VALUE)),
                () -> assertEquals("0", InfinityChestContainer.formatExactAmount(0L)),
                () -> assertEquals("1,234", InfinityChestContainer.formatExactAmount(1_234L)),
                () -> assertEquals("9,223,372,036,854,775,807",
                        InfinityChestContainer.formatExactAmount(Long.MAX_VALUE))
        );
    }

    @Test
    @DisplayName("虚拟索引使用菜单槽位而非底层容器槽号")
    void virtualIndexUsesMenuSlotId() {
        assertAll(
                () -> assertEquals(-1, InfinityChestMenu.virtualIndexForMenuSlot(35)),
                () -> assertEquals(0, InfinityChestMenu.virtualIndexForMenuSlot(36)),
                () -> assertEquals(104, InfinityChestMenu.virtualIndexForMenuSlot(140)),
                () -> assertEquals(-1, InfinityChestMenu.virtualIndexForMenuSlot(141))
        );
    }

    @Test
    @DisplayName("紧凑视口在常用 GUI 缩放下完整显示")
    void compactViewportFitsCommonGuiScale() throws Exception {
        String screen = compact(Files.readString(SCREEN));
        assertAll(
                () -> assertEquals(15, InfinityChestContainer.WIDTH),
                () -> assertEquals(7, InfinityChestContainer.HEIGHT),
                () -> assertEquals(105, InfinityChestContainer.SIZE),
                () -> assertTrue(screen.contains("super(menu,inventory,title,302,238)")),
                () -> assertTrue(screen.contains("LEGACY_PLAYER_SECTION_Y"))
        );
    }

    @Test
    @DisplayName("empty virtual slots accept the authoritative server cursor")
    void emptyVirtualSlotCanDepositCarriedStack() throws Exception {
        String screen = compact(Files.readString(SCREEN));
        String packet = compact(Files.readString(PACKET));
        String menu = compact(Files.readString(MENU));
        assertAll(
                () -> assertTrue(screen.contains("!resource.isEmpty()||!menu.getCarried().isEmpty()")),
                () -> assertTrue(packet.contains("packet.resource().isEmpty()&&!menu.getCarried().isEmpty()")),
                () -> assertTrue(menu.contains("selected.isEmpty()&&!getCarried().isEmpty()")),
                () -> assertTrue(menu.contains("deposit(cursor,cursor.getAmount())"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
