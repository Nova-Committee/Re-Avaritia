package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public record C2SInfinityChestActionPack(int containerId, int actionId, String id) implements CustomPacketPayload {
    public static final Type<C2SInfinityChestActionPack> TYPE = new Type<>(Const.rl("c2s_infinity_chest_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SInfinityChestActionPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            C2SInfinityChestActionPack::containerId,
            ByteBufCodecs.INT,
            C2SInfinityChestActionPack::actionId,
            ByteBufCodecs.STRING_UTF8,
            C2SInfinityChestActionPack::id,
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
                if (context.player() instanceof ServerPlayer player)  {
                    if (player.containerMenu.containerId != packet.containerId) return;
                    if (!player.containerMenu.stillValid(player)) {
                        Const.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                    } else {
                        ((InfinityChestMenu) player.containerMenu).action(packet.actionId, packet.id);
                        player.containerMenu.broadcastChanges();
                    }
                }

            });
        }
    }



}
