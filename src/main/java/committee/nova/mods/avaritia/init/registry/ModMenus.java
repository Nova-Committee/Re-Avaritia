package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.client.screen.CompressorScreen;
import committee.nova.mods.avaritia.client.screen.ExtremeCraftingScreen;
import committee.nova.mods.avaritia.client.screen.NeutronCollectorScreen;
import committee.nova.mods.avaritia.common.menu.CompressorMenu;
import committee.nova.mods.avaritia.common.menu.ExtremeCraftingMenu;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.util.registry.FabricRegistry;
import committee.nova.mods.avaritia.util.registry.RegistryHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;
/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:37
 * Version: 1.0
 */
public class ModMenus {
    public static final RegistryHolder<MenuType<?>> MENUS = FabricRegistry.INSTANCE.createMenusRegistryHolder();
    public static void init() {}

    @Environment(EnvType.CLIENT)
    public static void onClientSetup() {
        MenuScreens.register(extreme_crafting_table.get(), ExtremeCraftingScreen::new);
        MenuScreens.register(neutron_collector.get(), NeutronCollectorScreen::new);
        MenuScreens.register(compressor.get(), CompressorScreen::new);

    }
    public static <T extends AbstractContainerMenu> Supplier<ExtendedScreenHandlerType<T>> menu(String name, Supplier<ExtendedScreenHandlerType<T>> container) {
        return MENUS.register(name, container);
    }

    public static Supplier<ExtendedScreenHandlerType<ExtremeCraftingMenu>> extreme_crafting_table
            = menu("extreme_crafting_table", () -> new ExtendedScreenHandlerType<>(ExtremeCraftingMenu::create));
    public static Supplier<ExtendedScreenHandlerType<NeutronCollectorMenu>> neutron_collector
            = menu("neutron_collector", () -> new ExtendedScreenHandlerType<>(NeutronCollectorMenu::create));
    public static Supplier<ExtendedScreenHandlerType<CompressorMenu>> compressor
            = menu("compressor", () -> new ExtendedScreenHandlerType<>(CompressorMenu::create));


}
