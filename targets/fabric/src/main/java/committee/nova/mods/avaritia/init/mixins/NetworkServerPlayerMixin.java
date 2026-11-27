package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class NetworkServerPlayerMixin {
    @Inject(method = "openMenu", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;getType()Lnet/minecraft/world/inventory/MenuType;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void avaritia$openingData(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir,
                                      AbstractContainerMenu menu) {
        NetworkHandler.sendOpeningData((ServerPlayer) (Object) this, menu.containerId, menu.getType());
    }
}
