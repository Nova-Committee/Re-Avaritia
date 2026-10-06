package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Gui.class)
public abstract class ClientGuiMixin {
 @Inject(method="render",at=@At("TAIL")) private void avaritia$darkness(GuiGraphics graphics,float tick,CallbackInfo ci) { var window=net.minecraft.client.Minecraft.getInstance().getWindow(); AvaritiaForgeClient.renderDarkness(graphics,window.getGuiScaledWidth(),window.getGuiScaledHeight()); }
}
