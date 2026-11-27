package committee.nova.mods.avaritia.api.client.util.key;

import committee.nova.mods.avaritia.api.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;


public class GLFWKeyHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, Integer> KEY_NAME_TO_CODE = new HashMap<>();
    private static final Map<Integer, String> KEY_CODE_TO_NAME = new HashMap<>();
    private static final Map<String, Integer> MOUSE_NAME_TO_CODE = new HashMap<>();
    private static final Map<Integer, String> MOUSE_CODE_TO_NAME = new HashMap<>();

    // 操作类按键集合
    private static final Set<String> OPERATOR_KEYS = new HashSet<>(Arrays.asList(
            "Ctrl", "LeftCtrl", "RightCtrl", "LeftControl", "RightControl",
            "Shift", "LeftShift", "RightShift",
            "Alt", "LeftAlt", "RightAlt",
            "Super", "LeftSuper", "RightSuper",
            "Menu"
    ));

    static {
        Field[] fields = GLFWKey.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                // 键盘按键
                if (fieldName.startsWith("GLFW_KEY_")) {
                    int code = field.getInt(null);
                    String keyName = StringUtils.toPascalCase(fieldName.substring("GLFW_KEY_".length()));
                    KEY_NAME_TO_CODE.put(keyName, code);
                    KEY_CODE_TO_NAME.put(code, keyName);
                }
                // 鼠标按键
                else if (fieldName.startsWith("GLFW_MOUSE_BUTTON_")) {
                    int code = field.getInt(null);
                    String mouseName = StringUtils.toPascalCase("MOUSE_" + fieldName.substring("GLFW_MOUSE_BUTTON_".length()));
                    MOUSE_NAME_TO_CODE.put(mouseName, code);
                    MOUSE_CODE_TO_NAME.put(code, mouseName);
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Failed to get key code from field: {}", fieldName, e);
            }
        }
    }

    public static List<Integer> getKeyCodes(String... keyNames) {
        List<Integer> codes = new ArrayList<>();
        for (String name : keyNames) {
            Integer code = KEY_NAME_TO_CODE.get(name.toLowerCase());
            if (code != null) {
                codes.add(code);
            }
        }
        Collections.sort(codes);
        return codes;
    }

    public static List<Integer> getMouseCodes(List<String> mouseNames) {
        List<Integer> codes = new ArrayList<>();
        for (String name : mouseNames) {
            Integer code = MOUSE_NAME_TO_CODE.get(name.toLowerCase());
            if (code != null) {
                codes.add(code);
            }
        }
        Collections.sort(codes);
        return codes;
    }

    public static List<String> getKeyDisplayNames(boolean sort, int... codes) {
        List<String> operatorKeys = new ArrayList<>();
        List<String> normalKeys = new ArrayList<>();
        for (Integer code : codes) {
            String name = KEY_CODE_TO_NAME.get(code);
            if (StringUtils.isNullOrEmptyEx(name)) continue;
            if (sort && OPERATOR_KEYS.contains(name)) {
                operatorKeys.add(name);
            } else {
                normalKeys.add(name);
            }
        }
        if (sort) {
            // 按照映射中的 code 顺序排序
            operatorKeys.sort(Comparator.comparingInt(KEY_NAME_TO_CODE::get));
            normalKeys.sort(Comparator.comparingInt(KEY_NAME_TO_CODE::get));
        }
        List<String> result = new ArrayList<>();
        result.addAll(operatorKeys);
        result.addAll(normalKeys);
        return result;
    }

    public static List<String> getMouseDisplayNames(boolean sort, int... codes) {
        List<String> names = new ArrayList<>();
        for (Integer code : codes) {
            String name = MOUSE_CODE_TO_NAME.get(code);
            if (StringUtils.isNullOrEmptyEx(name)) continue;
            names.add(name);
        }
        if (sort) {
            names.sort(Comparator.comparingInt(MOUSE_NAME_TO_CODE::get));
        }
        return names;
    }

    public static String getKeyDisplayString(int... codes) {
        List<String> names = getKeyDisplayNames(true, codes);
        return String.join("+", names);
    }

    public static String getKeyDisplayStringInOrder(int... codes) {
        List<String> names = getKeyDisplayNames(false, codes);
        return String.join("+", names);
    }

    public static String getMouseDisplayString(int... codes) {
        List<String> names = getMouseDisplayNames(true, codes);
        return String.join("+", names);
    }

    public static String getMouseDisplayStringInOrder(int... codes) {
        List<String> names = getMouseDisplayNames(false, codes);
        return String.join("+", names);
    }

    public static List<String> getKeyRecordKeys(int... codes) {
        List<String> recordKeys = new ArrayList<>();
        List<Integer> sortedCodes = Arrays.stream(codes).boxed().sorted().toList();
        for (Integer code : sortedCodes) {
            String name = KEY_CODE_TO_NAME.get(code);
            if (name == null) continue;
            recordKeys.add("key.keyboard." + formatRecordName(name));
        }
        return recordKeys;
    }

    public static List<String> getMouseRecordKeys(int... codes) {
        List<String> recordKeys = new ArrayList<>();
        List<Integer> sortedCodes = Arrays.stream(codes).boxed().sorted().toList();
        for (Integer code : sortedCodes) {
            String name = MOUSE_CODE_TO_NAME.get(code);
            if (name == null) continue;
            recordKeys.add("key.mouse." + formatRecordName(name));
        }
        return recordKeys;
    }

    public static boolean matchKey(String displayString, int... codes) {
        List<String> generated = getKeyDisplayNames(true, codes);
        List<String> inputKeys = parseDisplayString(displayString);
        // 按照小写字母排序后再比较
        Comparator<String> comp = Comparator.comparing(String::toLowerCase);
        generated.sort(comp);
        inputKeys.sort(comp);
        return generated.equals(inputKeys);
    }

    public static boolean matchKeyInOrder(String displayString, int... codes) {
        List<String> generated = getKeyDisplayNames(false, codes);
        List<String> inputKeys = parseDisplayString(displayString);
        return generated.equals(inputKeys);
    }

    public static boolean matchMouse(String displayString, int... codes) {
        List<String> generated = getMouseDisplayNames(true, codes);
        List<String> inputKeys = parseDisplayString(displayString);
        Comparator<String> comp = Comparator.comparing(String::toLowerCase);
        generated.sort(comp);
        inputKeys.sort(comp);
        return generated.equals(inputKeys);
    }

    public static boolean matchMouseInOrder(String displayString, int... codes) {
        List<String> generated = getMouseDisplayNames(false, codes);
        List<String> inputKeys = parseDisplayString(displayString);
        return generated.equals(inputKeys);
    }

    private static List<String> parseDisplayString(String displayString) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isNullOrEmptyEx(displayString)) return list;

        for (String part : displayString.split("\\+")) {
            if (!part.trim().isEmpty()) {
                list.add(part.trim());
            }
        }
        return list;
    }

    private static String formatRecordName(String name) {
        String lower = name.toLowerCase();
        if (lower.startsWith("left")) {
            return "left" + (lower.length() > 4 ? "." + lower.substring(4) : "");
        } else if (lower.startsWith("right")) {
            return "right" + (lower.length() > 5 ? "." + lower.substring(5) : "");
        } else {
            return lower;
        }
    }

}
