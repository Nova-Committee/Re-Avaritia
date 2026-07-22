package committee.nova.mods.avaritia.core.chest;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyInfinityChestDataFixTest {

    @BeforeAll
    static void detectGameVersion() {
        SharedConstants.tryDetectVersion();
    }

    @Test
    void upgradesLegacyVanillaTagsToDataComponents() {
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("Damage", 7);
        legacy.putInt("RepairCost", 3);
        legacy.putString("legacy_custom", "preserved");
        CompoundTag display = new CompoundTag();
        display.putString("Name", "{\"text\":\"Legacy\"}");
        legacy.put("display", display);

        CompoundTag upgraded = ServerChestHandler.upgradeLegacyStackTag(
                ResourceLocation.withDefaultNamespace("diamond_sword"), legacy);
        CompoundTag components = upgraded.getCompound("components");

        assertEquals("minecraft:diamond_sword", upgraded.getString("id"));
        assertEquals(1, upgraded.getInt("count"));
        assertEquals(7, components.getInt("minecraft:damage"));
        assertEquals(3, components.getInt("minecraft:repair_cost"));
        assertTrue(components.contains("minecraft:custom_name"));
        assertEquals("preserved",
                components.getCompound("minecraft:custom_data").getString("legacy_custom"));
    }

    @Test
    void reportsUnavailableLegacyItemsAsIncomplete() {
        CompoundTag data = new CompoundTag();
        CompoundTag items = new CompoundTag();
        items.putLong("missing_mod:removed_item", 42L);
        data.put("items", items);

        ServerChestHandler handler = new ServerChestHandler();
        handler.initialize(data);

        assertTrue(!handler.isLoadComplete());
        assertTrue(handler.storageItems.isEmpty());
    }
}
