package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.data.listener.SingularityJsonReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
@Mod.EventBusSubscriber
public class ResourceReloadHandler {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SingularityJsonReloadListener(event.getConditionContext()));
        Const.LOGGER.debug("Singularity reload listener registered");
    }
}
