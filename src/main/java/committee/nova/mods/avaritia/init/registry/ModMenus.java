package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.screen.CompressedChestScreen;
import committee.nova.mods.avaritia.client.screen.CompressorScreen;
import committee.nova.mods.avaritia.client.screen.ExtremeCraftingScreen;
import committee.nova.mods.avaritia.client.screen.NeutronCollectorScreen;
import committee.nova.mods.avaritia.common.block.chest.CompressedChestBlock;
import committee.nova.mods.avaritia.common.menu.CompressorMenu;
import committee.nova.mods.avaritia.common.menu.ExtremeCraftingMenu;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
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
        MenuScreens.register(GENERIC_9x9.get(), CompressedChestScreen::new);
    }
    public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> menu(String name, Supplier<? extends MenuType<T>> container) {
        return MENUS.register(name, container);
    }

    public static RegistryObject<MenuType<ExtremeCraftingMenu>> extreme_crafting_table = menu("extreme_crafting_table", () -> new MenuType<>((IContainerFactory<ExtremeCraftingMenu>) ExtremeCraftingMenu::create, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<NeutronCollectorMenu>> neutron_collector = menu("neutron_collector", () -> new MenuType<>((IContainerFactory<NeutronCollectorMenu>) NeutronCollectorMenu::create, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<CompressorMenu>> compressor = menu("compressor", () -> new MenuType<>((IContainerFactory<CompressorMenu>) CompressorMenu::create, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<ChestMenu>> GENERIC_9x9 = menu("generic_9x9",
            () -> new MenuType<>((IContainerFactory<ChestMenu>)
                    (windowId, playerInventory, buffer) -> new ChestMenu(ModMenus.GENERIC_9x9.get(), windowId, playerInventory, new SimpleContainer(9*9), 9),
                    FeatureFlagSet.of()));


}
