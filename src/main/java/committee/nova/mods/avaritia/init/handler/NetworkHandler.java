package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.net.C2SJEIGhostPacket;
import committee.nova.mods.avaritia.common.net.S2CSingularitiesPacket;
import committee.nova.mods.avaritia.common.net.S2CTotemPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:07
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    public static int id = 0;
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Static.rl("main"), () -> {
        return "1.0";
    }, (s) -> {
        return true;
    }, (s) -> {
        return true;
    });;

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(id++, S2CSingularitiesPacket.class, S2CSingularitiesPacket::write, S2CSingularitiesPacket::new, S2CSingularitiesPacket::run);
        CHANNEL.registerMessage(id++, S2CTotemPacket.class, S2CTotemPacket::write, S2CTotemPacket::new, S2CTotemPacket::run);
        CHANNEL.registerMessage(id++, C2SJEIGhostPacket.class, C2SJEIGhostPacket::write, C2SJEIGhostPacket::new, C2SJEIGhostPacket::run);
    }
}
