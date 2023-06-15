package nova.committee.avaritia.init.registry;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.client.screen.CompressorScreen;
import nova.committee.avaritia.client.screen.ExtremeCraftingScreen;
import nova.committee.avaritia.client.screen.NeutronCollectorScreen;
import nova.committee.avaritia.common.menu.CompressorMenu;
import nova.committee.avaritia.common.menu.ExtremeCraftingMenu;
import nova.committee.avaritia.common.menu.NeutronCollectorMenu;
import nova.committee.avaritia.util.registry.RegistryUtil;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Static.MOD_ID);

    public static MenuType<ExtremeCraftingMenu> extreme_crafting_table = MENUS.register("extreme_crafting_table", () -> RegistryUtil.registerContainer(ExtremeCraftingMenu::create)).get();
    public static MenuType<NeutronCollectorMenu> neutron_collector = MENUS.register("neutron_collector", () -> RegistryUtil.registerContainer(NeutronCollectorMenu::create)).get();
    public static MenuType<CompressorMenu> compressor = MENUS.register("compressor", () -> RegistryUtil.registerContainer(CompressorMenu::create)).get();


    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        MenuScreens.register(extreme_crafting_table, ExtremeCraftingScreen::new);
        MenuScreens.register(neutron_collector, NeutronCollectorScreen::new);
        MenuScreens.register(compressor, CompressorScreen::new);

    }

}
