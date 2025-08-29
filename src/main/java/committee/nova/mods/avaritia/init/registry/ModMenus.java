package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.screen.BlackHoleChestScreen;
import committee.nova.mods.avaritia.common.menu.ChannelMenu;
import committee.nova.mods.avaritia.common.menu.ChannelSelectMenu;
import committee.nova.mods.avaritia.client.screen.ChannelSelectScreen;
import committee.nova.mods.avaritia.client.screen.*;
import committee.nova.mods.avaritia.client.screen.craft.EndCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.ExtremeCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.NetherCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.SculkCraftScreen;
import committee.nova.mods.avaritia.common.menu.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:37
 * Version: 1.0
 */
public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Const.MOD_ID);

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        MenuScreens.register(sculk_crafting_tile_table.get(), SculkCraftScreen::new);
        MenuScreens.register(nether_crafting_tile_table.get(), NetherCraftScreen::new);
        MenuScreens.register(end_crafting_tile_table.get(), EndCraftScreen::new);
        MenuScreens.register(extreme_crafting_table.get(), ExtremeCraftScreen::new);
        MenuScreens.register(neutron_collector.get(), NeutronCollectorScreen::new);
        MenuScreens.register(compressor.get(), CompressorScreen::new);
        MenuScreens.register(GENERIC_9x27.get(), CompressedChestScreen::new);
        MenuScreens.register(neutron_ring.get(), NeutronRingScreen::new);
        MenuScreens.register(infinity_chest.get(), InfinityChestScreen::new);
        MenuScreens.register(channel_menu.get(), BlackHoleChestScreen::new);
        MenuScreens.register(channel_select_menu.get(), ChannelSelectScreen::new);
        MenuScreens.register(extreme_smithing_table.get(), ExtremeSmithingScreen::new);
        MenuScreens.register(extreme_anvil.get(), ExtremeAnvilScreen::new);
    }

    public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> menu(String name, Supplier<? extends MenuType<T>> container) {
        return MENUS.register(name, container);
    }

    public static RegistryObject<MenuType<NeutronRingMenu>> neutron_ring = menu("neutron_ring", () -> IForgeMenuType.create(NeutronRingMenu::new));
    public static RegistryObject<MenuType<TierCraftMenu>> sculk_crafting_tile_table = menu("sculk_crafting_tile_table", () -> IForgeMenuType.create(TierCraftMenu::sculk));
    public static RegistryObject<MenuType<TierCraftMenu>> nether_crafting_tile_table = menu("nether_crafting_tile_table", () -> IForgeMenuType.create(TierCraftMenu::nether));
    public static RegistryObject<MenuType<TierCraftMenu>> end_crafting_tile_table = menu("end_crafting_tile_table", () -> IForgeMenuType.create(TierCraftMenu::end));
    public static RegistryObject<MenuType<TierCraftMenu>> extreme_crafting_table = menu("extreme_crafting_table", () -> IForgeMenuType.create(TierCraftMenu::extreme));
    public static RegistryObject<MenuType<NeutronCollectorMenu>> neutron_collector = menu("neutron_collector", () -> IForgeMenuType.create(NeutronCollectorMenu::new));
    public static RegistryObject<MenuType<CompressorMenu>> compressor = menu("compressor", () -> IForgeMenuType.create(CompressorMenu::new));
    public static RegistryObject<MenuType<ExtremeSmithingMenu>> extreme_smithing_table = menu("extreme_smithing_table", () -> IForgeMenuType.create(ExtremeSmithingMenu::new));
    public static RegistryObject<MenuType<InfinityChestMenu>> infinity_chest = menu("infinity_chest", () -> IForgeMenuType.create(InfinityChestMenu::new));
    public static RegistryObject<MenuType<CompressedChestMenu>> GENERIC_9x27 = menu("generic_9x27", () -> IForgeMenuType.create(CompressedChestMenu::new));
    public static RegistryObject<MenuType<ExtremeAnvilMenu>> extreme_anvil = menu("extreme_anvil", () -> IForgeMenuType.create(ExtremeAnvilMenu::new));
    public static RegistryObject<MenuType<ChannelSelectMenu>> channel_select_menu = menu("channel_select_menu", () -> IForgeMenuType.create(ChannelSelectMenu::new));
    public static RegistryObject<MenuType<ChannelMenu>> channel_menu = menu("channel", () -> IForgeMenuType.create(ChannelMenu::new));

}
