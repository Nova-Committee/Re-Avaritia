package committee.nova.mods.avaritia.init.mixins;
import com.google.gson.*;
import committee.nova.mods.avaritia.client.model.loader.base.NativeModelGeometry;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockModel.Deserializer.class)
public abstract class ClientBlockModelDeserializerMixin {
 @Inject(method="deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;",at=@At("HEAD"),cancellable=true)
 private void avaritia$geometry(JsonElement json,java.lang.reflect.Type type,JsonDeserializationContext context,CallbackInfoReturnable<BlockModel> cir) {
  BlockModel model=NativeModelGeometry.deserialize(json,context); if(model!=null)cir.setReturnValue(model);
 }
}
