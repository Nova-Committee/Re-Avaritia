package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Const.MOD_ID);
    public static final List<RegistryObject<Item>> ACCEPT_ITEM = new ArrayList<>();


    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = TABS.register("avaritia_group", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tab.Infinity"))
            .icon(()->ModItems.infinity_catalyst.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var item : ACCEPT_ITEM) {
                    output.accept(item.get());
                }

            })
            .build());
    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB_SINGULARITIES =
            TABS.register("avaritia_singularity_group", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tab.Singularity"))
                    .icon(ModCreativeModeTabs::makeIcon)
                    .displayItems((parameters, output) -> {
                        for (var singularity : SingularityRegistryHandler.getInstance().getSingularities()) {
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
