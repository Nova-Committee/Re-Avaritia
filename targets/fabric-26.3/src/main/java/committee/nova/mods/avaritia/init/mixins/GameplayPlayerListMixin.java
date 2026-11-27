package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.AbilityHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class GameplayPlayerListMixin {
    @Inject(method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V", at = @At("HEAD"))
    private void avaritia$login(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) { AbilityHandler.stripAbilities(player); }
    @Inject(method = "remove", at = @At("HEAD"))
    private void avaritia$logout(ServerPlayer player, CallbackInfo ci) { AbilityHandler.stripAbilities(player); }
}
