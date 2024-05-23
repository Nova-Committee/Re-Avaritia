package committee.nova.mods.avaritia.init.mixin;

import io.github.fabricators_of_create.porting_lib.event.common.RecipesUpdatedCallback;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ClientPacketMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午12:05
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketMixin {
//
//    @Inject(
//            method = "handleUpdateRecipes",
//            at = @At(value = "TAIL")
//    )
//    private void placeNewPlayer(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket, CallbackInfo ci) {
//        RecipesUpdatedCallback.EVENT.invoker().onDatapackSync((PlayerList) (Object) this, player);
//    }
}
