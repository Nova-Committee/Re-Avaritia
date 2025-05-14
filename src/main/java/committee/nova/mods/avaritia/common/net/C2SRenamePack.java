package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.menu.ExtremeAnvilMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * C2SRenamePack
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public record C2SRenamePack(String name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SRenamePack> TYPE = new CustomPacketPayload.Type<>(Static.rl("c2s_rename"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRenamePack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            C2SRenamePack::name,
            C2SRenamePack::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SRenamePack> {
        @Override
        public void handle(@NotNull C2SRenamePack packet, IPayloadContext context) {
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
