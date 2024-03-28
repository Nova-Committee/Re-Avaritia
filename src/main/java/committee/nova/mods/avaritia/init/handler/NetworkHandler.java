package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.init.handler.NetBaseHandler;
import committee.nova.mods.avaritia.common.net.SyncSingularitiesPacket;
import committee.nova.mods.avaritia.common.net.TotemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

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
        event.enqueueWork(() -> INSTANCE.register(SyncSingularitiesPacket.class, new SyncSingularitiesPacket()));
        event.enqueueWork(() -> INSTANCE.register(TotemPacket.class, new TotemPacket()));

    }
}
