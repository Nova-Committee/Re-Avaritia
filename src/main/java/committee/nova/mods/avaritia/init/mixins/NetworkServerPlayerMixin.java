package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public abstract class NetworkServerPlayerMixin {
    @Redirect(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void avaritia$openingData(ServerGamePacketListenerImpl connection, Packet<?> packet) {
        if (packet instanceof ClientboundOpenScreenPacket open) NetworkHandler.sendOpeningData((ServerPlayer) (Object) this, open.getContainerId(), open.getType());
        connection.send(packet);
    }
}
