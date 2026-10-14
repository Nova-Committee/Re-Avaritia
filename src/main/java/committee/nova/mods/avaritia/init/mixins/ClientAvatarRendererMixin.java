package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 第三人称 CROSSBOW_HOLD（已装填持弩姿态）分支硬编码 is(Items.CROSSBOW)。
 * Neo 版用 IClientItemExtensions.getArmPose 补偿；Fabric 侧用本重定向达到同等效果。
 * CROSSBOW_CHARGE 分支本身按 getUseAnimation()==CROSSBOW 判断，物品无关，无需处理。
 */
@Mixin(AvatarRenderer.class)
public abstract class ClientAvatarRendererMixin {
    @Redirect(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;->is(Ljava/lang/Object;)Z"))
    private static boolean avaritia$crossbowHoldIdentity(ItemStack stack, Object item) {
        if (item == Items.CROSSBOW) {
            return stack.getItem() instanceof CrossbowItem;
        }
        return stack.is((Item) item);
    }
}
