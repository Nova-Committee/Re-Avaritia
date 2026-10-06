package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.AvaritiaModClient;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(TextureAtlas.class)
public abstract class ClientTextureAtlasMixin {
 @Inject(method="upload",at=@At("TAIL")) private void avaritia$textures(CallbackInfo ci) { AvaritiaModClient.texturesUploaded((TextureAtlas)(Object)this); }
}
