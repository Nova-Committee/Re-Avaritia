package committee.nova.mods.avaritia.common.net.channel;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/28 14:07
 * @Description:
 */
public class S2CChannelStatePack {

    public final ChannelState channelState;
    public final CompoundTag tag;

    public S2CChannelStatePack(FriendlyByteBuf buf) {
        this.channelState = buf.readEnum(ChannelState.class);
        this.tag = buf.readNbt();
    }

    public S2CChannelStatePack(ChannelState channelState, CompoundTag tag) {
        this.channelState = channelState;
        this.tag = tag;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(channelState);
        buf.writeNbt(tag);
    }

    public void run(Supplier<PacketContext> context) { context.get().enqueueWork(() -> committee.nova.mods.avaritia.common.net.ClientPacketProxy.handle(this)); context.get().setPacketHandled(true); }
}
