package committee.nova.mods.avaritia.core.chest;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author cnlimiter
 */
public class ClientChestHandler extends ChestHandler {
    @Nullable
    public InfinityChestContainer container;

    public ClientChestHandler() {
    }

    public void addListener(InfinityChestContainer container) {
        this.container = container;
    }

    public void removeListener() {
        this.container = null;
        storageItems.clear();
    }

    @Override
    public void onItemChanged(ItemSuper itemId, boolean listChanged) {
        super.onItemChanged(itemId, listChanged);
        if (container != null) container.refreshContainer(listChanged);
    }

    public void update(Collection<ItemSuper> tag) {
        if (container == null) return;
        AtomicBoolean fullUpdate = new AtomicBoolean(false);
        AtomicBoolean needRefreshContainer = new AtomicBoolean(false);
        tag.forEach(itemId -> {
            long count = itemId.getRealCount();
            if (count <= 0L) {
                if (storageItems.containsKey(itemId)) {
                    storageItems.remove(itemId);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            } else {
                if (storageItems.containsKey(itemId)) {
                    if (storageItems.get(itemId) != count) {
                        storageItems.replace(itemId, count);
                        needRefreshContainer.set(true);
                    }
                } else {
                    storageItems.put(itemId, count);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            }
        });

        if (needRefreshContainer.get()) container.refreshContainer(fullUpdate.get());
        if (fullUpdate.get()) {updateItemKeys();}
    }

    public void fullUpdate(Collection<ItemSuper> tag) {
        storageItems.clear();

        tag.forEach(itemId -> storageItems.put(itemId, itemId.getRealCount()));

        updateItemKeys();
        if (container != null) {
            container.refreshContainer(true);
        }
    }

    @Override
    public boolean isRemoved() {
        return false;
    }
}
