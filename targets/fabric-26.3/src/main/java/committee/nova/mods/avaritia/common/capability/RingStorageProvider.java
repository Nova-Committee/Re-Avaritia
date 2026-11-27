package committee.nova.mods.avaritia.common.capability;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

/** One-way reader for backpack inventories persisted by older Neutron Rings. */
public final class RingStorageProvider {
    public static final String LEGACY_STORAGE = "NeutronRingLegacyStorage";

    private static final RegistryOps<Tag> OPS = RegistryAccess
            .fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
            .createSerializationContext(NbtOps.INSTANCE);

    private RingStorageProvider() {
    }

    public static List<ItemStack> takeLegacyItems(ItemStack ring) {
        List<ItemStack> items = new ArrayList<>();
        CustomData data = ring.get(DataComponents.CUSTOM_DATA);
        if (data == null) return items;
        CompoundTag tag = data.copyTag();
        if (tag.contains(LEGACY_STORAGE)) {
            readItems(tag.getCompoundOrEmpty(LEGACY_STORAGE), items);
            CustomData.update(DataComponents.CUSTOM_DATA, ring, t -> t.remove(LEGACY_STORAGE));
        }
        return items;
    }

    private static void readItems(CompoundTag data, List<ItemStack> items) {
        if (data.contains("Items")) {
            ListTag entries = data.getListOrEmpty("Items");
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompoundOrEmpty(i);
                ItemStack.CODEC.parse(OPS, entry).result().ifPresent(stack -> {
                    int extended = entry.getIntOr("ExtendedCount", stack.getCount());
                    stack.setCount(extended);
                    if (!stack.isEmpty()) {
                        items.add(stack);
                    }
                });
            }
        } else {
            for (String key : data.keySet()) {
                if (data.get(key) instanceof CompoundTag nested) {
                    readItems(nested, items);
                }
            }
        }
    }
}
