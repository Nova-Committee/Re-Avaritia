package committee.nova.mods.avaritia.core.chest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

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
    }

    @Override
    public void onItemChanged(ItemStack itemId, boolean listChanged) {
        super.onItemChanged(itemId, listChanged);
        if (container != null) container.refreshContainer(listChanged);
    }

    public void update(CompoundTag tag) {
        if (container == null) return;
        CompoundTag items = tag.getCompound("items");
        AtomicBoolean fullUpdate = new AtomicBoolean(false);
        AtomicBoolean needRefreshContainer = new AtomicBoolean(false);
        items.getAllKeys().forEach(itemId -> {
            CompoundTag itemTag = items.getCompound(itemId);
            long count = itemTag.getLong("realCount");
            ItemStack item = ItemStack.of(itemTag.getCompound("item"));
            if (count <= 0L) {
                if (storageItems.containsKey(item)) {
                    storageItems.remove(item);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            } else {
                if (storageItems.containsKey(item)) {
                    if (storageItems.get(item) != count) {
                        storageItems.replace(item, count);
                        needRefreshContainer.set(true);
                    }
                } else {
                    storageItems.put(item, count);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            }
        });
        if (needRefreshContainer.get()) container.refreshContainer(fullUpdate.get());
        if (fullUpdate.get()) {updateItemKeys();}
    }

    public void fullUpdate(CompoundTag tag) {
        CompoundTag items = tag.getCompound("items");
        storageItems.clear();
        items.getAllKeys().forEach(itemId -> {
            CompoundTag itemTag = items.getCompound(itemId);
            long count = itemTag.getLong("realCount");
            ItemStack item = ItemStack.of(itemTag.getCompound("item"));
            storageItems.put(item, count);
        });
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
