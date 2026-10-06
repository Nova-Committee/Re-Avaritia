package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * @author cnlimiter
 */
public class S2CInfinityChestStatePack {
    public final ChannelState channelState;
    public final CompoundTag tag;

    public S2CInfinityChestStatePack(FriendlyByteBuf buf) {
        this.channelState = buf.readEnum(ChannelState.class);
        this.tag = buf.readNbt();
    }

    public S2CInfinityChestStatePack(ChannelState channelState, CompoundTag tag) {
        this.channelState = channelState;
        this.tag = tag;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(channelState);
        buf.writeNbt(tag);
    }

    public void run(Supplier<PacketContext> context) { context.get().enqueueWork(() -> committee.nova.mods.avaritia.common.net.ClientPacketProxy.handle(this)); context.get().setPacketHandled(true); }
}
