package committee.nova.mods.avaritia.core.chest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
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
        nbtDataCache.clear();
    }

    @Override
    public void onItemChanged(String itemId, boolean listChanged) {
        super.onItemChanged(itemId, listChanged);
        if (container != null) container.refreshContainer(listChanged);
    }

    public void update(CompoundTag tag) {
        if (container == null) return;
        CompoundTag items = tag.getCompound("items");
        CompoundTag nbtData = tag.contains("nbtData") ? tag.getCompound("nbtData") : null;

        AtomicBoolean fullUpdate = new AtomicBoolean(false);
        AtomicBoolean needRefreshContainer = new AtomicBoolean(false);
        items.getAllKeys().forEach(itemId -> {
            long count = items.getLong(itemId);
            if (count <= 0L) {
                if (storageItems.containsKey(itemId)) {
                    storageItems.remove(itemId);
                    nbtDataCache.remove(itemId);
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

        // 处理NBT数据
        if (nbtData != null) {
            nbtData.getAllKeys().forEach(itemId -> {
                Tag nbtTag = nbtData.get(itemId);
                if (nbtTag instanceof CompoundTag) {
                    nbtDataCache.put(itemId, nbtTag);
                }
            });
        }

        if (needRefreshContainer.get()) container.refreshContainer(fullUpdate.get());
        if (fullUpdate.get()) {updateItemKeys();}
    }

    public void fullUpdate(CompoundTag tag) {
        CompoundTag items = tag.getCompound("items");
        CompoundTag nbtData = tag.contains("nbtData") ? tag.getCompound("nbtData") : null;

        storageItems.clear();
        nbtDataCache.clear();

        items.getAllKeys().forEach(itemId -> storageItems.put(itemId, items.getLong(itemId)));

        if (nbtData != null) {
            nbtData.getAllKeys().forEach(itemId -> {
                Tag nbtTag = nbtData.get(itemId);
                if (nbtTag instanceof CompoundTag) {
                    nbtDataCache.put(itemId, nbtTag);
                }
            });
        }

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
