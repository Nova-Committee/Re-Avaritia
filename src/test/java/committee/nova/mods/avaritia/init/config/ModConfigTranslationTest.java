package committee.nova.mods.avaritia.init.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModConfigTranslationTest {
    private static final Path ENGLISH_LANGUAGE = Path.of("src/generated/resources/assets/avaritia/lang/en_us.json");
    private static final Pattern CONFIG_PATH = Pattern.compile("[a-z][a-z0-9]*(?:_[a-z0-9]+)*");

    @Test
    void configPathsTranslationsAndCommentsStaySeparated() throws Exception {
        List<ModConfigSpec.ConfigValue<?>> values = configValues();
        assertEquals(36, values.size(), "新增配置项时应同步验证配置路径与翻译键");

        for (ModConfigSpec.ConfigValue<?> value : values) {
            List<String> path = value.getPath();
            assertEquals(2, path.size(), "配置路径应只包含分类和配置名: " + path);

            String name = path.get(path.size() - 1);
            assertTrue(CONFIG_PATH.matcher(name).matches(), "配置名应使用 snake_case: " + name);

            ModConfigSpec.ValueSpec spec = value.getSpec();
            String translationKey = "config.avaritia." + name;
            assertEquals(translationKey, spec.getTranslationKey(), "翻译键应由配置名派生: " + path);
            assertNotNull(spec.getComment(), "TOML 注释不能为空: " + path);
            assertFalse(spec.getComment().isBlank(), "TOML 注释不能为空: " + path);
            assertFalse(spec.getComment().startsWith("config."), "TOML 注释不能误用翻译键: " + path);
        }
    }

    @Test
    void everyConfigEntryHasTitleAndDescriptionTranslations() throws Exception {
        JsonObject english;
        try (Reader reader = Files.newBufferedReader(ENGLISH_LANGUAGE, StandardCharsets.UTF_8)) {
            english = JsonParser.parseReader(reader).getAsJsonObject();
        }

        for (ModConfigSpec.ConfigValue<?> value : configValues()) {
            String translationKey = value.getSpec().getTranslationKey();
            assertTrue(english.has(translationKey), "缺少配置标题翻译: " + translationKey);
            assertTrue(english.has(translationKey + ".tooltip"), "缺少配置描述翻译: " + translationKey);
        }
    }

    private static List<ModConfigSpec.ConfigValue<?>> configValues() {
        return Arrays.stream(ModConfig.class.getFields())
                .filter(field -> ModConfigSpec.ConfigValue.class.isAssignableFrom(field.getType()))
                .map(ModConfigTranslationTest::readConfigValue)
                .toList();
    }

    private static ModConfigSpec.ConfigValue<?> readConfigValue(Field field) {
        try {
            return (ModConfigSpec.ConfigValue<?>) field.get(null);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("无法读取配置字段: " + field.getName(), exception);
        }
    }
}
