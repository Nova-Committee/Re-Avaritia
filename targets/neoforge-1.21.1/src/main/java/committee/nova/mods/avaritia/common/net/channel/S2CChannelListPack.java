package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record S2CChannelListPack(CompoundTag myChannels, CompoundTag otherChannels, CompoundTag publicChannels)
        implements CustomPacketPayload {
    public static final Type<S2CChannelListPack> TYPE = new Type<>(Const.rl("s2c_tesseract_channel_list"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CChannelListPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, S2CChannelListPack::myChannels,
            ByteBufCodecs.COMPOUND_TAG, S2CChannelListPack::otherChannels,
            ByteBufCodecs.COMPOUND_TAG, S2CChannelListPack::publicChannels,
            S2CChannelListPack::new);

    public S2CChannelListPack {
        myChannels = sanitize(myChannels);
        otherChannels = sanitize(otherChannels);
        publicChannels = sanitize(publicChannels);
    }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static CompoundTag sanitize(CompoundTag source) {
        CompoundTag result = new CompoundTag();
        if (source == null) return result;
        source.getAllKeys().stream().limit(10_000).forEach(key -> {
            try {
                int id = Integer.parseInt(key);
                if (id >= 0 && id < 10_000) result.putString(key, ChannelPayloadCodecs.limit(source.getString(key), 64));
            } catch (NumberFormatException ignored) { }
        });
        return result;
    }

    public static final class Handler implements IPayloadHandler<S2CChannelListPack> {
        @Override public void handle(@NotNull S2CChannelListPack packet, IPayloadContext context) {
            context.enqueueWork(() -> ClientChannelManager.getInstance()
                    .setChannelList(packet.myChannels, packet.otherChannels, packet.publicChannels));
        }
    }
}
