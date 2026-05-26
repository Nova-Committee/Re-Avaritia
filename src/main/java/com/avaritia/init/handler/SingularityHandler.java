package com.avaritia.init.handler;

import com.avaritia.Const;
import com.avaritia.core.singularity.SingularityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * @author cnlimiter
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class SingularityHandler {

    @SubscribeEvent
    public static void onReloadSingularity(SingularityEvent.Reload event) {

    }
}
