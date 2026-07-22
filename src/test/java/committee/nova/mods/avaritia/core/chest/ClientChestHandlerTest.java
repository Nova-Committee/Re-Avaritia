package committee.nova.mods.avaritia.core.chest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("无尽箱客户端分页镜像")
class ClientChestHandlerTest {
    @Test
    @DisplayName("分页只在末片到达后原子发布")
    void pagesBecomeVisibleOnlyAtTheLastPage() {
        PagedUpdate<String> update = new PagedUpdate<>(4);
        assertTrue(update.accept(List.of("a", "b"), true, false).isEmpty());
        var completed = update.accept(List.of("c"), false, true);
        assertTrue(completed.isPresent());
        assertEquals(List.of("a", "b", "c"), completed.orElseThrow());
        assertTrue(update.accept(List.of("stale"), false, true).isEmpty());
    }

    @Test
    @DisplayName("越界分页被丢弃且下一首片可恢复")
    void oversizedSequenceIsRejected() {
        PagedUpdate<String> update = new PagedUpdate<>(2);
        assertTrue(update.accept(List.of("a", "b", "c"), true, true).isEmpty());
        var recovered = update.accept(List.of("d"), true, true);
        assertAll(
                () -> assertTrue(recovered.isPresent()),
                () -> assertEquals(List.of("d"), recovered.orElseThrow())
        );
    }

    @Test
    @DisplayName("65536 种条目按 512 条固定分页且空状态仍有终止页")
    void fullStateIsBoundedIntoPages() {
        List<Integer> entries = java.util.stream.IntStream.range(0, 65_536).boxed().toList();
        List<List<Integer>> pages = ChestSyncPages.partition(entries, 512);
        assertAll(
                () -> assertEquals(128, pages.size()),
                () -> assertTrue(pages.stream().allMatch(page -> page.size() <= 512)),
                () -> assertEquals(entries, pages.stream().flatMap(List::stream).toList()),
                () -> assertEquals(List.of(List.of()), ChestSyncPages.partition(List.of(), 512))
        );
    }
}
