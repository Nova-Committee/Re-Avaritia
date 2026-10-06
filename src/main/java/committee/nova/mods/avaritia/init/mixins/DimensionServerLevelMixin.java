package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.misc.InfinityClockItem;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class DimensionServerLevelMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void avaritia$clockTick(BooleanSupplier hasTime, CallbackInfo ci) {
        InfinityClockItem.TickHandler.onServerTick((ServerLevel) (Object) this);
    }

    // Forge scoped these vanilla weather broadcasts to their originating dimension.
    @Redirect(method = "advanceWeatherCycle", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"), require = 4)
    private void avaritia$dimensionWeather(PlayerList players, Packet<?> packet) {
        players.broadcastAll(packet, ((ServerLevel) (Object) this).dimension());
    }
}
