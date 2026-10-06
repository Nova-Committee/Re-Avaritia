package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import static committee.nova.mods.avaritia.core.singularity.SingularityReloadListener.RELOAD_LISTENER_ID;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class ResourceReloadHandler {
    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        /**
         * NeoForge 26.1.2 禁止通过 mixin 修改 ReloadableServerResources#listeners，必须走此事件注册服务端重载监听器。
         */
        event.addListener(RELOAD_LISTENER_ID, SingularityReloadListener.INSTANCE);
        // 26.3 起配方不再是重载监听器（VanillaServerListeners.RECIPES 已移除），而是随数据包注册表加载；
        // RegisterRecipesEvent 改由 RecipeManagerMixin 在 RecipeManager#finalizeRecipeLoading 时触发，
        // 该方法在所有重载监听器应用完成后才执行，因此数据快照天然先于配方重建，无需再声明依赖顺序。

    }
}
