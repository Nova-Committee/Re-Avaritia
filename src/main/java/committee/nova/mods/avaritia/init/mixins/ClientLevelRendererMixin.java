package committee.nova.mods.avaritia.init.mixins;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.client.render.NeutronRingPreviewRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LevelRenderer.class)
public abstract class ClientLevelRendererMixin {
 @Inject(method="renderLevel",at={
  @At(value="INVOKE",target="Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V",ordinal=3,shift=At.Shift.AFTER),
  @At(value="INVOKE",target="Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V",ordinal=5,shift=At.Shift.AFTER)})
 private void avaritia$preview(PoseStack pose,float tick,long time,boolean outline,Camera camera,GameRenderer game,LightTexture light,Matrix4f projection,CallbackInfo ci) { NeutronRingPreviewRenderer.onRender(pose,camera); }
}
