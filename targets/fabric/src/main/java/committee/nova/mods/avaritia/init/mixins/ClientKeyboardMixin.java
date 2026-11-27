package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(KeyboardHandler.class)
public abstract class ClientKeyboardMixin {
 @Inject(method="keyPress",at=@At("HEAD"),cancellable=true)
 private void avaritia$inspect(long window,int key,int scan,int action,int modifiers,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
  var minecraft=net.minecraft.client.Minecraft.getInstance();
  if(window==minecraft.getWindow().getWindow() && action==org.lwjgl.glfw.GLFW.GLFW_PRESS && minecraft.screen!=null
    && UiInspector.onKey(minecraft.screen,key,modifiers))ci.cancel();
 }
}
