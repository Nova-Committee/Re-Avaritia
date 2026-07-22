package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.*;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestActionPack;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestFilterPack;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.common.net.channel.*;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

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
        var registrar = event.registrar("1.3");

        registrar.playToClient(S2CSingularitiesPack.TYPE, S2CSingularitiesPack.STREAM_CODEC,
                new S2CSingularitiesPack.Handler());
        registrar.playToClient(S2CTotemPack.TYPE, S2CTotemPack.STREAM_CODEC,
                new S2CTotemPack.Handler());
        registrar.playToClient(S2CSideConfigSyncPacket.TYPE, S2CSideConfigSyncPacket.STREAM_CODEC,
                new S2CSideConfigSyncPacket.Handler());
        registrar.playToClient(S2CInfinityChestStatePack.TYPE, S2CInfinityChestStatePack.STREAM_CODEC,
                new S2CInfinityChestStatePack.Handler());
        registrar.playToClient(S2CChannelActionPack.TYPE, S2CChannelActionPack.STREAM_CODEC,
                new S2CChannelActionPack.Handler());
        registrar.playToClient(S2CChannelListPack.TYPE, S2CChannelListPack.STREAM_CODEC,
                new S2CChannelListPack.Handler());
        registrar.playToClient(S2CChannelStatePack.TYPE, S2CChannelStatePack.STREAM_CODEC,
                new S2CChannelStatePack.Handler());
        registrar.playToClient(S2CNameCachePack.TYPE, S2CNameCachePack.STREAM_CODEC,
                new S2CNameCachePack.Handler());

        registrar.playToServer(C2SSetTimePacket.TYPE, C2SSetTimePacket.STREAM_CODEC,
                new C2SSetTimePacket.Handler());
        registrar.playToServer(C2SSideConfigPacket.TYPE, C2SSideConfigPacket.STREAM_CODEC,
                new C2SSideConfigPacket.Handler());
        registrar.playToServer(C2SCompressorLockPacket.TYPE, C2SCompressorLockPacket.STREAM_CODEC,
                new C2SCompressorLockPacket.Handler());
        registrar.playToServer(C2SCompressorEjectPacket.TYPE, C2SCompressorEjectPacket.STREAM_CODEC,
                new C2SCompressorEjectPacket.Handler());
        registrar.playToServer(C2SRenamePack.TYPE, C2SRenamePack.STREAM_CODEC,
                new C2SRenamePack.Handler());
        registrar.playToServer(C2SOpenRingPack.TYPE, C2SOpenRingPack.STREAM_CODEC,
                new C2SOpenRingPack.Handler());
        registrar.playToServer(C2SInfinityChestActionPack.TYPE, C2SInfinityChestActionPack.STREAM_CODEC,
                new C2SInfinityChestActionPack.Handler());
        registrar.playToServer(C2SInfinityChestFilterPack.TYPE, C2SInfinityChestFilterPack.STREAM_CODEC,
                new C2SInfinityChestFilterPack.Handler());
        registrar.playToServer(C2SAddChannelPack.TYPE, C2SAddChannelPack.STREAM_CODEC,
                new C2SAddChannelPack.Handler());
        registrar.playToServer(C2SSetChannelPack.TYPE, C2SSetChannelPack.STREAM_CODEC,
                new C2SSetChannelPack.Handler());
        registrar.playToServer(C2SRenameChannelPack.TYPE, C2SRenameChannelPack.STREAM_CODEC,
                new C2SRenameChannelPack.Handler());
        registrar.playToServer(C2SChannelFilterPack.TYPE, C2SChannelFilterPack.STREAM_CODEC,
                new C2SChannelFilterPack.Handler());
        registrar.playToServer(C2SChannelActionPack.TYPE, C2SChannelActionPack.STREAM_CODEC,
                new C2SChannelActionPack.Handler());

        registrar.playBidirectional(NbtDataPack.TYPE, NbtDataPack.STREAM_CODEC, new NbtDataPack.Handler());
        //CHANNEL.registerMessage(itemSuper++, NbtDataPack.class, NbtDataPack::write, NbtDataPack::new, NbtDataPack::run);
        //CHANNEL.registerMessage(itemSuper++, C2SItemFilterPack.class, C2SItemFilterPack::write, C2SItemFilterPack::new, C2SItemFilterPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(itemSuper++, C2SWipChestActionPack.class, C2SWipChestActionPack::write, C2SWipChestActionPack::new, C2SWipChestActionPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(itemSuper++, S2CChannelActionPack.class, S2CChannelActionPack::write, S2CChannelActionPack::new, S2CChannelActionPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
//        CHANNEL.registerMessage(itemSuper++, S2CChannelListPack.class, S2CChannelListPack::write, S2CChannelListPack::new, S2CChannelListPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
//        CHANNEL.registerMessage(itemSuper++, S2CChannelStatePack.class, S2CChannelStatePack::write, S2CChannelStatePack::new, S2CChannelStatePack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
//        CHANNEL.registerMessage(itemSuper++, C2SFilterChannelPack.class, C2SFilterChannelPack::write, C2SFilterChannelPack::new, C2SFilterChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(itemSuper++, C2SSetChannelPack.class, C2SSetChannelPack::write, C2SSetChannelPack::new, C2SSetChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(itemSuper++, C2SAddChannelPack.class, C2SAddChannelPack::write, C2SAddChannelPack::new, C2SAddChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//        CHANNEL.registerMessage(itemSuper++, C2SRenameChannelPack.class, C2SRenameChannelPack::write, C2SRenameChannelPack::new, C2SRenameChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }


    public static void sendNbtDataToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(new NbtDataPack(tag));
    }

    public static void sendCompressorLockPacket(BlockPos pos, boolean locked) {
        PacketDistributor.sendToServer(new C2SCompressorLockPacket(pos, locked));
    }

    public static void sendCompressorEjectPacket(BlockPos pos) {
        PacketDistributor.sendToServer(new C2SCompressorEjectPacket(pos));
    }

    public static void sendSideConfigUpdate(BlockPos blockPos, SideConfiguration sideConfig) {
        PacketDistributor.sendToServer(new C2SSideConfigPacket(blockPos, sideConfig));
    }

    public static void sendSideConfigSync(BlockPos pos, SideConfiguration sideConfig) {
        PacketDistributor.sendToAllPlayers(new S2CSideConfigSyncPacket(pos, sideConfig));
    }
}
