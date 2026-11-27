package committee.nova.mods.avaritia.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** One-way reader for backpack inventories persisted by older Neutron Rings. */
public final class RingStorageProvider {
    public static final String LEGACY_STORAGE = "NeutronRingLegacyStorage";

    private RingStorageProvider() {
    }

    public static List<ItemStack> takeLegacyItems(ItemStack ring) {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag tag = ring.getTag();
        if (tag != null && tag.contains(LEGACY_STORAGE, Tag.TAG_COMPOUND)) {
            readItems(tag.getCompound(LEGACY_STORAGE), items);
            ring.removeTagKey(LEGACY_STORAGE);
        }
        return items;
    }

    private static void readItems(CompoundTag data, List<ItemStack> items) {
        if (data.contains("Items", Tag.TAG_LIST)) {
            ListTag entries = data.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                ItemStack stack = ItemStack.of(entry);
                if (entry.contains("ExtendedCount", Tag.TAG_INT)) {
                    stack.setCount(entry.getInt("ExtendedCount"));
                }
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        } else {
            for (String key : data.getAllKeys()) {
                if (data.contains(key, Tag.TAG_COMPOUND)) {
                    readItems(data.getCompound(key), items);
                }
            }
        }
    }
}
