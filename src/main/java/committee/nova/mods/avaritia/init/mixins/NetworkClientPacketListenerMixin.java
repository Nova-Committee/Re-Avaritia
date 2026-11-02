package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.common.net.PacketContext;
import committee.nova.mods.avaritia.api.init.handler.NetBaseHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class NetworkClientPacketListenerMixin {
    // 26.3: handleCustomPayload receives the deserialized payload directly.
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void avaritia$receive(CustomPacketPayload packetPayload, CallbackInfo ci) {
        if (!(packetPayload instanceof NetBaseHandler.ChannelPayload payload)) return;
        if (!NetBaseHandler.ownsChannel(payload.type().id())) return;
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));
        try {
            if (NetBaseHandler.receive(payload.type().id(), data, new PacketContext(null, Minecraft.getInstance()))) ci.cancel();
        } finally {
            data.release();
        }
    }
}
