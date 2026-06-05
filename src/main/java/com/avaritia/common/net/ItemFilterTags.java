package com.avaritia.common.net;

import net.minecraft.nbt.CompoundTag;

public final class ItemFilterTags {
    private ItemFilterTags() {
    }

    public static CompoundTag mutate(CompoundTag currentFilters, String key, CompoundTag customData, int action) {
        CompoundTag filters = currentFilters == null ? new CompoundTag() : currentFilters.copy();
        if (action == 2) {
            return new CompoundTag();
        }
        if (key == null || key.isBlank()) {
            return filters;
        }

        if (action == 0) {
            filters.put(key, customData == null ? new CompoundTag() : customData.copy());
        } else if (action == 1) {
            filters.remove(key);
        }
        return filters;
    }
}
