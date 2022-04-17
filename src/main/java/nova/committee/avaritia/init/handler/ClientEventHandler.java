package nova.committee.avaritia.init.handler;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.init.ModModelCache;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 11:43
 * Version: 1.0
 */
@Mod.EventBusSubscriber(modid = Static.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void modelRegEvent(ModelRegistryEvent event) {
        ModModelCache.instance.setup();
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        ModModelCache.instance.onBake(event);
    }


}
