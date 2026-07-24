package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.resource.VanillaServerListeners;

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
        // 数据快照必须先应用，RecipeManager 的 apply 才能原子提交脚本操作并生成派生配方。
        event.addDependency(RELOAD_LISTENER_ID, VanillaServerListeners.RECIPES);

    }
}
