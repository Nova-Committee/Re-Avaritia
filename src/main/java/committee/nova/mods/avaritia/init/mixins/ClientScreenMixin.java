package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Screen.class)
public abstract class ClientScreenMixin {
 @Inject(method="init(Lnet/minecraft/client/Minecraft;II)V",at=@At("TAIL")) private void avaritia$init(CallbackInfo ci) { UiInspector.onInit((Screen)(Object)this); }
 @Inject(method="renderWithTooltip",at=@At("HEAD")) private void avaritia$before(GuiGraphics graphics,int mouseX,int mouseY,float tick,CallbackInfo ci) { AvaritiaForgeClient.inventoryRender=true; UiInspector.onRenderPre((Screen)(Object)this); }
 @Inject(method="renderWithTooltip",at=@At("RETURN")) private void avaritia$after(GuiGraphics graphics,int mouseX,int mouseY,float tick,CallbackInfo ci) { UiInspector.onRenderPost((Screen)(Object)this,graphics); AvaritiaForgeClient.inventoryRender=false; }
}
