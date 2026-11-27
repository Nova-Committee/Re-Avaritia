package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ScreenEffectRenderer.class)
public abstract class ClientScreenEffectMixin {
 @Inject(method="renderScreenEffect",at=@At("HEAD"),cancellable=true) private static void avaritia$immune(CallbackInfo ci) { if(AvaritiaForgeClient.suppressBlockOverlay())ci.cancel(); }
}
