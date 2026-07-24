package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.common.menu.provider.ChannelMenuProvider;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("超立方体菜单布局")
class TesseractMenuLayoutTest {
    @Test
    @DisplayName("合成模式槽位与旧版贴图纵坐标一致")
    void alignsCraftingSlotsWithLegacyTexture() {
        assertAll(
                () -> assertEquals(136, TesseractMenu.CRAFTING_SLOT_TOP_Y),
                () -> assertEquals(153, TesseractMenu.CRAFTING_SLOT_MIDDLE_Y),
                () -> assertEquals(170, TesseractMenu.CRAFTING_SLOT_BOTTOM_Y),
                () -> assertEquals(17,
                        TesseractMenu.CRAFTING_SLOT_MIDDLE_Y - TesseractMenu.CRAFTING_SLOT_TOP_Y),
                () -> assertEquals(17,
                        TesseractMenu.CRAFTING_SLOT_BOTTOM_Y - TesseractMenu.CRAFTING_SLOT_MIDDLE_Y)
        );
    }

    @Test
    @DisplayName("主界面使用超立方体本地化标题")
    void exposesLocalizedTesseractTitle() {
        assertEquals(Component.translatable("block.avaritia.tesseract"),
                new ChannelMenuProvider(-2).getDisplayName());
    }
}
