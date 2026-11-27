package committee.nova.mods.avaritia.common.net;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class C2SItemFilterPacketTest {
    @Test
    void addItemWithoutCustomDataStoresEmptyFilterValue() {
        CompoundTag result = ItemFilterTags.mutate(new CompoundTag(), "minecraft:dirt", null, 0);

        assertAll(
                () -> assertTrue(result.contains("minecraft:dirt")),
                () -> assertTrue(result.getCompoundOrEmpty("minecraft:dirt").isEmpty())
        );
    }

    @Test
    void addItemCopiesCustomData() {
        CompoundTag customData = new CompoundTag();
        customData.putString("marker", "cosmic");

        CompoundTag result = ItemFilterTags.mutate(new CompoundTag(), "minecraft:diamond", customData, 0);

        assertEquals("cosmic", result.getCompoundOrEmpty("minecraft:diamond").getStringOr("marker", ""));
    }

    @Test
    void removeItemDeletesOnlyMatchingKey() {
        CompoundTag current = new CompoundTag();
        current.put("minecraft:dirt", new CompoundTag());
        current.put("minecraft:stone", new CompoundTag());

        CompoundTag result = ItemFilterTags.mutate(current, "minecraft:dirt", null, 1);

        assertAll(
                () -> assertFalse(result.contains("minecraft:dirt")),
                () -> assertTrue(result.contains("minecraft:stone")),
                () -> assertTrue(current.contains("minecraft:dirt"))
        );
    }

    @Test
    void clearReturnsEmptyTag() {
        CompoundTag current = new CompoundTag();
        current.put("minecraft:dirt", new CompoundTag());

        CompoundTag result = ItemFilterTags.mutate(current, "", null, 2);

        assertAll(
                () -> assertTrue(result.isEmpty()),
                () -> assertTrue(current.contains("minecraft:dirt"))
        );
    }
}
