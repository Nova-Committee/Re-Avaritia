package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.UUID;

/** Request the current entry point or save the server-observed player position. */
public record C2SInfinityRingSpawnPack(UUID owner, UUID request, boolean save) implements CustomPacketPayload {
    public static final Type<C2SInfinityRingSpawnPack> TYPE = new Type<>(Const.rl("c2s_infinity_ring_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SInfinityRingSpawnPack> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, C2SInfinityRingSpawnPack::owner,
                    UUIDUtil.STREAM_CODEC, C2SInfinityRingSpawnPack::request,
                    ByteBufCodecs.BOOL, C2SInfinityRingSpawnPack::save, C2SInfinityRingSpawnPack::new);

    public static boolean supported(ICommonPacketListener listener) {
        return NetworkRegistry.hasChannel(listener, TYPE.id())
                && NetworkRegistry.hasChannel(listener, S2CInfinityRingSpawnPack.TYPE.id());
    }

    public static void handle(C2SInfinityRingSpawnPack packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && supported(player.connection)) {
            context.reply(InfinityRingDimensions.handleSpawnRequest(player, packet));
        }
    }

    @Override
    public Type<C2SInfinityRingSpawnPack> type() {
        return TYPE;
    }
}
