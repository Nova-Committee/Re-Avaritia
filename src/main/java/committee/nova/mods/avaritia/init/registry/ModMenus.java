package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import committee.nova.mods.avaritia.api.init.registry.DataMenuType;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;

import java.util.function.Supplier;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 11:37
 * Version: 1.0
 */
public class ModMenus {
    public static final RegistryEntries<MenuType<?>> MENUS = RegistryEntries.create(BuiltInRegistries.MENU, Const.MOD_ID);


    public static <T extends AbstractContainerMenu> RegistryEntry<MenuType<T>> menu(String name, Supplier<? extends MenuType<T>> container) {
        return MENUS.register(name, container);
    }


    public static RegistryEntry<MenuType<TierCraftMenu>> sculk_crafting_tile_table = menu("sculk_crafting_tile_table", () -> DataMenuType.create(TierCraftMenu::sculk));
    public static RegistryEntry<MenuType<TierCraftMenu>> nether_crafting_tile_table = menu("nether_crafting_tile_table", () -> DataMenuType.create(TierCraftMenu::nether));
    public static RegistryEntry<MenuType<TierCraftMenu>> end_crafting_tile_table = menu("end_crafting_tile_table", () -> DataMenuType.create(TierCraftMenu::end));
    public static RegistryEntry<MenuType<TierCraftMenu>> extreme_crafting_table = menu("extreme_crafting_table", () -> DataMenuType.create(TierCraftMenu::extreme));
    public static RegistryEntry<MenuType<NeutronCollectorMenu>> neutron_collector = menu("neutron_collector", () -> DataMenuType.create(NeutronCollectorMenu::new));
    public static RegistryEntry<MenuType<NeutronCompressorMenu>> compressor = menu("compressor", () -> DataMenuType.create(NeutronCompressorMenu::new));
    public static RegistryEntry<MenuType<ExtremeSmithingMenu>> extreme_smithing_table = menu("extreme_smithing_table", () -> DataMenuType.create(ExtremeSmithingMenu::new));
    public static RegistryEntry<MenuType<CompressedChestMenu>> GENERIC_9x27 = menu("generic_9x27", () -> DataMenuType.create(CompressedChestMenu::new));
    public static RegistryEntry<MenuType<ExtremeAnvilMenu>> extreme_anvil = menu("extreme_anvil", () -> DataMenuType.create(ExtremeAnvilMenu::new));
    public static RegistryEntry<MenuType<TesseractChannelMenu>> tesseract_channel = menu("tesseract_channel", () -> DataMenuType.create(TesseractChannelMenu::new));
    public static RegistryEntry<MenuType<TesseractMenu>> tesseract = menu("tesseract", () -> DataMenuType.create(TesseractMenu::new));
    public static RegistryEntry<MenuType<InfinityClockMenu>> infinity_clock_menu =
            menu("infinity_clock_menu", () -> DataMenuType.create((id, inv, buf) -> new InfinityClockMenu(id, inv)));
    public static RegistryEntry<MenuType<InfinityChestMenu>> infinity_chest = menu("infinity_chest", () -> DataMenuType.create(InfinityChestMenu::new));
    public static RegistryEntry<MenuType<InfinityBucketMenu>> infinity_bucket = menu("infinity_bucket", () -> DataMenuType.create((id, inv, buf) -> new InfinityBucketMenu(id, inv)));
}
