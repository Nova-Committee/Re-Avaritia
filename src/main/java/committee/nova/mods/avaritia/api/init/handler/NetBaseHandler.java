package committee.nova.mods.avaritia.api.init.handler;

import committee.nova.mods.avaritia.api.common.net.IPacket;
import committee.nova.mods.avaritia.api.common.net.PacketContext;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import java.util.*;
import java.util.function.*;

/** Project-owned play protocol carried by vanilla custom payload packets. */
public final class NetBaseHandler {
    public enum Direction { SERVERBOUND, CLIENTBOUND, BOTH }
    private static final int PROTOCOL = 1;
    private static final Map<Identifier, NetBaseHandler> CHANNELS = new HashMap<>();
    private static Consumer<CustomPacketPayload> clientSender;
    private final Identifier identifier;
    private final CustomPacketPayload.Type<ChannelPayload> payloadType;
    private final List<Registration<?>> registrations = new ArrayList<>();
    private final Map<Class<?>, Registration<?>> byType = new HashMap<>();

    public NetBaseHandler(Identifier identifier) {
        this.identifier = identifier;
        if (CHANNELS.putIfAbsent(identifier, this) != null) throw new IllegalArgumentException("Duplicate channel " + identifier);
        this.payloadType = new CustomPacketPayload.Type<>(identifier);
        PayloadTypeRegistry.serverboundPlay().register(this.payloadType, ChannelPayload.codec(this.payloadType));
        PayloadTypeRegistry.clientboundPlay().register(this.payloadType, ChannelPayload.codec(this.payloadType));
    }

    public static void setClientSender(Consumer<CustomPacketPayload> sender) { clientSender = Objects.requireNonNull(sender); }
    public static boolean ownsChannel(Identifier identifier) { return CHANNELS.containsKey(identifier); }

    public <T> void register(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder,
                             Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<PacketContext>> consumer,
                             Direction direction) {
        Registration<T> entry = new Registration<>(registrations.size(), type, encoder, decoder, consumer, direction);
        if (byType.putIfAbsent(type, entry) != null) throw new IllegalArgumentException("Duplicate packet " + type);
        registrations.add(entry);
    }

    public <T extends IPacket<T>> void register(Class<T> type, IPacket<T> codec, Direction direction) {
        register(type, codec::write, codec::read, codec::run, direction);
    }

    public static boolean receive(Identifier identifier, FriendlyByteBuf data, PacketContext context) {
        NetBaseHandler channel = CHANNELS.get(identifier);
        if (channel == null) return false;
        try {
            if (data.readVarInt() != PROTOCOL) throw new IllegalArgumentException("Protocol version");
            int id = data.readVarInt();
            if (id < 0 || id >= channel.registrations.size()) throw new IllegalArgumentException("Unknown packet");
            Registration<?> registration = channel.registrations.get(id);
            registration.receive(data, context);
            if (data.isReadable()) throw new IllegalArgumentException("Trailing packet bytes");
        } catch (RuntimeException exception) {
            committee.nova.mods.avaritia.Const.LOGGER.warn("Rejected malformed Avaritia payload {}", identifier, exception);
            if (context.getSender() != null) context.getSender().connection.disconnect(Component.literal("Invalid Avaritia network payload"));
        }
        return true;
    }

    private <T> byte[] encode(T message, boolean serverbound) {
        @SuppressWarnings("unchecked") Registration<T> registration = (Registration<T>) byType.get(message.getClass());
        if (registration == null) throw new IllegalArgumentException("Unregistered packet " + message.getClass());
        registration.checkDirection(serverbound);
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        try {
            data.writeVarInt(PROTOCOL);
            data.writeVarInt(registration.id);
            registration.encoder.accept(message, data);
            int limit = serverbound ? 32767 : 1048576;
            if (data.readableBytes() > limit) throw new IllegalArgumentException("Avaritia packet exceeds vanilla payload limit");
            byte[] bytes = new byte[data.readableBytes()];
            data.readBytes(bytes);
            return bytes;
        } finally {
            data.release();
        }
    }

    public <T> void sendToServer(T message) {
        if (clientSender == null) throw new IllegalStateException("Client networking is not initialized");
        clientSender.accept(new ChannelPayload(this.payloadType, encode(message, true)));
    }
    public <T> void sendTo(ServerPlayer player, T message) {
        ServerPlayNetworking.send(player, new ChannelPayload(this.payloadType, encode(message, false)));
    }
    public <T> void sendToAll(MinecraftServer server, T message) {
        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;
        ChannelPayload payload = new ChannelPayload(this.payloadType, encode(message, false));
        for (ServerPlayer player : players) ServerPlayNetworking.send(player, payload);
    }
    public <T> void sendToNear(Level level, BlockPos pos, double radius, T message) {
        if (level.isClientSide) return;
        ChannelPayload payload = null;
        double radiusSquared = radius * radius;
        double x = pos.getX() + .5, y = pos.getY() + .5, z = pos.getZ() + .5;
        for (net.minecraft.world.entity.player.Player player : level.players()) {
            if (player instanceof ServerPlayer serverPlayer && player.distanceToSqr(x, y, z) <= radiusSquared) {
                if (payload == null) payload = new ChannelPayload(this.payloadType, encode(message, false));
                ServerPlayNetworking.send(serverPlayer, payload);
            }
        }
    }
    public <T> void reply(T message, PacketContext context) {
        if (context.getSender() == null) throw new IllegalArgumentException("Reply requires serverbound context");
        sendTo(context.getSender(), message);
    }

    /** Wrapper payload routed through the Fabric payload registries; carries one encoded project packet. */
    public record ChannelPayload(CustomPacketPayload.Type<ChannelPayload> payloadType, byte[] data) implements CustomPacketPayload {
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return this.payloadType;
        }

        public static StreamCodec<FriendlyByteBuf, ChannelPayload> codec(CustomPacketPayload.Type<ChannelPayload> type) {
            return CustomPacketPayload.codec((payload, buf) -> buf.writeByteArray(payload.data()),
                    buf -> new ChannelPayload(type, buf.readByteArray()));
        }
    }

    private record Registration<T>(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder,
                                   Function<FriendlyByteBuf, T> decoder,
                                   BiConsumer<T, Supplier<PacketContext>> consumer, Direction direction) {
        private void checkDirection(boolean serverbound) {
            if (direction != Direction.BOTH && direction != (serverbound ? Direction.SERVERBOUND : Direction.CLIENTBOUND))
                throw new IllegalArgumentException("Wrong packet direction: " + type.getName());
        }
        private void receive(FriendlyByteBuf buffer, PacketContext context) {
            checkDirection(context.isServerbound());
            T message = decoder.apply(buffer);
            if (buffer.isReadable()) throw new IllegalArgumentException("Trailing packet bytes");
            context.enqueueWork(() -> consumer.accept(message, () -> context));
        }
    }
}
