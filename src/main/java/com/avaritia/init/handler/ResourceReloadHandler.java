package com.avaritia.init.handler;

import com.avaritia.Const;
import com.avaritia.core.singularity.SingularityReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import static com.avaritia.core.singularity.SingularityReloadListener.RELOAD_LISTENER_ID;

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

    }
}
