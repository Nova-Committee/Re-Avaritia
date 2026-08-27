package committee.nova.mods.avaritia.init.data.provider;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Avaritia 语言数据生成")
class AvaritiaLanguageProviderTest {
    private static final Set<String> LOCALES = Set.of("en_us", "zh_cn", "zh_tw", "ja_jp", "uk_ua");
    private static final Set<String> MIGRATED_KEYS = Set.of(
            "item.avaritia.infinity_spear",
            "item.avaritia.blaze_spear",
            "item.avaritia.crystal_spear",
            "tooltip.avaritia.infinity_spear.desc",
            "tooltip.avaritia.blaze_spear.desc",
            "tooltip.avaritia.crystal_spear.desc",
            "tooltip.avaritia.tool.lunge",
            "tooltip.avaritia.tool.infinity_spear_lunge",
            "tooltip.avaritia.infinity_spear_lunge.active",
            "tooltip.avaritia.tool.infinity_spear_normal",
            "tooltip.avaritia.tool.infinity_spear_long_range",
            "tooltip.avaritia.infinity_spear_long_range.desc",
            "message.avaritia.infinity_spear.marked",
            "message.avaritia.infinity_spear.target_unavailable",
            "message.avaritia.infinity_spear.no_target",
            "tooltip.avaritia.tool.crystal_shatter",
            "tooltip.avaritia.crystal_shatter.active",
            "tooltip.avaritia.tool.crystal_spear_normal",
            "tooltip.avaritia.tool.crystal_spear_sevenfold",
            "tooltip.avaritia.crystal_spear_sevenfold.remaining",
            "message.avaritia.crystal_spear.marked",
            "message.avaritia.crystal_spear.target_unavailable",
            "message.avaritia.crystal_spear.no_target",
            "message.avaritia.crystal_spear.exhausted",
            "config.jade.plugin_avaritia.spear_mark",
            "tooltip.avaritia.jade.spear_mark",
            "tooltip.avaritia.tool.blaze_spear_blast",
            "tooltip.avaritia.blaze_spear_blast.active",
            "tooltip.avaritia.tool.infinity_shield_normal",
            "tooltip.avaritia.tool.infinity_shield_defending",
            "tooltip.avaritia.tool.infinity_shield_definite_defending",
            "tooltip.avaritia.tool.infinity_shield_float",
            "item.avaritia.neutron_nautilus_armor",
            "item.avaritia.neutron_wolf_armor",
            "item.avaritia.neutron_harness"
    );

    @Test
    @DisplayName("五种语言均生成迁移后的全部翻译键")
    void migratedKeysExistInEveryLocale() {
        for (String locale : LOCALES) {
            JsonObject language = AvaritiaLanguageProvider.buildLanguage(locale);
            for (String key : MIGRATED_KEYS) {
                assertTrue(language.has(key), () -> locale + " 缺少翻译键 " + key);
            }
        }
    }

