package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.net.SyncSingularitiesPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:07
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {


    private static final String PROTOCOL_VERSION = Integer.toString(1);

    @SubscribeEvent
    public static void registerHandler(RegisterPayloadHandlerEvent event) {
        registerPackets(event.registrar(Static.MOD_ID).versioned(PROTOCOL_VERSION));
    }

    public static void registerPackets(IPayloadRegistrar registrar) {
        registrar.common(SyncSingularitiesPacket.ID, buf -> new SyncSingularitiesPacket(SingularityRegistryHandler.getInstance().readFromBuffer(buf)), handler -> handler.server(SyncSingularitiesPacket::run));

    }

}
