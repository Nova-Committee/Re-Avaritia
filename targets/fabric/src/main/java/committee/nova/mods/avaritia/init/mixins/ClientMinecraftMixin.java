package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.*;
import committee.nova.mods.avaritia.api.client.screen.component.PortableScreenLayers;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public abstract class ClientMinecraftMixin {
 @Inject(method="<init>", at=@At("TAIL")) private void avaritia$setup(CallbackInfo ci) { AvaritiaModClient.finishClientSetup((Minecraft)(Object)this); }
 @Inject(method="tick", at=@At("TAIL")) private void avaritia$tick(CallbackInfo ci) { AvaritiaForgeClient.onClientTickEnd(); }
 @Inject(method="setScreen", at=@At("HEAD")) private void avaritia$closing(net.minecraft.client.gui.screens.Screen next,CallbackInfo ci) {
  Minecraft minecraft=(Minecraft)(Object)this;
  PortableScreenLayers.beforeScreenChange(minecraft);
  if(minecraft.screen!=null && minecraft.screen!=next)committee.nova.mods.avaritia.api.client.screen.component.UiInspector.onClosing(minecraft.screen);
 }
 @Inject(method="resizeDisplay", at=@At("TAIL")) private void avaritia$resizeLayers(CallbackInfo ci) { PortableScreenLayers.resizeParents((Minecraft)(Object)this); }
 @Inject(method="destroy", at=@At("HEAD")) private void avaritia$disposeLayers(CallbackInfo ci) { PortableScreenLayers.beforeScreenChange((Minecraft)(Object)this); }
}
