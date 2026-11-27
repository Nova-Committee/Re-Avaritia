package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.render.entity.InfinityArmorRender;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerRenderer.class)
public abstract class ClientPlayerRendererMixin {
 @Inject(method="<init>",at=@At("TAIL")) private void avaritia$armor(EntityRendererProvider.Context context,boolean slim,CallbackInfo ci) {
  PlayerRenderer renderer=(PlayerRenderer)(Object)this; renderer.addLayer(new InfinityArmorRender<>(renderer,context.getModelSet(),slim));
 }
}
