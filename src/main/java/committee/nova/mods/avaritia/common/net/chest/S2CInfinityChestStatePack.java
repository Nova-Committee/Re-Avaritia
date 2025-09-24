package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author: cnlimiter
 */
public class S2CInfinityChestStatePack {
    private final ChannelState channelState;
    private final CompoundTag tag;

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

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            switch (channelState) {
                case COMMON -> ClientChestManager.getInstance().updateChannel(tag);
                case FULL -> ClientChestManager.getInstance().fullUpdateChannel(tag);
                case NAME -> ClientChestManager.getInstance().setUserCache(tag);
            }
        }));
        context.get().setPacketHandled(true);
    }
}
