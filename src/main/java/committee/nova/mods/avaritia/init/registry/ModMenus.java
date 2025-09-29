package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.screen.*;
import committee.nova.mods.avaritia.client.screen.craft.EndCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.ExtremeCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.NetherCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.SculkCraftScreen;
import committee.nova.mods.avaritia.common.menu.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
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
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Const.MOD_ID);

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup(RegisterMenuScreensEvent event) {
        event.register(sculk_crafting_tile_table.get(), SculkCraftScreen::new);
        event.register(nether_crafting_tile_table.get(), NetherCraftScreen::new);
        event.register(end_crafting_tile_table.get(), EndCraftScreen::new);
        event.register(extreme_crafting_table.get(), ExtremeCraftScreen::new);
        event.register(neutron_collector.get(), NeutronCollectorScreen::new);
        event.register(compressor.get(), CompressorScreen::new);
        event.register(GENERIC_9x27.get(), CompressedChestScreen::new);
        event.register(neutron_ring.get(), NeutronRingScreen::new);
        event.register(infinity_chest.get(), InfinityChestScreen::new);
        event.register(extreme_smithing_table.get(), ExtremeSmithingScreen::new);
        event.register(extreme_anvil.get(), ExtremeAnvilScreen::new);
    }

    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> menu(String name, Supplier<? extends MenuType<T>> container) {
        return MENUS.register(name, container);
    }

    public static DeferredHolder<MenuType<?>, MenuType<NeutronRingMenu>> neutron_ring = menu("neutron_ring",
            () -> new MenuType<>((IContainerFactory<NeutronRingMenu>)NeutronRingMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<TierCraftMenu>> sculk_crafting_tile_table = menu("sculk_crafting_tile_table",
            () -> new MenuType<>((IContainerFactory<TierCraftMenu>)TierCraftMenu::sculk, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<TierCraftMenu>> nether_crafting_tile_table = menu("nether_crafting_tile_table",
            () -> new MenuType<>((IContainerFactory<TierCraftMenu>)TierCraftMenu::nether, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<TierCraftMenu>> end_crafting_tile_table = menu("end_crafting_tile_table",
            () -> new MenuType<>((IContainerFactory<TierCraftMenu>)TierCraftMenu::end, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<TierCraftMenu>> extreme_crafting_table = menu("extreme_crafting_table",
            () -> new MenuType<>((IContainerFactory<TierCraftMenu>)TierCraftMenu::extreme, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<NeutronCollectorMenu>> neutron_collector = menu("neutron_collector",
            () -> new MenuType<>((IContainerFactory<NeutronCollectorMenu>)NeutronCollectorMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<CompressorMenu>> compressor = menu("compressor",
            () -> new MenuType<>((IContainerFactory<CompressorMenu>)CompressorMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<ExtremeSmithingMenu>> extreme_smithing_table = menu("extreme_smithing_table",
            () -> new MenuType<>((IContainerFactory<ExtremeSmithingMenu>)ExtremeSmithingMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<InfinityChestMenu>> infinity_chest = menu("infinity_chest",
            () -> new MenuType<>((IContainerFactory<InfinityChestMenu>)InfinityChestMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<CompressedChestMenu>> GENERIC_9x27 = menu("generic_9x27",
            () -> new MenuType<>((IContainerFactory<CompressedChestMenu>)CompressedChestMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<ExtremeAnvilMenu>> extreme_anvil = menu("extreme_anvil",
            () -> new MenuType<>((IContainerFactory<ExtremeAnvilMenu>)ExtremeAnvilMenu::new, FeatureFlagSet.of()));
}
