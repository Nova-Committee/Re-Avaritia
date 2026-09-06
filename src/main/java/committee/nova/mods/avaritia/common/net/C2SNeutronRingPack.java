package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSavedData;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSpaces;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** GUI mutations for the Neutron Ring library. */
public record C2SNeutronRingPack(int action, String id, String name, int hand, UUID storageId,
                                 NeutronRingContents.Size size) implements CustomPacketPayload {
    public static final int SELECT = 0;
    public static final int RENAME = 1;
    public static final int DELETE = 2;
    public static final int DESELECT = 3;
    public static final int SET_SIZE = 4;

    public static final Type<C2SNeutronRingPack> TYPE = new Type<>(Const.rl("c2s_neutron_ring"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SNeutronRingPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SNeutronRingPack::action,
            ByteBufCodecs.STRING_UTF8, C2SNeutronRingPack::id,
            ByteBufCodecs.STRING_UTF8, C2SNeutronRingPack::name,
            ByteBufCodecs.VAR_INT, C2SNeutronRingPack::hand,
            UUIDUtil.STREAM_CODEC, C2SNeutronRingPack::storageId,
            NeutronRingContents.Size.STREAM_CODEC, C2SNeutronRingPack::size,
            C2SNeutronRingPack::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<C2SNeutronRingPack> {
        @Override
        public void handle(@NotNull C2SNeutronRingPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) {
                    return;
                }
                ItemStack stack = NeutronRingItem.resolve(player, packet.hand);
                if (stack.isEmpty()) {
                    return;
                }
                NeutronRingContents data = NeutronRingItem.bind(player, stack);
                if (!data.storageId().equals(packet.storageId)) {
                    return;
                }
                NeutronRingSavedData store = NeutronRingSavedData.get(player.server);
                UUID library = data.storageId();
                switch (packet.action) {
                    case SELECT -> store.get(library, packet.id).ifPresent(space ->
                            stack.set(ModDataComponents.NEUTRON_RING.get(),
                                    data.select(packet.id, NeutronRingSpaces.sizeOf(space.template()))));
                    case RENAME -> {
                        if (!packet.name.isBlank()) {
                            store.rename(library, packet.id, packet.name.trim());
                        }
                    }
                    case DELETE -> {
                        store.remove(library, packet.id);
                        if (data.selectedId().filter(packet.id::equals).isPresent()) {
                            stack.set(ModDataComponents.NEUTRON_RING.get(), data.deselect());
                        }
                    }
                    case DESELECT -> stack.set(ModDataComponents.NEUTRON_RING.get(), data.deselect());
                    case SET_SIZE -> {
                    }
                    default -> {
                    }
                }
                PacketDistributor.sendToPlayer(player, S2CNeutronRingOpenPack.from(player.server,
                        NeutronRingItem.bind(player, stack), handOf(packet.hand)));
            });
        }

        private static InteractionHand handOf(int hand) {
            return hand == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        }
    }
}
