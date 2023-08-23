package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModCreativeModeTabs {
    private static final List<RegistryObject<Item>> DONT_INCLUDE = List.of();
    public static final ResourceLocation CREATIVE_MODE_TAB_KEY = Static.rl("tab");
    public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = RegistryObject.create(
            CREATIVE_MODE_TAB_KEY, Registries.CREATIVE_MODE_TAB, Static.MOD_ID
    );
    static void register(RegisterEvent.RegisterHelper<CreativeModeTab> helper) {
        helper.register(CREATIVE_MODE_TAB_KEY, CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.tab.Infinity"))
                //.icon(() -> new ItemStack(RegistryItems.FLUX_CORE.get()))
                .displayItems((parameters, output) -> {
                    ModItems.ITEMS.getEntries().forEach(itemRegistryObject -> {
                        if (!DONT_INCLUDE.contains(itemRegistryObject)) {
                            output.accept(itemRegistryObject.get());
                        }
                    });
                })
                .build());
    }

    private ModCreativeModeTabs() {}

}
