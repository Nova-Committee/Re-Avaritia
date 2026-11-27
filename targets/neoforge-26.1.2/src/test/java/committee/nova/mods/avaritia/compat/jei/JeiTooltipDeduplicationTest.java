package committee.nova.mods.avaritia.compat.jei;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JeiTooltipDeduplicationTest {
    private static final Path MIXIN_CONFIG = Path.of("src/main/resources/avaritia.mixins.json");
    private static final Path MIXIN_PLUGIN_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/mixin/AvaritiaMixinPlugin.java");
    private static final Path JEI_MIXIN_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/mixin/compat/JeiModIdHelperMixin.java");
    private static final Path DEDUPLICATION_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/compat/jei/JeiTooltipDeduplication.java");

    @Test
    void jeiTooltipMixinIsLoadedOnlyWhenJeiIsPresent() throws IOException {
        String mixinConfig = compact(Files.readString(MIXIN_CONFIG));
        String pluginSource = compact(Files.readString(MIXIN_PLUGIN_SOURCE));

        assertTrue(mixinConfig.contains("\"plugin\":\"committee.nova.mods.avaritia.mixin.AvaritiaMixinPlugin\""));
        assertTrue(mixinConfig.contains("\"compat.JeiModIdHelperMixin\""));
        assertTrue(pluginSource.contains("JEI_MOD_ID_HELPER_MIXIN.equals(mixinClassName)"));
        assertTrue(pluginSource.contains("FMLLoader.getCurrentOrNull()"));
        assertTrue(pluginSource.contains("loader.getLoadingModList().getModFileById(modId)!=null"));
        assertTrue(pluginSource.contains("catch(IllegalStateExceptionignored)"));
    }

    @Test
    void deduplicationOnlySkipsJeiItemStackModNameWhenJadeAddsIt() throws IOException {
        String mixinSource = compact(Files.readString(JEI_MIXIN_SOURCE));
        String deduplicationSource = compact(Files.readString(DEDUPLICATION_SOURCE));

        assertTrue(mixinSource.contains("method=\"getModNameForTooltip\""));
        assertTrue(mixinSource.contains("JeiTooltipDeduplication.shouldSuppressItemModNameTooltip(typedIngredient)"));
        assertTrue(deduplicationSource.contains("typedIngredient.getType()!=VanillaTypes.ITEM_STACK"));
        assertTrue(deduplicationSource.contains("showItemModNameTooltip"));
        assertTrue(deduplicationSource.contains("IWAILA_CONFIG_CLASS"));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
