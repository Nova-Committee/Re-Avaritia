package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.DataPackSyncHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class SingularityPlayerListMixin {
    @Shadow public abstract MinecraftServer getServer();

    @Inject(method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V", at = @At("TAIL"))
    private void avaritia$syncJoiningPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo callback) {
        DataPackSyncHandler.onPlayerLogin(player);
    }

    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void avaritia$syncReloadedData(CallbackInfo callback) {
        DataPackSyncHandler.onDatapackSync(this.getServer());
    }
}
