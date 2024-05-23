package committee.nova.mods.avaritia.init.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Name: Avaritia-fabric / PlayerListMixin
 * Author: cnlimiter
 * CreateTime: 2023/9/17 19:31
 * Description:
 */

@Mixin(PlayerList.class)
public class PlayerListMixin {
//    @Inject(
//            method = "placeNewPlayer",
//            at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/network/protocol/game/ClientboundSetCarriedItemPacket;<init>(I)V")
//    )
//    private void placeNewPlayer(Connection netManager, ServerPlayer player, CallbackInfo ci) {
//        OnDataPackSyncCallback.EVENT.invoker().onDatapackSync((PlayerList) (Object) this, player);
//    }
//
//    @Inject(
//            method = "reloadResources",
//            at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V")
//    )
//    private void placeNewPlayer(CallbackInfo ci) {
//        OnDataPackSyncCallback.EVENT.invoker().onDatapackSync((PlayerList) (Object) this, null);
//    }
//
//    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
//    private void setPlayerUsername(Connection netManager, ServerPlayer player, CallbackInfo ci) {
//        //UsernameCache.setUsername(player.getUUID(), player.getGameProfile().getName());
//    }
}
