package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.screen.*;
import committee.nova.mods.avaritia.common.block.craft.ModCraftingTier;
import committee.nova.mods.avaritia.common.menu.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.IContainerFactory;
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
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Static.MOD_ID);

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        MenuScreens.register(extreme_crafting_table.get(), ExtremeCraftingScreen::new);
        MenuScreens.register(neutron_collector.get(), NeutronCollectorScreen::new);
        MenuScreens.register(compressor.get(), CompressorScreen::new);
        MenuScreens.register(GENERIC_9x27.get(), CompressedChestScreen::new);
        MenuScreens.register(neutron_ring.get(), NeutronRingScreen::new);
    }
    public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> menu(String name, Supplier<? extends MenuType<T>> container) {
        return MENUS.register(name, container);
    }

    public static RegistryObject<MenuType<NeutronRingMenu>> neutron_ring = menu("neutron_ring", () -> new MenuType<>((IContainerFactory<NeutronRingMenu>) NeutronRingMenu::create, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<ModCraftingMenu>> sculk_crafting_tile_table = menu("sculk_crafting_tile_table", () -> new MenuType<>((IContainerFactory<ModCraftingMenu>) (id, inventory, byteBuf) -> ModCraftingMenu.create(id, inventory, byteBuf, ModCraftingTier.SCULK.size), FeatureFlagSet.of()));
    public static RegistryObject<MenuType<ModCraftingMenu>> nether_crafting_tile_table = menu("nether_crafting_tile_table", () -> new MenuType<>((IContainerFactory<ModCraftingMenu>) (id, inventory, byteBuf) -> ModCraftingMenu.create(id, inventory, byteBuf, ModCraftingTier.NETHER.size), FeatureFlagSet.of()));
    public static RegistryObject<MenuType<ModCraftingMenu>> end_crafting_tile_table = menu("end_crafting_tile_table", () -> new MenuType<>((IContainerFactory<ModCraftingMenu>) (id, inventory, byteBuf) -> ModCraftingMenu.create(id, inventory, byteBuf, ModCraftingTier.END.size), FeatureFlagSet.of()));
    public static RegistryObject<MenuType<ModCraftingMenu>> extreme_crafting_table = menu("extreme_crafting_table", () -> new MenuType<>((IContainerFactory<ModCraftingMenu>) (id, inventory, byteBuf) -> ModCraftingMenu.create(id, inventory, byteBuf, ModCraftingTier.EXTREME.size), FeatureFlagSet.of()));
    public static RegistryObject<MenuType<NeutronCollectorMenu>> neutron_collector = menu("neutron_collector", () -> new MenuType<>((IContainerFactory<NeutronCollectorMenu>) NeutronCollectorMenu::create, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<CompressorMenu>> compressor = menu("compressor", () -> new MenuType<>((IContainerFactory<CompressorMenu>) CompressorMenu::create, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<CompressedChestMenu>> GENERIC_9x27 = menu("generic_9x27",
            () -> new MenuType<>((IContainerFactory<CompressedChestMenu>)
                    (windowId, playerInventory, buffer) -> new CompressedChestMenu(ModMenus.GENERIC_9x27.get(), windowId, playerInventory, new SimpleContainer(9*27), 9),
                    FeatureFlagSet.of()));


}
