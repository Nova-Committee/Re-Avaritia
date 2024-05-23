package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.net.S2CPacket;
import committee.nova.mods.avaritia.api.common.net.SimpleChannel;
import committee.nova.mods.avaritia.api.common.net.SimplePacketBase;
import committee.nova.mods.avaritia.common.net.SyncSingularitiesPacket;
import committee.nova.mods.avaritia.common.net.TotemPacket;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.function.Function;

import static committee.nova.mods.avaritia.api.common.net.SimplePacketBase.NetworkDirection.PLAY_TO_CLIENT;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 19:20
 * Name NetworkHandler
 * Description
 */

public enum NetworkHandler {
    SYNC_SINGULARITIES(SyncSingularitiesPacket.class, SyncSingularitiesPacket::new, PLAY_TO_CLIENT),
    SYNC_TOTEMS(TotemPacket.class, TotemPacket::new, PLAY_TO_CLIENT);
    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Static.MOD_ID, "main");
    public static final int NETWORK_VERSION = 3;
    public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
    @Getter
    private static SimpleChannel channel;

    private PacketType<?> packetType;

    <T extends SimplePacketBase> NetworkHandler(Class<T> type, Function<FriendlyByteBuf, T> factory,
                                            SimplePacketBase.NetworkDirection direction) {
        packetType = new PacketType<>(type, factory, direction);
    }

    public static void registerPackets() {
        channel = new SimpleChannel(CHANNEL_NAME);
        for (NetworkHandler packet : values())
            packet.packetType.register();
    }

    public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
        getChannel().sendToClientsAround((S2CPacket) message, (ServerLevel) world, pos, range);
    }

    private static class PacketType<T extends SimplePacketBase> {
        private static int index = 0;

        private Function<FriendlyByteBuf, T> decoder;
        private Class<T> type;
        private SimplePacketBase.NetworkDirection direction;

        private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, SimplePacketBase.NetworkDirection direction) {
            decoder = factory;
            this.type = type;
            this.direction = direction;
        }

        private void register() {
            switch (direction) {
                case PLAY_TO_CLIENT -> getChannel().registerS2CPacket(type, index++, decoder);
                case PLAY_TO_SERVER -> getChannel().registerC2SPacket(type, index++, decoder);
            }
        }
    }
}
