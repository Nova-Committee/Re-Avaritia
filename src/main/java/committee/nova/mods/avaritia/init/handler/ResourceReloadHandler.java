package committee.nova.mods.avaritia.init.handler;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
@Mod.EventBusSubscriber
public class ResourceReloadHandler {
    @SubscribeEvent//指定不了加载顺序
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
//        SingularityReloadListener.INSTANCE = new SingularityReloadListener(event.getConditionContext());
//        event.addListener(SingularityReloadListener.INSTANCE);
    }
}
