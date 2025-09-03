package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.common.wrappers.StorageItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/4 13:54
 * @Description:
 */
public class SortUtils {
    public static final Comparator<StorageItem> ITEM_REGISTRY_NAME = (item1, item2) -> {
        ResourceLocation registryName1 = BuiltInRegistries.ITEM.getKey(item1.getStack().getItem());
        ResourceLocation registryName2 = BuiltInRegistries.ITEM.getKey(item2.getStack().getItem());
        if (registryName1 != null && registryName2 != null) {
            return registryName1.getPath().compareTo(registryName2.getPath());
        } else if (registryName1 == null && registryName2 == null) {
            return 0;
        } else {
            return registryName1 == null ? 1 : -1;
        }
    };
    public static final Comparator<StorageItem> ITEM_COUNT = Comparator.comparingLong(StorageItem::getCount);
    public static final Comparator<StorageItem> MOD_ID = (item1, item2) -> {
        ResourceLocation registryName1 = BuiltInRegistries.ITEM.getKey(item1.getStack().getItem());
        ResourceLocation registryName2 = BuiltInRegistries.ITEM.getKey(item2.getStack().getItem());
        if (registryName1 != null && registryName2 != null) {
            return registryName1.getNamespace().compareTo(registryName2.getNamespace());
        } else if (registryName1 == null && registryName2 == null) {
            return 0;
        } else {
            return registryName1 == null ? 1 : -1;
        }
    };
    public static final Comparator<StorageItem> ITEM_NAME = (item1, item2) -> {
        String name1 = item1.getStack().getDisplayName().getString();
        String name2 = item2.getStack().getDisplayName().getString();
        return name1.compareTo(name2);
    };
    public static final Comparator<StorageItem> DEFAULT_1 = ITEM_REGISTRY_NAME.thenComparing(MOD_ID).thenComparing(ITEM_COUNT.reversed());
    public static final Comparator<StorageItem> DEFAULT_2 = ITEM_NAME.thenComparing(ITEM_COUNT.reversed());
    public static final Comparator<StorageItem> DEFAULT_3 = ITEM_COUNT.reversed().thenComparing(ITEM_NAME);

    public static <K1, K2, V> Map<K2, V> convertKeys(
            Map<K1, V> originalMap,
            Function<K1, K2> keyConverter) {

        return originalMap.entrySet().stream()
                .map(entry -> {
                    try {
                        K2 newKey = keyConverter.apply(entry.getKey());
                        return new AbstractMap.SimpleEntry<>(newKey, entry.getValue());
                    } catch (Exception e) {
                        return null; // 返回null，后续过滤
                    }
                })
                .filter(entry -> entry != null && entry.getKey() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,
                        HashMap::new
                ));
    }

    public static int sortFromCount(String s1, String s2, Map<String, Long> storageItems, boolean reverseOrder) {
        int i;
        if (reverseOrder) {
            i = storageItems.get(s2).compareTo(storageItems.get(s1));
        } else {
            i = storageItems.get(s1).compareTo(storageItems.get(s2));
        }
        if (i == 0) i = s1.compareTo(s2);
        return i;
    }

    public static int sortFromRightID(String s1, String s2) {
        int i = s1.indexOf(":");
        String a = s1.substring(i + 1);
        int j = s2.indexOf(":");
        String b = s2.substring(j + 1);
        int k = a.compareTo(b);
        if (k == 0) k = s1.compareTo(s2);
        return k;
    }

    public static int sortFromMirrorID(String s1, String s2) {
        char[] a = s1.toCharArray();
        char[] b = s2.toCharArray();
        int j = a.length - 1;
        int k = b.length - 1;
        int l;
        int min = Math.min(a.length, b.length);
        for (int i = 0; i < min; i++) {
            l = Character.compare(a[j], b[k]);
            if (l != 0) return l;
            j--;
            k--;
        }
        return Integer.compare(a.length, b.length);
    }

    public static class Sort {
        public static final byte ID_ASCENDING = 0;
        public static final byte ID_DESCENDING = 1;
        public static final byte NAMESPACE_ID_ASCENDING = 2;
        public static final byte NAMESPACE_ID_DESCENDING = 3;
        public static final byte MIRROR_ID_ASCENDING = 4;
        public static final byte MIRROR_ID_DESCENDING = 5;
        public static final byte COUNT_ASCENDING = 6;
        public static final byte COUNT_DESCENDING = 7;
    }
}
