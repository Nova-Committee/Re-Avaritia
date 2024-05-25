package committee.nova.mods.avaritia.init.compact.rei;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.menu.ExtremeCraftingMenu;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.init.compact.rei.display.ExtremeTableDisplay;
import committee.nova.mods.avaritia.init.compact.rei.display.CompressorDisplay;
import committee.nova.mods.avaritia.init.compact.rei.menu.ExtremeTableMenuInfo;
import committee.nova.mods.avaritia.init.compact.rei.menu.CompressorMenu;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;

public class ServerREIPlugin implements REIServerPlugin {

    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        registry.register(EXTREME_TABLE, ExtremeCraftingMenu.class, SimpleMenuInfoProvider.of(ExtremeTableMenuInfo::new));
        registry.register(COMPRESSOR, NeutronCollectorMenu.class, SimpleMenuInfoProvider.of(CompressorMenu::new));

    }

    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(EXTREME_TABLE, ExtremeTableDisplay.serializer());
        registry.register(COMPRESSOR, CompressorDisplay.Serializer.ofRecipeLess(CompressorDisplay::new));
    }
    public static final CategoryIdentifier<ExtremeTableDisplay<?>> EXTREME_TABLE = CategoryIdentifier.of(Static.MOD_ID, "extreme_display");
    public static final CategoryIdentifier<CompressorDisplay> COMPRESSOR = CategoryIdentifier.of(Static.MOD_ID, "neutron_compressor");

}
