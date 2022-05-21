package nova.committee.avaritia.init.registry;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.client.screen.CompressorScreen;
import nova.committee.avaritia.client.screen.ExtremeCraftingScreen;
import nova.committee.avaritia.client.screen.NeutronCollectorScreen;
import nova.committee.avaritia.common.menu.CompressorMenu;
import nova.committee.avaritia.common.menu.ExtremeCraftingMenu;
import nova.committee.avaritia.common.menu.NeutronCollectorMenu;
import nova.committee.avaritia.util.RegistryUtil;

@Mod.EventBusSubscriber(modid = Static.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMenus {

    public static MenuType<ExtremeCraftingMenu> extreme_crafting_table;
    public static MenuType<NeutronCollectorMenu> neutron_collector;
    public static MenuType<CompressorMenu> compressor;

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
        final IForgeRegistry<MenuType<?>> registry = event.getRegistry();

        registry.registerAll(
                extreme_crafting_table = RegistryUtil.registerContainer("extreme_crafting_table", ExtremeCraftingMenu::create),
                neutron_collector = RegistryUtil.registerContainer("neutron_collector", NeutronCollectorMenu::create),
                compressor = RegistryUtil.registerContainer("compressor", CompressorMenu::create)

        );
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        MenuScreens.register(extreme_crafting_table, ExtremeCraftingScreen::new);
        MenuScreens.register(neutron_collector, NeutronCollectorScreen::new);
        MenuScreens.register(compressor, CompressorScreen::new);

    }

}
