package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.core.chest.ItemSuper;
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
public record C2SInfinityChestActionPack(int containerId, int actionId, ItemSuper itemSuper) implements CustomPacketPayload {
    public static final Type<C2SInfinityChestActionPack> TYPE = new Type<>(Const.rl("c2s_infinity_chest_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SInfinityChestActionPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            C2SInfinityChestActionPack::containerId,
            ByteBufCodecs.INT,
            C2SInfinityChestActionPack::actionId,
            ItemSuper.STREAM_CODEC,
            C2SInfinityChestActionPack::itemSuper,
            C2SInfinityChestActionPack::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SInfinityChestActionPack> {
        @Override
        public void handle(@NotNull C2SInfinityChestActionPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)
                        || !(player.containerMenu instanceof InfinityChestMenu menu)
                        || menu.containerId != packet.containerId
                        || !menu.stillValid(player)
                        || !menu.canPlayerModify(player)) {
                    Const.LOGGER.debug("Rejected invalid infinity chest action from {}", context.player().getUUID());
                    return;
                }
                menu.action(packet.actionId, packet.itemSuper);
                menu.broadcastChanges();
            });
        }
    }



}
