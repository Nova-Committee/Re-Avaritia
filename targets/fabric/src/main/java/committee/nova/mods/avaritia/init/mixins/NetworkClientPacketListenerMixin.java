package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.common.net.PacketContext;
import committee.nova.mods.avaritia.api.init.handler.NetBaseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class NetworkClientPacketListenerMixin {
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void avaritia$receive(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (!NetBaseHandler.ownsChannel(packet.getIdentifier())) return;
        var data = packet.getData();
        try {
            if (NetBaseHandler.receive(packet.getIdentifier(), data, new PacketContext(null, Minecraft.getInstance()))) ci.cancel();
        } finally { data.release(); }
    }
}
