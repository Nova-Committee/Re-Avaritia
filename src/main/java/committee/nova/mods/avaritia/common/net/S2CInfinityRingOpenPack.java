package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/** Opens Infinity Ring create or control screens on the client. */
public record S2CInfinityRingOpenPack(boolean create, int terrain, int time, int weather, int access,
                                      List<String> friends, UUID owner, boolean canDelete) implements CustomPacketPayload {
    public static final Type<S2CInfinityRingOpenPack> TYPE = new Type<>(Const.rl("s2c_infinity_ring_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CInfinityRingOpenPack> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.create);
                buf.writeVarInt(packet.terrain);
                buf.writeVarInt(packet.time);
                buf.writeVarInt(packet.weather);
                buf.writeVarInt(packet.access);
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, packet.friends);
                UUIDUtil.STREAM_CODEC.encode(buf, packet.owner);
                buf.writeBoolean(packet.canDelete);
            },
            buf -> new S2CInfinityRingOpenPack(
                    buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf),
                    UUIDUtil.STREAM_CODEC.decode(buf), buf.readBoolean()));

    public static void openCreate(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                new S2CInfinityRingOpenPack(true, 0, InfinityRingSettings.TimeMode.DAY.ordinal(),
                        InfinityRingSettings.WeatherMode.CLEAR.ordinal(),
                        InfinityRingSettings.Access.PRIVATE.ordinal(), List.of(), player.getUUID(), true));
    }

    public static void openControl(ServerPlayer player, InfinityRingSettings settings, List<String> friends,
                                   UUID owner, boolean canDelete) {
        PacketDistributor.sendToPlayer(player,
                new S2CInfinityRingOpenPack(false, settings.terrain.ordinal(), settings.time.ordinal(),
                        settings.weather.ordinal(), settings.access.ordinal(), friends, owner, canDelete));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<S2CInfinityRingOpenPack> {
        @Override
        public void handle(@NotNull S2CInfinityRingOpenPack packet, IPayloadContext context) {
            context.enqueueWork(() -> ClientPacketProxy.infinityRingOpen.accept(packet));
        }
    }
}
