package committee.nova.mods.avaritia.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaritiaKeyBindingsTest {
    @Test
    void controlsSortAvaritiaKeysAfterVanillaAndSparseModCategories() throws ReflectiveOperationException {
        Map<String, KeyMapping> allKeys = staticField(KeyMapping.class, "ALL");
        Map<InputConstants.Key, List<KeyMapping>> keyLookup = staticField(KeyMapping.class, "MAP");
        List<KeyMapping.Category> sortOrder = staticField(KeyMapping.Category.class, "SORT_ORDER");
        List<KeyMapping.Category> savedOrder = new ArrayList<>(sortOrder);
        try {
            // 先引用 AvaritiaClient 触发静态初始化，保证 KEY_CATEGORY 的注册顺序早于下面的测试类别，
            // 模拟"本模组在客户端初始化时注册、其他模组更晚注册"的真实顺序。
            KeyMapping[] avaritiaKeys = {
                    AvaritiaClient.FILTER_KEY,
                    AvaritiaClient.RING_KEY,
                    AvaritiaClient.CONFIG_KEY
            };
            // 26.3 的 Category 按注册顺序排序；另一个模组可能在本模组之后注册自己的类别。
            KeyMapping vanilla = new KeyMapping("key.avaritia.test.vanilla", -1, KeyMapping.Category.MOVEMENT);
            KeyMapping.Category otherCategory = KeyMapping.Category.register(
                    Identifier.parse("avaritia_test:other_category"));
            KeyMapping otherMod = new KeyMapping("key.avaritia.test.other", -1, otherCategory);
            KeyMapping[] controls = {avaritiaKeys[0], otherMod, avaritiaKeys[1], vanilla, avaritiaKeys[2]};
            Arrays.sort(controls);
            // Avaritia 的类别注册早于 otherMod、晚于原版：vanilla < avaritia < otherMod
            assertEquals(vanilla, controls[0]);
            assertEquals(otherMod, controls[controls.length - 1]);
            for (KeyMapping key : avaritiaKeys) {
                assertTrue(key.compareTo(vanilla) > 0);
                assertTrue(key.compareTo(otherMod) < 0);
            }
            for (int i = 0; i < savedOrder.size(); i++) {
                assertEquals(savedOrder.get(i), sortOrder.get(i), "Existing category order must not change");
            }
        } finally {
            sortOrder.clear();
            sortOrder.addAll(savedOrder);
            for (String name : List.of("key.avaritia.test.vanilla", "key.avaritia.test.other")) {
                KeyMapping removed = allKeys.remove(name);
                if (removed != null) {
                    keyLookup.values().forEach(list -> list.remove(removed));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T staticField(Class<?> owner, String name) throws ReflectiveOperationException {
        var field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(null);
    }
}
