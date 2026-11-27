package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.core.io.StorageLifecycle;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerList.class)
public abstract class StoragePlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void avaritia$syncNames(Connection connection, ServerPlayer player, CallbackInfo ci) { StorageLifecycle.join(player); }
}
