package committee.nova.mods.avaritia.common.net.client;

import committee.nova.mods.avaritia.api.iface.IDataReceiver;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.api.init.handler.NetBaseHandler;
import committee.nova.mods.avaritia.api.init.registry.DataMenuType;
import committee.nova.mods.avaritia.common.net.*;
import committee.nova.mods.avaritia.common.net.channel.*;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;

/** Loaded exclusively by the Fabric client entrypoint. */
public final class NetworkClientPackets {
    private NetworkClientPackets() {}
    public static void initialize() {
        NetBaseHandler.setClientSender(packet -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) throw new IllegalStateException("No active server connection");
            connection.send(packet);
        });
        ClientPacketProxy.initialize(NetworkClientPackets::handle);
    }
    private static void handle(Object packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (packet instanceof S2CMenuDataPack menu) {
            DataMenuType.setOpeningData(menu.containerId, new FriendlyByteBuf(Unpooled.wrappedBuffer(menu.data)));
        } else if (packet instanceof NbtDataPack nbt) {
            if (minecraft.screen instanceof IDataReceiver receiver) receiver.receive(nbt.tag);
        } else if (packet instanceof S2CSingularitiesPack singularities) {
            SingularityReloadListener.INSTANCE.replaceEffectiveSnapshot(singularities.singularities);
        } else if (packet instanceof S2CTotemPack totem) {
            if (minecraft.level == null) return;
            var entity = minecraft.level.getEntity(totem.entityId);
            if (entity == null) return;
            minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
            minecraft.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1, 1, false);
            minecraft.gameRenderer.displayItemActivation(totem.stack);
        } else if (packet instanceof S2CSideConfigSyncPacket configuration) {
            if (minecraft.level != null && minecraft.level.getBlockEntity(configuration.pos) instanceof ITileIO tile)
                tile.setSideConfiguration(configuration.sideConfig);
        } else if (packet instanceof S2CChannelActionPack action) {
            var manager = ClientChannelManager.getInstance();
            switch (action.action) {
                case ADD -> manager.addChannel(action.type, action.id, action.name);
                case REMOVE -> manager.removeChannel(action.type, action.id, action.name);
                case SET -> manager.setSelectedChannel(action.type, action.id, action.name);
            }
        } else if (packet instanceof S2CChannelListPack list) {
            ClientChannelManager.getInstance().setChannelList(list.myChannels, list.otherChannels, list.publicChannels);
        } else if (packet instanceof S2CChannelStatePack state) {
            var manager = ClientChannelManager.getInstance();
            switch (state.channelState) {
                case COMMON -> manager.updateChannel(state.tag);
                case FULL -> manager.fullUpdateChannel(state.tag);
                case NAME -> manager.setUserCache(state.tag);
            }
        } else if (packet instanceof S2CInfinityChestStatePack state) {
            var manager = ClientChestManager.getInstance();
            switch (state.channelState) {
                case COMMON -> manager.updateChest(state.tag);
                case FULL -> manager.fullUpdateChest(state.tag);
                case NAME -> manager.setUserCache(state.tag);
            }
        } else throw new IllegalArgumentException("Unhandled client packet " + packet.getClass());
    }
}
