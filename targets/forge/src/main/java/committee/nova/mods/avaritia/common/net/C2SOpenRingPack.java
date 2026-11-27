package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.item.misc.InfinityRingItem;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Opens player-bound ring GUIs from the N keybind. */
public class C2SOpenRingPack {

    public C2SOpenRingPack(FriendlyByteBuf buf) {
    }

    public C2SOpenRingPack() {
    }

    public void write(FriendlyByteBuf buf) {
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                open(player);
            }
        });
        ctx.get().setPacketHandled(true);
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
        openFrom(player, NeutronRingItem.find(player), InteractionHand.MAIN_HAND);
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
