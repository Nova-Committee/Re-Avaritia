package committee.nova.mods.avaritia.init.data.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaritiaLanguageProviderTest {
    private static final Path LANGUAGES = Path.of("src/generated/resources/assets/avaritia/lang");
    private static final Path ITEM_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia/common/item/misc");
    private static final List<String> LOCALES = List.of("en_us", "ja_jp", "uk_ua", "zh_cn", "zh_tw");
    private static final Pattern PLACEHOLDER = Pattern.compile("%(?:\\d+\\$)?([a-zA-Z%])");

    @Test
    void generatedLocalesUseTheSameKeysAndPlaceholderShapes() throws IOException {
        JsonObject english = readLocale("en_us");

        for (String locale : LOCALES) {
            JsonObject language = readLocale(locale);
            assertEquals(english.keySet(), language.keySet(), locale + " 的翻译键应与英文一致");

            for (String key : english.keySet()) {
                // 1.21 基线的中/繁 smithing 文案只有一个占位符，迁移时必须原样保留旧值。
                if (key.equals("tooltip.avaritia.smithing")) continue;
                assertEquals(
                        placeholders(english.get(key).getAsString()),
                        placeholders(language.get(key).getAsString()),
                        locale + " 的格式参数与英文不一致: " + key
                );
            }
        }
    }

    @Test
    void migratedDescriptionsStayEnabled() throws IOException {
        assertTrue(
                compact(readSource("InfinityClockItem.java"))
                        .contains("super(ModRarities.COSMIC.getValue(),true,ModItems.properties().stacksTo(1));"),
                "无尽时钟应保留 1.20.1 的物品描述"
        );
        assertTrue(
                compact(readSource("InfinityUmbrellaItem.java"))
                        .contains("super(ModRarities.COSMIC.getValue(),true,ModItems.properties().stacksTo(1));"),
                "无尽伞应保留 1.20.1 的物品描述"
        );
    }

    @Test
    void allLocalesContainNeutronRingDescription() throws IOException {
        for (String locale : LOCALES) {
            assertTrue(
                    readLocale(locale).has("tooltip.avaritia.neutron_ring.desc"),
                    locale + " 缺少中子态指环描述"
            );
        }
    }

    private static JsonObject readLocale(String locale) throws IOException {
        Path path = LANGUAGES.resolve(locale + ".json");
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String readSource(String name) throws IOException {
        return Files.readString(ITEM_SOURCES.resolve(name), StandardCharsets.UTF_8);
    }

    private static List<String> placeholders(String value) {
        Matcher matcher = PLACEHOLDER.matcher(value);
        List<String> placeholders = new ArrayList<>();
        while (matcher.find()) {
            if (!"%".equals(matcher.group(1))) {
                placeholders.add(matcher.group(1));
            }
        }
        return placeholders;
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
