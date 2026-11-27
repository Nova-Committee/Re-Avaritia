package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSpawnPoint;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/** Status and result for one request on one owner's management screen. */
public record S2CInfinityRingSpawnPack(UUID owner, UUID request, Result result,
                                       Optional<InfinityRingSpawnPoint> spawn) implements CustomPacketPayload {
    public enum Result { STATUS, SAVED, DENIED, UNSAFE }

    public static final Type<S2CInfinityRingSpawnPack> TYPE = new Type<>(Const.rl("s2c_infinity_ring_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CInfinityRingSpawnPack> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUUID(packet.owner);
                buf.writeUUID(packet.request);
                buf.writeEnum(packet.result);
                ByteBufCodecs.optional(InfinityRingSpawnPoint.STREAM_CODEC).encode(buf, packet.spawn);
            },
            buf -> new S2CInfinityRingSpawnPack(buf.readUUID(), buf.readUUID(), buf.readEnum(Result.class),
                    ByteBufCodecs.optional(InfinityRingSpawnPoint.STREAM_CODEC).decode(buf)));

    public static S2CInfinityRingSpawnPack reply(C2SInfinityRingSpawnPack packet, Result result,
                                                  @Nullable InfinityRingSpawnPoint spawn) {
        return new S2CInfinityRingSpawnPack(packet.owner(), packet.request(), result, Optional.ofNullable(spawn));
    }

    @Override
    public Type<S2CInfinityRingSpawnPack> type() {
        return TYPE;
    }
}
