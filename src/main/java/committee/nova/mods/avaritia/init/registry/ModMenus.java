package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.screen.CompressorScreen;
import committee.nova.mods.avaritia.client.screen.ExtremeCraftingScreen;
import committee.nova.mods.avaritia.client.screen.NeutronCollectorScreen;
import committee.nova.mods.avaritia.common.menu.CompressorMenu;
import committee.nova.mods.avaritia.common.menu.ExtremeCraftingMenu;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:37
 * Version: 1.0
 */
public class ModMenus {
    public static final LazyRegistrar<MenuType<?>> MENUS = LazyRegistrar.create(BuiltInRegistries.MENU, Static.MOD_ID);

    public static void init() {
        Static.LOGGER.info("Registering Mod Menus...");
        MENUS.register();
    }

    public static RegistryObject<MenuType<ExtremeCraftingMenu>> extreme_crafting_table
            = menu("extreme_crafting_table", ExtremeCraftingMenu::new);
    public static RegistryObject<MenuType<NeutronCollectorMenu>> neutron_collector
            = menu("neutron_collector", NeutronCollectorMenu::new);
    public static RegistryObject<MenuType<CompressorMenu>> compressor
            = menu("compressor", CompressorMenu::new);


    public static <C extends AbstractContainerMenu> RegistryObject<MenuType<C>> menu(String name, ExtendedScreenHandlerType.ExtendedFactory<C> factory) {
        return MENUS.register(name, () -> new ExtendedScreenHandlerType<>(factory));
    }


    @Environment(EnvType.CLIENT)
    public static void onClientSetup() {
        MenuScreens.register(extreme_crafting_table.get(), ExtremeCraftingScreen::new);
        MenuScreens.register(neutron_collector.get(), NeutronCollectorScreen::new);
        MenuScreens.register(compressor.get(), CompressorScreen::new);
    }
}
