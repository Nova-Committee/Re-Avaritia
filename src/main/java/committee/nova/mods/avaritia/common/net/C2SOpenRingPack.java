package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.util.InventoryUtils;
import committee.nova.mods.avaritia.common.menu.NeutronRingMenu;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * C2SJEIGhostPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
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
            var ring = InventoryUtils.findItemInInv(player, stack -> stack.is(ModItems.neutron_ring.get()), stack -> stack);
            if (player != null && !ring.isEmpty()) {
                NetworkHooks.openScreen(player,
                        new SimpleMenuProvider((id, playerInventory, player1) -> new NeutronRingMenu(id, playerInventory, -1), Component.translatable("item.avaritia.neutron_ring")),
                        buf -> buf.writeInt(-1));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
