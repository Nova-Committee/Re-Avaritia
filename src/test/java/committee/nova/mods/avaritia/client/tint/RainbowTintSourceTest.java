package committee.nova.mods.avaritia.client.tint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("无尽箱物品动态颜色")
class RainbowTintSourceTest {
    private static final Path ITEM_RENDERER = Path.of(
            "src/main/java/committee/nova/mods/avaritia/client/render/item/InfinityChestItemRender.java");

    @Test
    @DisplayName("彩虹颜色保持不透明并随周期变化")
    void producesOpaqueChangingColors() {
        int start = RainbowTintSource.colorAt(0L);
        int halfCycle = RainbowTintSource.colorAt(9_000L);
        assertAll(
                () -> assertEquals(0xFFFF0000, start),
                () -> assertEquals(0xFF00FFFF, halfCycle),
                () -> assertEquals(0xFF000000, start & 0xFF000000),
                () -> assertNotEquals(start, halfCycle)
        );
    }

    @Test
    @DisplayName("无尽箱特殊渲染器复用统一颜色源")
    void infinityChestRendererUsesSharedColorSource() throws Exception {
        String source = Files.readString(ITEM_RENDERER).replaceAll("\\s+", "");
        assertTrue(source.contains("RainbowTintSource.currentColor()"));
    }
}
