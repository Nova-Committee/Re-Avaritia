package nova.committee.avaritia.init.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.api.init.handler.NetBaseHandler;
import nova.committee.avaritia.common.net.SyncSingularitiesPacket;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:07
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    public static final NetBaseHandler INSTANCE = new NetBaseHandler(new ResourceLocation(Static.MOD_ID, "main"));

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        INSTANCE.register(SyncSingularitiesPacket.class, new SyncSingularitiesPacket());
    }
}
