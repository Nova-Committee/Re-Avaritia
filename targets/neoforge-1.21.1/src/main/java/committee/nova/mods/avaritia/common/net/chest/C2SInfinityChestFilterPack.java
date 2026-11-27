package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public record C2SInfinityChestFilterPack(int containerId, String filter) implements CustomPacketPayload {
    private static final int MAX_FILTER_LENGTH = 64;
    private static final StreamCodec<RegistryFriendlyByteBuf, String> FILTER_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(limitFilter(value), MAX_FILTER_LENGTH),
            buffer -> buffer.readUtf(MAX_FILTER_LENGTH));

    public static final Type<C2SInfinityChestFilterPack> TYPE = new Type<>(Const.rl("c2s_infinity_chest_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SInfinityChestFilterPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            C2SInfinityChestFilterPack::containerId,
            FILTER_CODEC,
            C2SInfinityChestFilterPack::filter,
            C2SInfinityChestFilterPack::new
    );

    public C2SInfinityChestFilterPack {
        filter = limitFilter(filter);
    }

    private static String limitFilter(String value) {
        String safe = value == null ? "" : value;
        return safe.substring(0, Math.min(MAX_FILTER_LENGTH, safe.length()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SInfinityChestFilterPack> {
        @Override
        public void handle(@NotNull C2SInfinityChestFilterPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)
                        || !(player.containerMenu instanceof InfinityChestMenu menu)
                        || menu.containerId != packet.containerId
                        || !menu.stillValid(player)
                        || !menu.canPlayerModify(player)) {
                    Const.LOGGER.debug("Rejected invalid infinity chest filter update from {}", context.player().getUUID());
                    return;
                }
                menu.setFilterFromClient(packet.filter);
                menu.broadcastChanges();
            });
        }
    }



}
