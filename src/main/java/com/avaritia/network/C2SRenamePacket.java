package com.avaritia.network;

import com.avaritia.Avaritia;
import com.avaritia.common.menu.ExtremeAnvilMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * C2SRenamePacket — 客户端请求重命名铁砧物品。
 *
 * @author cnlimiter
 * @version 1.0
 * @date 2024/3/28 14:02
 */
public record C2SRenamePacket(String name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SRenamePacket> TYPE = new CustomPacketPayload.Type<>(Identifier.of(Avaritia.MOD_ID, "c2s_rename"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRenamePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            C2SRenamePacket::name,
            C2SRenamePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SRenamePacket> {
        @Override
        public void handle(@NotNull C2SRenamePacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                var player = context.player();
                if (player instanceof ServerPlayer serverPlayer){
                    AbstractContainerMenu abstractcontainermenu = serverPlayer.containerMenu;
                    if (abstractcontainermenu instanceof ExtremeAnvilMenu anvilmenu) {
                        anvilmenu.setItemName(packet.name);
                    }
                }
            });
        }
    }
}
