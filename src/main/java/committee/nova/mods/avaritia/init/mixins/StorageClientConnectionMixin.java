package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public abstract class StorageClientConnectionMixin {
    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void avaritia$resetStorage(CallbackInfo ci) { ClientChannelManager.onLoggingOutServer(); ClientChestManager.onLoggingOutServer(); }
}
