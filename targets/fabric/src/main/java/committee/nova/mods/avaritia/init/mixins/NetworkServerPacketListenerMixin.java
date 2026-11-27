package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.common.net.PacketContext;
import committee.nova.mods.avaritia.api.init.handler.NetBaseHandler;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
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
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void avaritia$receive(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (NetBaseHandler.receive(packet.getIdentifier(), packet.getData(), new PacketContext(player, player.server))) ci.cancel();
    }
}
