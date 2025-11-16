package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:07
 * Version: 1.0
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class NetworkHandler {
    @SubscribeEvent
    public static void init(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.1");

        registrar.playToClient(S2CSingularitiesPack.TYPE, S2CSingularitiesPack.STREAM_CODEC,
                new S2CSingularitiesPack.Handler());
        registrar.playToClient(S2CTotemPack.TYPE, S2CTotemPack.STREAM_CODEC,
                new S2CTotemPack.Handler());



        registrar.playToServer(C2SRenamePack.TYPE, C2SRenamePack.STREAM_CODEC,
                new C2SRenamePack.Handler());
        registrar.playToServer(C2SOpenRingPack.TYPE, C2SOpenRingPack.STREAM_CODEC,
                new C2SOpenRingPack.Handler());
        //CHANNEL.registerMessage(id++, NbtDataPack.class, NbtDataPack::write, NbtDataPack::new, NbtDataPack::run);
        //CHANNEL.registerMessage(id++, C2SItemFilterPack.class, C2SItemFilterPack::write, C2SItemFilterPack::new, C2SItemFilterPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(id++, C2SWipChestActionPack.class, C2SWipChestActionPack::write, C2SWipChestActionPack::new, C2SWipChestActionPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(id++, S2CChannelActionPack.class, S2CChannelActionPack::write, S2CChannelActionPack::new, S2CChannelActionPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
//        CHANNEL.registerMessage(id++, S2CChannelListPack.class, S2CChannelListPack::write, S2CChannelListPack::new, S2CChannelListPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
//        CHANNEL.registerMessage(id++, S2CChannelStatePack.class, S2CChannelStatePack::write, S2CChannelStatePack::new, S2CChannelStatePack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
//        CHANNEL.registerMessage(id++, C2SFilterChannelPack.class, C2SFilterChannelPack::write, C2SFilterChannelPack::new, C2SFilterChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(id++, C2SSetChannelPack.class, C2SSetChannelPack::write, C2SSetChannelPack::new, C2SSetChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(id++, C2SAddChannelPack.class, C2SAddChannelPack::write, C2SAddChannelPack::new, C2SAddChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(id++, C2SRenameChannelPack.class, C2SRenameChannelPack::write, C2SRenameChannelPack::new, C2SRenameChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }


//    public static void sendNbtDataToServer(CompoundTag tag) {
//        CHANNEL.sendToServer(new NbtDataPack(tag));
//    }

}
