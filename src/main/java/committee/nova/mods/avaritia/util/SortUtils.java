package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.common.wrappers.StorageItem;
import committee.nova.mods.avaritia.core.chest.ItemSuper;
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

    public static int sortFromCount(ItemSuper s1, ItemSuper s2, Map<ItemSuper, Long> storageItems, boolean reverseOrder) {
        int i;
        if (reverseOrder) {
            i = storageItems.get(s2).compareTo(storageItems.get(s1));
        } else {
            i = storageItems.get(s1).compareTo(storageItems.get(s2));
        }
        if (i == 0) i = s1.toString().compareTo(s2.toString());
        return i;
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
            j--; k--;
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
