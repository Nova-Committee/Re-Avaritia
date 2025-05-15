package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.addons._channel.BlackHoleScreen;
import committee.nova.mods.avaritia.addons._channel.ChannelMenu;
import committee.nova.mods.avaritia.addons._channel.ChannelSelectMenu;
import committee.nova.mods.avaritia.addons._channel.ChannelSelectScreen;
import committee.nova.mods.avaritia.client.screen.*;
import committee.nova.mods.avaritia.client.screen.craft.EndCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.ExtremeCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.NetherCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.SculkCraftScreen;
import committee.nova.mods.avaritia.common.menu.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:37
 * Version: 1.0
 */
public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Static.MOD_ID);

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
        MenuScreens.register(channel_menu.get(), BlackHoleScreen::new);
        MenuScreens.register(channel_select_menu.get(), ChannelSelectScreen::new);
        MenuScreens.register(extreme_smithing_table.get(), ExtremeSmithingScreen::new);
        MenuScreens.register(extreme_anvil.get(), ExtremeAnvilScreen::new);
        MenuScreens.register(infinity_clock_block.get(), InfinityClockBlockScreen::new);
    }

    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> menu(String name, Supplier<? extends MenuType<T>> container) {
        return MENUS.register(name, container);
    }

    public static DeferredHolder<MenuType<?>, MenuType<NeutronRingMenu>> neutron_ring = menu("neutron_ring", () -> IContainerFactory.create(NeutronRingMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<TierCraftMenu>> sculk_crafting_tile_table = menu("sculk_crafting_tile_table", () -> IForgeMenuType.create(TierCraftMenu::sculk));
    public static DeferredHolder<MenuType<?>, MenuType<TierCraftMenu>> nether_crafting_tile_table = menu("nether_crafting_tile_table", () -> IForgeMenuType.create(TierCraftMenu::nether));
    public static DeferredHolder<MenuType<?>, MenuType<TierCraftMenu>> end_crafting_tile_table = menu("end_crafting_tile_table", () -> IForgeMenuType.create(TierCraftMenu::end));
    public static DeferredHolder<MenuType<?>, MenuType<TierCraftMenu>> extreme_crafting_table = menu("extreme_crafting_table", () -> IForgeMenuType.create(TierCraftMenu::extreme));
    public static DeferredHolder<MenuType<?>, MenuType<NeutronCollectorMenu>> neutron_collector = menu("neutron_collector", () -> IForgeMenuType.create(NeutronCollectorMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<CompressorMenu>> compressor = menu("compressor", () -> IForgeMenuType.create(CompressorMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<ExtremeSmithingMenu>> extreme_smithing_table = menu("extreme_smithing_table", () -> IForgeMenuType.create(ExtremeSmithingMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<InfinityChestMenu>> infinity_chest = menu("infinity_chest", () -> IForgeMenuType.create(InfinityChestMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<CompressedChestMenu>> GENERIC_9x27 = menu("generic_9x27", () -> IForgeMenuType.create(CompressedChestMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<ExtremeAnvilMenu>> extreme_anvil = menu("extreme_anvil", () -> IForgeMenuType.create(ExtremeAnvilMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<InfinityClockBlockMenu>> infinity_clock_block = menu("infinity_clock_block", () -> IForgeMenuType.create(InfinityClockBlockMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<ChannelSelectMenu>> channel_select_menu = menu("channel_select_menu", () -> IForgeMenuType.create(ChannelSelectMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<ChannelMenu>> channel_menu = menu("channel", () -> IForgeMenuType.create(ChannelMenu::new));

}
