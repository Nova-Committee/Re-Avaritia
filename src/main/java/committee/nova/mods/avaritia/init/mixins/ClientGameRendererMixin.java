package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.api.client.screen.component.PortableScreenLayers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public abstract class ClientGameRendererMixin {
 @Inject(method="reloadShaders",at=@At("TAIL")) private void avaritia$shaders(ResourceProvider resources,CallbackInfo ci) { AvaritiaShaders.reload(resources); }
 @Inject(method="render",at=@At("HEAD")) private void avaritia$frame(float partialTick,long time,boolean tick,CallbackInfo ci) { AvaritiaForgeClient.onRenderTickStart(partialTick); }
 @Inject(method="close",at=@At("TAIL")) private void avaritia$close(CallbackInfo ci) { AvaritiaShaders.close(); }
 @Redirect(method="render", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
 private void avaritia$renderLayers(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { PortableScreenLayers.render(screen, graphics, mouseX, mouseY, partialTick); }
 @ModifyConstant(method="render", constant=@Constant(floatValue=21000.0F)) private float avaritia$layerFarPlane(float depth) { return depth + PortableScreenLayers.extraDepth(); }
 @ModifyConstant(method="render", constant=@Constant(floatValue=-11000.0F)) private float avaritia$layerOrigin(float depth) { return depth - PortableScreenLayers.extraDepth(); }
}
