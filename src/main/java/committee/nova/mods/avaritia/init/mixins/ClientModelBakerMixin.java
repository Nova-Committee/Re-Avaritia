package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.model.loader.base.NativeModelGeometry;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ModelBakery.ModelBakerImpl.class)
public abstract class ClientModelBakerMixin {
 @Shadow @Final private Function<Material,TextureAtlasSprite> modelTextureGetter;
 @Inject(method="bake",at=@At("RETURN"),cancellable=true)
 private void avaritia$art(ResourceLocation location,ModelState state,CallbackInfoReturnable<BakedModel> cir) {
  ModelBaker baker=(ModelBaker)(Object)this;
  cir.setReturnValue(NativeModelGeometry.decorate(baker.getModel(location),cir.getReturnValue(),baker,modelTextureGetter,state,location));
 }
}
