package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

/** 以完整物品组件身份描述的虚拟槽动作。 */
public record C2SInfinityChestActionPacket(
        int containerId, ChannelAction action, ItemResource resource) implements CustomPacketPayload {
    public static final Type<C2SInfinityChestActionPacket> TYPE =
            new Type<>(Const.rl("c2s_infinity_chest_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SInfinityChestActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, C2SInfinityChestActionPacket::containerId,
                    ChannelAction.STREAM_CODEC, C2SInfinityChestActionPacket::action,
                    ItemResource.STREAM_CODEC, C2SInfinityChestActionPacket::resource,
                    C2SInfinityChestActionPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<C2SInfinityChestActionPacket> {
        @Override
        public void handle(@NotNull C2SInfinityChestActionPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                var menu = InfinityChestPayloadGuard.menu(context, packet.containerId());
                if (menu != null && (menu.contains(packet.resource())
                        || packet.resource().isEmpty() && !menu.getCarried().isEmpty())) {
                    menu.action(packet.action(), packet.resource());
                    menu.broadcastChanges();
                }
            });
        }
    }
}
