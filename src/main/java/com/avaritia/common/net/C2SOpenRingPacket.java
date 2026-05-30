package com.avaritia.common.net;

import com.avaritia.Const;
import com.avaritia.api.utils.InventoryUtils;
import com.avaritia.common.menu.NeutronRingMenu;
import com.avaritia.init.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * C2SOpenRingPack
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
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
                var player = context.player();
                if (player instanceof ServerPlayer serverPlayer){
                    var ring = InventoryUtils.findItemInInv(serverPlayer, stack -> stack.is(ModItems.neutron_ring.get()), stack -> stack);
                    if (!ring.isEmpty()) {
                        serverPlayer.openMenu(
                                new SimpleMenuProvider((id, playerInventory, player1) -> new NeutronRingMenu(id, playerInventory, -1), Component.translatable("item.avaritia.neutron_ring")),
                                buf -> buf.writeInt(-1));
                    }
                }
            });
        }
    }
}
