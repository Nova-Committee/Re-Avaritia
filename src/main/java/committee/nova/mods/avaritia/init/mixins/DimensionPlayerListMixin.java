package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.InfinityRingHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class DimensionPlayerListMixin {
    @Inject(method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V", at = @At("RETURN"))
    private void avaritia$login(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        InfinityRingHandler.onLogin(player);
    }

    @Inject(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("RETURN"))
    private void avaritia$respawn(ServerPlayer previous, boolean keepEverything, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        InfinityRingHandler.onJoin(cir.getReturnValue());
    }
}
