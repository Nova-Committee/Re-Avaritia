package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public record S2CChannelStatePack(ChannelState channelState, CompoundTag data) implements CustomPacketPayload {
    private static final int MAX_VARIANTS = 2_000;
    public static final Type<S2CChannelStatePack> TYPE = new Type<>(Const.rl("s2c_tesseract_channel_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CChannelStatePack> STREAM_CODEC = StreamCodec.composite(
            ChannelState.STREAM_CODEC, S2CChannelStatePack::channelState,
            ByteBufCodecs.COMPOUND_TAG, S2CChannelStatePack::data,
            S2CChannelStatePack::new);

    public S2CChannelStatePack {
        data = sanitize(channelState, data);
    }

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static CompoundTag sanitize(ChannelState state, CompoundTag source) {
        CompoundTag result = new CompoundTag();
        if (source == null) return result;
        if (state == ChannelState.NAME) {
            CompoundTag names = new CompoundTag();
            CompoundTag sourceNames = source.getCompound("nameCache");
            sourceNames.getAllKeys().stream().limit(4_096).forEach(key ->
                    names.putString(key, ChannelPayloadCodecs.limit(sourceNames.getString(key), 64)));
            result.put("nameCache", names);
            return result;
        }
        AtomicInteger remaining = new AtomicInteger(MAX_VARIANTS);
        result.put("items", sanitizeValues(source.getCompound("items"), remaining, true));
        result.put("fluids", sanitizeValues(source.getCompound("fluids"), remaining, true));
        result.put("energies", sanitizeValues(source.getCompound("energies"), remaining, false));
        if (source.contains("name")) {
            result.putString("name", ChannelPayloadCodecs.limit(source.getString("name"), 64));
        }
        return result;
    }

    private static CompoundTag sanitizeValues(CompoundTag source, AtomicInteger remaining, boolean resourceIds) {
        CompoundTag result = new CompoundTag();
        for (String key : source.getAllKeys()) {
            if (remaining.get() <= 0) break;
            if ((!resourceIds || ResourceLocation.tryParse(key) != null)
                    && (resourceIds || key.equals("avaritia:forge_energy"))) {
                result.putLong(key, source.getLong(key));
                remaining.decrementAndGet();
            }
        }
        return result;
    }

    public static final class Handler implements IPayloadHandler<S2CChannelStatePack> {
        @Override public void handle(@NotNull S2CChannelStatePack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                ClientChannelManager manager = ClientChannelManager.getInstance();
                switch (packet.channelState) {
                    case COMMON -> manager.updateChannel(packet.data);
                    case FULL -> manager.fullUpdateChannel(packet.data);
                    case NAME -> manager.setUserCache(packet.data);
                }
            });
        }
    }
}
