package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 第一人称手部选择（evaluateWhichHandsToRender / isChargedCrossbow）同样硬编码
 * is(Items.CROSSBOW)，需与渲染器同步放宽，否则已装填的无尽弩不会按弩的方式占手。
 */
@Mixin(FirstPersonHandsAndItems.class)
public abstract class ClientFirstPersonHandsAndItemsMixin {
    @Redirect(method = {"evaluateWhichHandsToRender", "isChargedCrossbow"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;->is(Ljava/lang/Object;)Z"))
    private static boolean avaritia$crossbowIdentity(ItemStack stack, Object item) {
        if (item == Items.CROSSBOW) {
            return stack.getItem() instanceof CrossbowItem;
        }
        return stack.is((Item) item);
    }
}
