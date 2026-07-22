package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.*;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
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
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Const.MOD_ID);

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
    public static DeferredHolder<MenuType<?>, MenuType<NeutronCompressorMenu>> neutron_compressor = menu("neutron_compressor",
            () -> new MenuType<>((IContainerFactory<NeutronCompressorMenu>) NeutronCompressorMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<ExtremeSmithingMenu>> extreme_smithing_table = menu("extreme_smithing_table",
            () -> new MenuType<>((IContainerFactory<ExtremeSmithingMenu>)ExtremeSmithingMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<InfinityChestMenu>> infinity_chest = menu("infinity_chest",
            () -> new MenuType<>((IContainerFactory<InfinityChestMenu>) InfinityChestMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<TesseractMenu>> tesseract = menu("tesseract",
            () -> new MenuType<>((IContainerFactory<TesseractMenu>) TesseractMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<TesseractChannelMenu>> tesseract_channel = menu("tesseract_channel",
            () -> new MenuType<>((IContainerFactory<TesseractChannelMenu>) TesseractChannelMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<InfinityClockMenu>> infinity_clock_menu = menu("infinity_clock_menu",
            () -> new MenuType<>((IContainerFactory<InfinityClockMenu>)(id, inv, buf) -> new InfinityClockMenu(id, inv), FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<CompressedChestMenu>> GENERIC_9x27 = menu("generic_9x27",
            () -> new MenuType<>((IContainerFactory<CompressedChestMenu>)CompressedChestMenu::new, FeatureFlagSet.of()));
    public static DeferredHolder<MenuType<?>, MenuType<ExtremeAnvilMenu>> extreme_anvil = menu("extreme_anvil",
            () -> new MenuType<>((IContainerFactory<ExtremeAnvilMenu>)ExtremeAnvilMenu::new, FeatureFlagSet.of()));
}
