package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.AvaritiaModClient;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LayerDefinitions.class)
public abstract class ClientLayerDefinitionsMixin {
 @Inject(method="createRoots",at=@At("RETURN"),cancellable=true)
 private static void avaritia$layers(CallbackInfoReturnable<Map<ModelLayerLocation,LayerDefinition>> cir) {
  Map<ModelLayerLocation,LayerDefinition> roots=new java.util.HashMap<>(cir.getReturnValue()); AvaritiaModClient.addLayers(roots); cir.setReturnValue(Map.copyOf(roots));
 }
}
