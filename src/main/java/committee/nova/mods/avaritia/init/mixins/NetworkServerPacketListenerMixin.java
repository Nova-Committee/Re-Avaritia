package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.common.net.PacketContext;
import committee.nova.mods.avaritia.api.init.handler.NetBaseHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class NetworkServerPacketListenerMixin {
    @Shadow public ServerPlayer player;

    // 26.3: custom payloads are records; our channels arrive as NetBaseHandler.ChannelPayload.
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void avaritia$receive(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (!(packet.payload() instanceof NetBaseHandler.ChannelPayload payload)) return;
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));
        try {
            if (NetBaseHandler.receive(payload.type().id(), data, new PacketContext(player, player.server))) ci.cancel();
        } finally {
            data.release();
        }
    }
}
