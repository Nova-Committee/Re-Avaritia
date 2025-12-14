package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Const.MOD_ID);
    public static final List<DeferredItem<?>> ACCEPT_ITEM = new ArrayList<>();
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = TABS.register("avaritia_group", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tab.Infinity"))
            .icon(() -> ModItems.infinity_catalyst.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var item : ACCEPT_ITEM){
                    output.accept(item.get());
                }

            })
            .build());
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SINGULARITY_CREATIVE_TAB = TABS.register("avaritia_singularity_group", () ->
            new CyclingTab(
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.tab.Singularity"))
                            .displayItems((parameters, output) -> {
                                for (var singularity : SingularityReloadListener.INSTANCE.getAllSingularities().values()) {
                                    if (singularity.isEnabled()) {
                                        output.accept(SingularityUtils.getItemForSingularity(singularity));
                                    }
                                }
                            })
            )
    );
    private static class CyclingTab extends CreativeModeTab {

        public CyclingTab(CreativeModeTab.Builder builder) {
            super(builder);
        }

        @Override
        public @NotNull ItemStack getIconItem() {
            var enabledSingularities = SingularityReloadListener.INSTANCE.getAllSingularities().values()
                    .stream()
                    .filter(Singularity::isEnabled)
                    .toList();

            if (enabledSingularities.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int idx = (int) (System.currentTimeMillis() / 1200) % enabledSingularities.size();
            Singularity currentSingularity = enabledSingularities.get(idx);

            return SingularityUtils.getItemForSingularity(currentSingularity);
        }
    }
}
