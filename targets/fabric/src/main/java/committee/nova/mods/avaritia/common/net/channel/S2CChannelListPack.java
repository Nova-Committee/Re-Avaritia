package committee.nova.mods.avaritia.common.net.channel;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/28 12:52
 * @Description:
 */

public class S2CChannelListPack {

    public final CompoundTag myChannels;
    public final CompoundTag otherChannels;
    public final CompoundTag publicChannels;

    public S2CChannelListPack(FriendlyByteBuf buf) {
        this.myChannels = buf.readNbt();
        this.otherChannels = buf.readNbt();
        this.publicChannels = buf.readNbt();
    }

    public S2CChannelListPack(CompoundTag my, CompoundTag other, CompoundTag pub) {
        this.myChannels = my;
        this.otherChannels = other;
        this.publicChannels = pub;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(myChannels);
        buf.writeNbt(otherChannels);
        buf.writeNbt(publicChannels);
    }

    public void run(Supplier<PacketContext> context) { context.get().enqueueWork(() -> committee.nova.mods.avaritia.common.net.ClientPacketProxy.handle(this)); context.get().setPacketHandled(true); }
}
