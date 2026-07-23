package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.AvaritiaClientMod;
import committee.nova.mods.avaritia.init.config.ModConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaritiaConfigScreenTest {
    private static final Path CONFIG_SCREEN_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/client/screen/AvaritiaConfigScreen.java");
    private static final Path CLIENT_MOD_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/client/AvaritiaClientMod.java");
    private static final Pattern CONFIG_ENTRY = Pattern.compile(
            "add(?:Boolean|Int|Double)Entry\\(\\s*\"[^\"]+\"\\s*,\\s*ModConfig\\.([A-Za-z0-9_]+)");
    private static final Pattern CATEGORY_ENTRY = Pattern.compile("addCategoryHeader\\(\\s*\"");

    @Test
    void configScreenContainsEveryRegisteredConfigValueExactlyOnce() throws IOException {
        Set<String> expected = configFieldNames();
        Set<String> actual = new HashSet<>();
        String source = Files.readString(CONFIG_SCREEN_SOURCE, StandardCharsets.UTF_8);
        Matcher matcher = CONFIG_ENTRY.matcher(source);
        while (matcher.find()) {
            assertTrue(actual.add(matcher.group(1)), "配置界面重复注册了配置项: " + matcher.group(1));
        }

        assertEquals(4, CATEGORY_ENTRY.matcher(source).results().count(),
                "配置界面应保留 tools、emc、channel、misc 四个分类");
        assertEquals(expected, actual, "配置界面必须展示 ModConfig 中的全部配置项");
    }

    @Test
    void clientModEntryIsRestrictedToClientDistribution() {
        Mod annotation = AvaritiaClientMod.class.getAnnotation(Mod.class);
        assertNotNull(annotation, "客户端配置入口必须注册为 NeoForge 模组入口");
        assertEquals(Const.MOD_ID, annotation.value());
        assertArrayEquals(new Dist[]{Dist.CLIENT}, annotation.dist());
    }

    @Test
    void clientModEntryRegistersCustomConfigScreenFactory() throws IOException {
        String source = Files.readString(CLIENT_MOD_SOURCE, StandardCharsets.UTF_8).replaceAll("\\s+", "");
        assertTrue(source.contains(
                        "modContainer.registerExtensionPoint(IConfigScreenFactory.class," +
                                "(container,parent)->newAvaritiaConfigScreen(parent));"),
                "模组列表入口必须创建自定义 Avaritia 配置界面");
    }

    private static Set<String> configFieldNames() {
        Set<String> values = new HashSet<>();
        for (Field field : Arrays.stream(ModConfig.class.getFields())
                .filter(field -> ModConfigSpec.ConfigValue.class.isAssignableFrom(field.getType()))
                .toList()) {
            values.add(field.getName());
        }
        return values;
    }
}
