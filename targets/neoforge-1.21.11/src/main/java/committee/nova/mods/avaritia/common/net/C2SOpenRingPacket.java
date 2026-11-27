package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.item.misc.InfinityRingItem;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/** Opens player-bound ring GUIs from the N keybind. */
public record C2SOpenRingPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SOpenRingPacket> TYPE = new CustomPacketPayload.Type<>(Const.rl("c2s_open_ring"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SOpenRingPacket> STREAM_CODEC = StreamCodec.unit(new C2SOpenRingPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SOpenRingPacket> {
        @Override
        public void handle(@NotNull C2SOpenRingPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    open(player);
                }
            });
        }

        private static void open(ServerPlayer player) {
            if (openFrom(player, player.getMainHandItem(), InteractionHand.MAIN_HAND)) {
                return;
            }
            if (openFrom(player, player.getOffhandItem(), InteractionHand.OFF_HAND)) {
                return;
            }
            ItemStack extra = Const.checkExtraSlots(player,
                    stack -> stack.is(ModItems.neutron_ring.get()) || stack.getItem() instanceof InfinityRingItem,
                    ItemStack.EMPTY, stack -> stack);
            if (openFrom(player, extra, InteractionHand.MAIN_HAND)) {
                return;
            }
            ItemStack packed = NeutronRingItem.find(player);
            openFrom(player, packed, InteractionHand.MAIN_HAND);
        }

        private static boolean openFrom(ServerPlayer player, ItemStack stack, InteractionHand hand) {
            if (stack.is(ModItems.neutron_ring.get())) {
                NeutronRingItem.openGui(player, stack, hand);
                return true;
            }
            if (stack.getItem() instanceof InfinityRingItem) {
                InfinityRingDimensions.openControl(player);
                return true;
            }
            return false;
        }
    }
}
