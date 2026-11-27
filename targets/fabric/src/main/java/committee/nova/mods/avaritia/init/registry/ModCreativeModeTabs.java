package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModCreativeModeTabs {

    public static final RegistryEntries<CreativeModeTab> TABS = RegistryEntries.create(BuiltInRegistries.CREATIVE_MODE_TAB, Const.MOD_ID);
    public static final List<RegistryEntry<Item>> ACCEPT_ITEM = new ArrayList<>();


    public static final RegistryEntry<CreativeModeTab> CREATIVE_TAB = TABS.register("avaritia_group", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
            .title(Component.translatable("itemGroup.tab.Infinity"))
            .icon(() -> ModItems.infinity_catalyst.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var item : ACCEPT_ITEM) {
                    output.accept(item.get());
                }

            })
            .build());
    public static final RegistryEntry<CreativeModeTab> CREATIVE_TAB_SINGULARITIES =
            TABS.register("avaritia_singularity_group", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 8)
                    .title(Component.translatable("itemGroup.tab.Singularity"))
                    .icon(ModCreativeModeTabs::makeIcon)
                    .displayItems((parameters, output) -> {
                        for (var singularity : SingularityReloadListener.INSTANCE.getAllSingularities().values()) {
                            if (singularity.isEnabled()) {
                                output.accept(SingularityUtils.getItemForSingularity(singularity));
                            }
                        }
                    })
                    .build());

    private static ItemStack makeIcon() {
        ItemStack stack = new ItemStack(ModItems.singularity.get());
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("IsCreativeTab", true);
        stack.setTag(tag);
        return stack;
    }
}
