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
 * @author cnlimiter
 * @CreateTime: 2025/2/28 14:07
 * @Description:
 */
public class S2CChannelStatePack {

    private final ChannelState channelState;
    private final CompoundTag tag;

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

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            switch (channelState) {
                case COMMON -> ClientChannelManager.getInstance().updateChannel(tag);
                case FULL -> ClientChannelManager.getInstance().fullUpdateChannel(tag);
                case NAME -> ClientChannelManager.getInstance().setUserCache(tag);
            }
        }));
        context.get().setPacketHandled(true);
    }
}
