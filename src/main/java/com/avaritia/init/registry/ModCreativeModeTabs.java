package com.avaritia.init.registry;

import com.avaritia.Avaritia;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册模组中的所有创造模式物品栏标签页。
 *
 * <p>维护一个 {@link #ACCEPT_ITEM} 列表，供 {@link ModItems} 在注册物品时将物品加入主标签页。</p>
 */
public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Avaritia.MOD_ID);

    /** 所有应出现在主标签页中的物品。由 {@link ModItems} 在注册时填充。 */
    public static final List<DeferredItem<?>> ACCEPT_ITEM = new ArrayList<>();

    /** 模组主标签页，包含所有普通物品与方块。 */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = TABS.register("avaritia_group", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tab.Infinity"))
            .icon(() -> ModItems.infinity_catalyst.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var item : ACCEPT_ITEM) {
                    output.accept(item.get());
                }
            })
            .build());

    /** 奇点标签页，循环显示所有已启用的奇点作为图标，并在列表中展示全部奇点。 */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SINGULARITY_CREATIVE_TAB = TABS.register("avaritia_singularity_group", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tab.Singularity"))
            .icon(() -> {
                var enabledSingularities = SingularityReloadListener.INSTANCE.getAllSingularities().values()
                        .stream()
                        .filter(Singularity::isEnabled)
                        .toList();

                if (enabledSingularities.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                int idx = (int) (System.currentTimeMillis() / 1200) % enabledSingularities.size();
                var currentSingularity = enabledSingularities.get(idx);
                return SingularityUtils.getItemForSingularity(currentSingularity);
            })
            .displayItems((parameters, output) -> {
                for (var singularity : SingularityReloadListener.INSTANCE.getAllSingularities().values()) {
                    if (singularity.isEnabled()) {
                        output.accept(SingularityUtils.getItemForSingularity(singularity));
                    }
                }
            })
            .build());
}
