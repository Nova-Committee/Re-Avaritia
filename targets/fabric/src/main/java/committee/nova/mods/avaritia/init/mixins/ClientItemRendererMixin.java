package committee.nova.mods.avaritia.init.mixins;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.client.render.item.*;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemRenderer.class)
public abstract class ClientItemRendererMixin {
 @Unique private InfinityShieldRender avaritia$shield;
 @Unique private InfinityChestItemRender avaritia$chest;
 @Inject(method="render",at=@At("HEAD"),cancellable=true)
 private void avaritia$nativeItem(ItemStack stack,ItemDisplayContext context,boolean left,PoseStack pose,MultiBufferSource buffers,int light,int overlay,BakedModel model,CallbackInfo ci) {
  boolean shield=stack.getItem() instanceof InfinityShieldItem && InfinityShieldItem.getShieldMode(stack)==InfinityShieldItem.MODE_NORMAL;
  boolean chest=stack.is(ModItems.infinity_chest.get());
  if(!shield && !chest)return;
  Minecraft minecraft=Minecraft.getInstance();
  BlockEntityWithoutLevelRenderer renderer;
  if(shield) { if(avaritia$shield==null)avaritia$shield=new InfinityShieldRender(minecraft.getBlockEntityRenderDispatcher(),minecraft.getEntityModels()); renderer=avaritia$shield; }
  else { if(avaritia$chest==null)avaritia$chest=new InfinityChestItemRender(minecraft.getBlockEntityRenderDispatcher(),minecraft.getEntityModels()); renderer=avaritia$chest; }
  pose.pushPose(); model.getTransforms().getTransform(context).apply(left,pose); pose.translate(-0.5,-0.5,-0.5);
  renderer.renderByItem(stack,context,pose,buffers,light,overlay); pose.popPose(); ci.cancel();
 }
 @Inject(method="onResourceManagerReload",at=@At("TAIL")) private void avaritia$reload(CallbackInfo ci) { avaritia$shield=null; avaritia$chest=null; }
}
