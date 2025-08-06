package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 12:52
 * @Description:
 */

public class S2CChannelListPack {

    private final CompoundTag myChannels;
    private final CompoundTag otherChannels;
    private final CompoundTag publicChannels;

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

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientChannelManager.getInstance().setChannelList(myChannels, otherChannels, publicChannels)));
        context.get().setPacketHandled(true);
    }
}
