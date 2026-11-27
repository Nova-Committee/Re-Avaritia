package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSpawnPoint;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

public record S2CInfinityRingSpawnPack(UUID owner, UUID request, Result result,
                                       Optional<InfinityRingSpawnPoint> spawn) implements CustomPacketPayload {
    public enum Result {
        STATUS, SAVED, UNSAFE, DENIED
    }

    public static final Type<S2CInfinityRingSpawnPack> TYPE = new Type<>(Const.rl("s2c_infinity_ring_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CInfinityRingSpawnPack> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, S2CInfinityRingSpawnPack::owner,
                    UUIDUtil.STREAM_CODEC, S2CInfinityRingSpawnPack::request,
                    ByteBufCodecs.idMapper(id -> Result.values()[id], Result::ordinal), S2CInfinityRingSpawnPack::result,
                    ByteBufCodecs.optional(InfinityRingSpawnPoint.STREAM_CODEC), S2CInfinityRingSpawnPack::spawn,
                    S2CInfinityRingSpawnPack::new);

    public static void handle(S2CInfinityRingSpawnPack packet, IPayloadContext context) {
        ClientPacketProxy.infinityRingSpawn.accept(packet);
    }

    @Override
    public Type<S2CInfinityRingSpawnPack> type() {
        return TYPE;
    }
}
