package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** Client actions for Infinity Ring create/control screens. */
public record C2SInfinityRingPack(int action, int terrain, int time, int weather, int access,
                                  String name, UUID owner) implements CustomPacketPayload {
    public static final int CREATE = 0;
    public static final int SET_MODES = 1;
    public static final int ADD_FRIEND = 2;
    public static final int REMOVE_FRIEND = 3;
    public static final int VISIT = 4;
    public static final int DELETE = 5;
    public static final int SET_ROLE = 6;
    public static final int BAN = 7;
    public static final int UNBAN = 8;

    public static final Type<C2SInfinityRingPack> TYPE = new Type<>(Const.rl("c2s_infinity_ring"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SInfinityRingPack> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.action);
                buf.writeVarInt(packet.terrain);
                buf.writeVarInt(packet.time);
                buf.writeVarInt(packet.weather);
                buf.writeVarInt(packet.access);
                buf.writeUtf(packet.name);
                UUIDUtil.STREAM_CODEC.encode(buf, packet.owner);
            },
            buf -> new C2SInfinityRingPack(
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readUtf(), UUIDUtil.STREAM_CODEC.decode(buf)));

    public static C2SInfinityRingPack of(int action, int terrain, int time, int weather, int access, String name, UUID owner) {
        return new C2SInfinityRingPack(action, terrain, time, weather, access, name, owner);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<C2SInfinityRingPack> {
        @Override
        public void handle(@NotNull C2SInfinityRingPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)
                        || !InfinityRingDimensions.holdingRing(player)) {
                    return;
                }
                switch (packet.action) {
                    case CREATE -> InfinityRingDimensions.create(player,
                            InfinityRingSettings.Terrain.byId(packet.terrain),
                            InfinityRingSettings.TimeMode.byId(packet.time),
                            InfinityRingSettings.WeatherMode.byId(packet.weather),
                            InfinityRingSettings.Access.byId(packet.access));
                    case SET_MODES -> InfinityRingDimensions.updateModes(player, packet.owner,
                            InfinityRingSettings.TimeMode.byId(packet.time),
                            InfinityRingSettings.WeatherMode.byId(packet.weather),
                            InfinityRingSettings.Access.byId(packet.access));
                    case ADD_FRIEND -> InfinityRingDimensions.addFriend(player, packet.owner, packet.name);
                    case REMOVE_FRIEND -> InfinityRingDimensions.removeFriend(player, packet.owner, packet.name);
                    case VISIT -> InfinityRingDimensions.visit(player, packet.name);
                    case DELETE -> InfinityRingDimensions.deleteOwn(player);
                    case SET_ROLE -> InfinityRingDimensions.setRole(player, packet.owner, packet.name,
                            InfinityRingSettings.Role.byId(packet.access));
                    case BAN -> InfinityRingDimensions.ban(player, packet.owner, packet.name);
                    case UNBAN -> InfinityRingDimensions.unban(player, packet.owner, packet.name);
                    default -> {
                    }
                }
            });
        }
    }
}