    @Test
    @DisplayName("英文和简体中文沿用迁移前的既有翻译")
    void existingEnglishAndSimplifiedChineseValuesStayUnchanged() {
        assertTranslations("en_us", Map.ofEntries(
                Map.entry("item.avaritia.infinity_spear", "Stellar Piercing Spear"),
                Map.entry("item.avaritia.blaze_spear", "Blaze Spear"),
                Map.entry("item.avaritia.crystal_spear", "Crystal Spear"),
                Map.entry("tooltip.avaritia.infinity_spear.desc", "Where starlight pierces, the cosmos yields."),
                Map.entry("tooltip.avaritia.blaze_spear.desc", "Pierce skeletal husks, incinerate all remnants."),
                Map.entry("tooltip.avaritia.crystal_spear.desc", "Cold light strides past every bound; spear thrust impales every foe."),
                Map.entry("tooltip.avaritia.tool.lunge", "Lunge"),
                Map.entry("tooltip.avaritia.tool.infinity_spear_lunge", "Lunge"),
                Map.entry("tooltip.avaritia.infinity_spear_lunge.active", "Lunge Mode Active"),
                Map.entry("tooltip.avaritia.tool.infinity_spear_normal", "Normal"),
                Map.entry("tooltip.avaritia.tool.infinity_spear_long_range", "Long-Range Thrust"),
                Map.entry("tooltip.avaritia.infinity_spear_long_range.desc", "Where the spear points, ten thousand miles become inches."),
                Map.entry("message.avaritia.infinity_spear.marked", "Marked target: %s"),
                Map.entry("message.avaritia.infinity_spear.target_unavailable", "The marked target is temporarily unavailable."),
                Map.entry("message.avaritia.infinity_spear.no_target", "No non-allied target is within reach."),
                Map.entry("tooltip.avaritia.tool.crystal_shatter", "Crystal Shatter"),
                Map.entry("tooltip.avaritia.crystal_shatter.active", "Crystal Shatter Active"),
                Map.entry("tooltip.avaritia.tool.crystal_spear_normal", "Normal"),
                Map.entry("tooltip.avaritia.tool.crystal_spear_sevenfold", "Seven In, Seven Out"),
                Map.entry("tooltip.avaritia.crystal_spear_sevenfold.remaining", "Seven In, Seven Out — Thrusts Remaining: %s/14"),
                Map.entry("message.avaritia.crystal_spear.marked", "Marked target: %s"),
                Map.entry("message.avaritia.crystal_spear.target_unavailable", "No safe thrust position is available."),
                Map.entry("message.avaritia.crystal_spear.no_target", "No non-allied target is nearby."),
                Map.entry("message.avaritia.crystal_spear.exhausted", "Seven In, Seven Out is exhausted. Switch modes to reset."),
                Map.entry("config.jade.plugin_avaritia.spear_mark", "Spear Mark"),
                Map.entry("tooltip.avaritia.jade.spear_mark", "Spear mark: %s s remaining"),
                Map.entry("tooltip.avaritia.tool.blaze_spear_blast", "Blast"),
                Map.entry("tooltip.avaritia.blaze_spear_blast.active", "Blast Mode Active"),
                Map.entry("tooltip.avaritia.tool.infinity_shield_normal", "Normal"),
                Map.entry("tooltip.avaritia.tool.infinity_shield_defending", "Defending"),
                Map.entry("tooltip.avaritia.tool.infinity_shield_definite_defending", "Ultimate Defending"),
                Map.entry("tooltip.avaritia.tool.infinity_shield_float", "Floating"),
                Map.entry("item.avaritia.neutron_nautilus_armor", "Neutron Nautilus Armor"),
                Map.entry("item.avaritia.neutron_wolf_armor", "Neutron Wolf Armor"),
                Map.entry("item.avaritia.neutron_harness", "Neutron Harness")
        ));
        assertTranslations("zh_cn", Map.ofEntries(
                Map.entry("item.avaritia.infinity_spear", "星辰贯穿之矛"),
                Map.entry("item.avaritia.blaze_spear", "炽烬之穿骸矛"),
                Map.entry("item.avaritia.crystal_spear", "双锋破界之矛"),
                Map.entry("tooltip.avaritia.infinity_spear.desc", "星辉所向，寰宇尽穿。"),
                Map.entry("tooltip.avaritia.blaze_spear.desc", "刺枯骸躯壳，烬焚尽残形。"),
                Map.entry("tooltip.avaritia.crystal_spear.desc", "寒芒所向，破界无拘；矛锋所贯，众敌难栖。"),
                Map.entry("tooltip.avaritia.tool.lunge", "突进"),
                Map.entry("tooltip.avaritia.tool.infinity_spear_lunge", "突进"),
                Map.entry("tooltip.avaritia.infinity_spear_lunge.active", "突进模式已激活"),
                Map.entry("tooltip.avaritia.tool.infinity_spear_normal", "普通"),
                Map.entry("tooltip.avaritia.tool.infinity_spear_long_range", "远距突刺"),
                Map.entry("tooltip.avaritia.infinity_spear_long_range.desc", "矛锋所指，万里咫尺。"),
                Map.entry("message.avaritia.infinity_spear.marked", "已标记目标：%s"),
                Map.entry("message.avaritia.infinity_spear.target_unavailable", "标记目标暂时不可用"),
                Map.entry("message.avaritia.infinity_spear.no_target", "附近没有可突刺的非友方目标"),
                Map.entry("tooltip.avaritia.tool.crystal_shatter", "晶爆"),
                Map.entry("tooltip.avaritia.crystal_shatter.active", "晶爆已激活"),
                Map.entry("tooltip.avaritia.tool.crystal_spear_normal", "普通"),
                Map.entry("tooltip.avaritia.tool.crystal_spear_sevenfold", "七进七出"),
                Map.entry("tooltip.avaritia.crystal_spear_sevenfold.remaining", "七进七出——剩余标记突刺：%s/14"),
                Map.entry("message.avaritia.crystal_spear.marked", "已标记目标：%s"),
                Map.entry("message.avaritia.crystal_spear.target_unavailable", "没有可安全抵达的突刺位置"),
                Map.entry("message.avaritia.crystal_spear.no_target", "附近没有可突刺的非友方目标"),
                Map.entry("message.avaritia.crystal_spear.exhausted", "七进七出次数已耗尽，切换模式后可重置"),
                Map.entry("config.jade.plugin_avaritia.spear_mark", "长矛标记"),
                Map.entry("tooltip.avaritia.jade.spear_mark", "长矛标记：剩余 %s 秒"),
                Map.entry("tooltip.avaritia.tool.blaze_spear_blast", "炎爆"),
                Map.entry("tooltip.avaritia.blaze_spear_blast.active", "炎爆模式已激活"),
                Map.entry("tooltip.avaritia.tool.infinity_shield_normal", "普通"),
                Map.entry("tooltip.avaritia.tool.infinity_shield_defending", "防御"),
                Map.entry("tooltip.avaritia.tool.infinity_shield_definite_defending", "终极防御"),
                Map.entry("tooltip.avaritia.tool.infinity_shield_float", "漂浮"),
                Map.entry("item.avaritia.neutron_nautilus_armor", "中子鹦鹉螺铠"),
                Map.entry("item.avaritia.neutron_wolf_armor", "中子战狼铠"),
                Map.entry("item.avaritia.neutron_harness", "中子战魂铠")
        ));
    }

    private static void assertTranslations(String locale, Map<String, String> expected) {
        JsonObject language = AvaritiaLanguageProvider.buildLanguage(locale);
        expected.forEach((key, value) -> {
            assertTrue(language.has(key), () -> locale + " 缺少翻译键 " + key);
            assertEquals(value, language.get(key).getAsString(), key);
        });
    }
}
