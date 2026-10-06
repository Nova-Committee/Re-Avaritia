package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.misc.InfinityClockItem;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class DimensionServerLevelMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void avaritia$clockTick(BooleanSupplier hasTime, CallbackInfo ci) {
        InfinityClockItem.TickHandler.onServerTick((ServerLevel) (Object) this);
    }
}
