package com.avaritia.init.handler;

import com.avaritia.Const;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class ResourceReloadHandler {
    @SubscribeEvent
    public static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        // 在此注册客户端重载监听器
    }
}
