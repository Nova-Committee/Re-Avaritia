package committee.nova.mods.avaritia.init.mixins;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HumanoidArmorLayer.class)
public abstract class ClientArmorLayerMixin {
 @Inject(method="renderArmorPiece",at=@At("HEAD"),cancellable=true)
 private void avaritia$cosmicArmor(PoseStack pose,MultiBufferSource buffers,LivingEntity entity,EquipmentSlot slot,int light,HumanoidModel<?> model,CallbackInfo ci) {
  if(entity instanceof net.minecraft.world.entity.player.Player && entity.getItemBySlot(slot).getItem() instanceof InfinityArmorItem)ci.cancel();
 }
}
