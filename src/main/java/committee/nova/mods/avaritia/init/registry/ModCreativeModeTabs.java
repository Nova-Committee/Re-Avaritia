package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.util.SingularityUtil;
import committee.nova.mods.avaritia.util.registry.FabricRegistry;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModCreativeModeTabs{
    public static final LazyRegistrar<CreativeModeTab> TABS = LazyRegistrar.create(BuiltInRegistries.CREATIVE_MODE_TAB, Static.MOD_ID);
    public static void init() {
        Static.LOGGER.info("Registering Mod Tabs...");
        TABS.register();
    }

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = TABS.register("avaritia_group", () -> FabricRegistry.INSTANCE.createTabBuilder()
            .title(Component.translatable("itemGroup.tab.Infinity"))
            .icon(() -> ModItems.infinity_pickaxe.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var singularity : SingularityRegistryHandler.getInstance().getSingularities()) {
                    if (singularity.isEnabled()) {
                        output.accept(SingularityUtil.getItemForSingularity(singularity));
                    }
                }
                ModItems.ITEMS.getEntries().forEach(itemRegistryObject -> {
                    output.accept(itemRegistryObject.get());
                });
            })
            .build());

}
