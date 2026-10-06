package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FogRenderer.class)
public abstract class ClientFogMixin {
 @Inject(method="setupFog",at=@At("TAIL")) private static void avaritia$fog(Camera camera,FogRenderer.FogMode mode,float distance,boolean thick,float tick,CallbackInfo ci) { AvaritiaForgeClient.onFog(camera,distance); }
}
