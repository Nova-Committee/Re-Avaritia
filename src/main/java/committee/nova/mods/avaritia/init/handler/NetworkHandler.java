package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.net.SyncSingularitiesPacket;
import committee.nova.mods.avaritia.common.net.TotemPacket;
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
        CHANNEL.registerMessage(id++, SyncSingularitiesPacket.class, SyncSingularitiesPacket::write, SyncSingularitiesPacket::new, SyncSingularitiesPacket::run);
        CHANNEL.registerMessage(id++, TotemPacket.class, TotemPacket::write, TotemPacket::new, TotemPacket::run);
    }
}
