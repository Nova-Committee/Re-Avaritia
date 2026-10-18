package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 26.3 第一人称弩动画分支硬编码 is(Items.CROSSBOW)，无尽弩作为子类被排除，
 * 导致拉弦时手臂不动而模型弦自己走。这里把身份判断放宽到所有 CrossbowItem 子类。
 */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ClientFirstPersonHandsAndItemsRendererMixin {
    @Redirect(method = "submitArmWithItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean avaritia$crossbowBranchIdentity(ItemStack stack, Object item) {
        if (item == Items.CROSSBOW) {
            return stack.getItem() instanceof CrossbowItem;
        }
        return stack.is((Item) item);
    }
}
