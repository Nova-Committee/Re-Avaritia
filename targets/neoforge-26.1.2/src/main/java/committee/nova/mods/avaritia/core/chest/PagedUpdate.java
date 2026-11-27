package committee.nova.mods.avaritia.core.chest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 将有序网络分页组装成一次原子可见的全量更新。 */
final class PagedUpdate<T> {
    private final int maximumEntries;
    private final List<T> staging = new ArrayList<>();
    private boolean receiving;

    PagedUpdate(int maximumEntries) {
        if (maximumEntries <= 0) throw new IllegalArgumentException("maximumEntries must be positive");
        this.maximumEntries = maximumEntries;
    }

    Optional<List<T>> accept(Collection<T> page, boolean reset, boolean last) {
        if (reset) {
            staging.clear();
            receiving = true;
        }
        if (!receiving) return Optional.empty();
        staging.addAll(page);
        if (staging.size() > maximumEntries) {
            clear();
            return Optional.empty();
        }
        if (!last) return Optional.empty();
        List<T> completed = List.copyOf(staging);
        clear();
        return Optional.of(completed);
    }

    void clear() {
        staging.clear();
        receiving = false;
    }
}
