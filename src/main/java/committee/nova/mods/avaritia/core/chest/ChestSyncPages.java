package committee.nova.mods.avaritia.core.chest;

import java.util.ArrayList;
import java.util.List;

/** 有界网络页切分，空状态也会产生一张终止页。 */
final class ChestSyncPages {
    private ChestSyncPages() {
    }

    static <T> List<List<T>> partition(List<T> entries, int pageSize) {
        if (pageSize <= 0) throw new IllegalArgumentException("pageSize must be positive");
        if (entries.isEmpty()) return List.of(List.of());
        List<List<T>> pages = new ArrayList<>((entries.size() + pageSize - 1) / pageSize);
        for (int start = 0; start < entries.size(); start += pageSize) {
            pages.add(List.copyOf(entries.subList(start, Math.min(entries.size(), start + pageSize))));
        }
        return List.copyOf(pages);
    }
}
