package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.util.SingularityUtil;
import committee.nova.mods.avaritia.util.registry.FabricRegistry;
import committee.nova.mods.avaritia.util.registry.RegistryHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModCreativeModeTabs{
    public static void init() {}
    public static final RegistryHolder<CreativeModeTab> TABS = FabricRegistry.INSTANCE.createCreativeTabRegistryHolder();
    private static final List<Supplier<Item>> DONT_INCLUDE = List.of();

    public static final Supplier<CreativeModeTab> CREATIVE_TAB = TABS.register("avaritia_group", () -> FabricRegistry.INSTANCE.createTabBuilder()
            .title(Component.translatable("itemGroup.tab.Infinity"))
            .icon(() -> ModItems.infinity_pickaxe.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var singularity : SingularityRegistryHandler.getInstance().getSingularities()) {
                    if (singularity.isEnabled()) {
                        output.accept(SingularityUtil.getItemForSingularity(singularity));
                    }
                }
                ModItems.ITEMS.listAll().forEach(itemSupplier -> {
                    if (!DONT_INCLUDE.contains(itemSupplier)) {
                        output.accept(itemSupplier.get());
                    }
                });
            })
            .build());

}
