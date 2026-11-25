package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSpawnPoint;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;
import java.util.UUID;

/** Optional play payloads, separate from the original ring management protocol. */
public final class InfinityRingSpawnPayloads {
    private InfinityRingSpawnPayloads() {
    }

    public enum Result {
        STATUS, SAVED, UNSAFE, DENIED
    }

    public record Request(UUID owner, UUID requestId, boolean save) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(Const.rl("c2s_infinity_ring_spawn"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Request::owner,
                UUIDUtil.STREAM_CODEC, Request::requestId,
                ByteBufCodecs.BOOL, Request::save,
                Request::new);

        @Override
        public Type<Request> type() {
            return TYPE;
        }
    }

    public record Reply(UUID owner, UUID requestId, Result result,
                        Optional<InfinityRingSpawnPoint> spawn) implements CustomPacketPayload {
        public static final Type<Reply> TYPE = new Type<>(Const.rl("s2c_infinity_ring_spawn"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Reply> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Reply::owner,
                UUIDUtil.STREAM_CODEC, Reply::requestId,
                ByteBufCodecs.idMapper(id -> Result.values()[id], Result::ordinal), Reply::result,
                ByteBufCodecs.optional(InfinityRingSpawnPoint.STREAM_CODEC), Reply::spawn,
                Reply::new);

        @Override
        public Type<Reply> type() {
            return TYPE;
        }
    }
}
