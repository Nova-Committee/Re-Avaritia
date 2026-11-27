package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.container.InfinityChestContainer;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/** 当前连接内的无尽箱镜像；不允许在客户端直接修改。 */
public final class ClientChestHandler extends ChestHandler {
    private InfinityChestContainer listener;
    private final PagedUpdate<StoredItem> pendingFullUpdate = new PagedUpdate<>(MAX_VARIANTS);

    public void addListener(InfinityChestContainer container) {
        listener = container;
    }

    public void removeListener(InfinityChestContainer container) {
        if (listener == container) {
            listener = null;
        }
    }

    public void clear() {
        load(null);
        pendingFullUpdate.clear();
        listener = null;
    }

    public void fullUpdatePage(Collection<StoredItem> entries, boolean reset, boolean last) {
        pendingFullUpdate.accept(entries, reset, last).ifPresent(completed -> {
            load(completed);
            refresh(true);
        });
    }

    public void update(Collection<StoredItem> changed, Collection<ItemResource> removed) {
        var merged = entries().stream().collect(Collectors.toMap(
                StoredItem::resource, StoredItem::amount, Math::max, LinkedHashMap::new));
        if (removed != null) {
            removed.forEach(merged::remove);
        }
        if (changed != null) {
            changed.forEach(entry -> merged.put(entry.resource(), entry.amount()));
        }
        load(merged.entrySet().stream().map(entry -> new StoredItem(entry.getKey(), entry.getValue())).toList());
        refresh(removed != null && !removed.isEmpty());
    }

    private void refresh(boolean structureChanged) {
        if (listener != null) {
            listener.refresh(structureChanged);
        }
    }
}
