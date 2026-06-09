package committee.nova.mods.avaritia.api.utils.java;

import java.util.Optional;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/3 11:24
 * @Description:
 */
public class EnumUtils {
    public static <T extends Enum<T>> Optional<T> getEnumFromString(Class<T> clazz, String string) {
        T ret = null;
        if (clazz != null && string != null && !string.isEmpty()) {
            try {
                ret = Enum.valueOf(clazz, string.trim());
            } catch (IllegalArgumentException ex) {}
            try {
                ret = Enum.valueOf(clazz, string.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {}
            try {
                ret = Enum.valueOf(clazz,
                        string.trim().substring(0, 1).toUpperCase() + string.trim().substring(1).toLowerCase());
            } catch (IllegalArgumentException ex) {}
            try {
                ret = Enum.valueOf(clazz, string.trim().toLowerCase());
            } catch (IllegalArgumentException ex) {}
        }
        return Optional.ofNullable(ret);
    }
}
