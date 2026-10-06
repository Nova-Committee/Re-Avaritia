package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.InfinityRingHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class DimensionPlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void avaritia$login(Connection connection, ServerPlayer player, CallbackInfo ci) {
        InfinityRingHandler.onLogin(player);
    }

    @Inject(method = "respawn", at = @At("RETURN"))
    private void avaritia$respawn(ServerPlayer previous, boolean keepEverything, CallbackInfoReturnable<ServerPlayer> cir) {
        InfinityRingHandler.onJoin(cir.getReturnValue());
    }
}
