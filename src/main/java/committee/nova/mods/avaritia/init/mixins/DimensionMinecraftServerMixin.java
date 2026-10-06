package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.dimension.DynamicDimensions;
import committee.nova.mods.avaritia.common.item.misc.InfinityClockItem;
import committee.nova.mods.avaritia.init.handler.InfinityRingHandler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class DimensionMinecraftServerMixin {
    @Inject(method = "loadLevel", at = @At("RETURN"))
    private void avaritia$restoreDimensions(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        InfinityRingHandler.onServerStarted(server);
        InfinityClockItem.loadAcceleratedBlocksFromSavedData(server.overworld());
    }

    @Inject(method = "saveAllChunks", at = @At("HEAD"))
    private void avaritia$saveClock(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        if (server.overworld() != null) {
            InfinityClockItem.saveAcceleratedBlocksToSavedData(server.overworld());
        }
    }

    @Inject(method = "stopServer", at = @At("RETURN"))
    private void avaritia$clearClock(CallbackInfo ci) {
        InfinityClockItem.clearServerState();
        DynamicDimensions.clearServerState();
    }
}
