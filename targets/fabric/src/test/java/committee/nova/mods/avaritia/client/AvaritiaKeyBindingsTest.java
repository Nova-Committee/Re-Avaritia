package committee.nova.mods.avaritia.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaritiaKeyBindingsTest {
    @Test
    void controlsSortAvaritiaKeysAfterVanillaAndSparseModCategories() throws ReflectiveOperationException {
        Map<String, Integer> categoryOrder = KeyMapping.CATEGORY_SORT_ORDER;
        Map<String, KeyMapping> allKeys = staticField("ALL");
        Map<InputConstants.Key, KeyMapping> keyLookup = staticField("MAP");
        Set<String> categories = staticField("CATEGORIES");
        var savedOrder = new HashMap<>(categoryOrder);
        var savedKeys = new HashMap<>(allKeys);
        var savedLookup = new HashMap<>(keyLookup);
        var savedCategories = new HashSet<>(categories);
        try {
            // Another mod can register a rank larger than the number of categories.
            String otherCategory = "key.avaritia.test.other_category";
            categoryOrder.put(otherCategory, 100);
            KeyMapping vanilla = new KeyMapping("key.avaritia.test.vanilla", -1, KeyMapping.CATEGORY_MOVEMENT);
            KeyMapping otherMod = new KeyMapping("key.avaritia.test.other", -1, otherCategory);
            KeyMapping[] avaritiaKeys = {
                    AvaritiaForgeClient.FILTER_KEY,
                    AvaritiaForgeClient.RING_KEY,
                    AvaritiaForgeClient.CONFIG_KEY
            };
            KeyMapping[] controls = {avaritiaKeys[0], otherMod, avaritiaKeys[1], vanilla, avaritiaKeys[2]};
            Arrays.sort(controls);
            assertEquals(List.of(vanilla, otherMod), Arrays.asList(controls).subList(0, 2));
            for (KeyMapping key : avaritiaKeys) {
                assertTrue(key.compareTo(otherMod) > 0);
                assertTrue(otherMod.compareTo(key) < 0);
            }
            for (var entry : savedOrder.entrySet()) {
                assertEquals(entry.getValue(), categoryOrder.get(entry.getKey()), "Existing category order must not change");
            }
        } finally {
            categoryOrder.clear();
            categoryOrder.putAll(savedOrder);
            allKeys.clear();
            allKeys.putAll(savedKeys);
            keyLookup.clear();
            keyLookup.putAll(savedLookup);
            categories.clear();
            categories.addAll(savedCategories);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T staticField(String name) throws ReflectiveOperationException {
        var field = KeyMapping.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(null);
    }
}
