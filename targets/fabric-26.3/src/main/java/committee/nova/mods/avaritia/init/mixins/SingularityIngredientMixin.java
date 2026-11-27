package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.crafting.recipe.NbtIngredientAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public abstract class SingularityIngredientMixin implements NbtIngredientAccess {
    @Unique private ItemStack avaritia$nbtStack;
    @Unique private Ingredient[] avaritia$alternatives;

    @Override
    public void avaritia$setNbtStack(ItemStack stack) { this.avaritia$nbtStack = stack; }
    @Override
    public void avaritia$setAlternatives(Ingredient[] alternatives) { this.avaritia$alternatives = alternatives; }

    // 数据包 JSON/网络编解码已由 Fabric 内建的 fabric:components 配料承担
    // （RecipeCodecs 会把旧式 avaritia:nbt 配料转换过去）；本 mixin 仅保留对
    // NbtIngredients.of(...) 程序化配料的运行时匹配。
    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void avaritia$matchNbt(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (this.avaritia$nbtStack != null) {
            callback.setReturnValue(stack != null && ItemStack.isSameItemSameComponents(this.avaritia$nbtStack, stack));
        } else if (this.avaritia$alternatives != null) {
            for (Ingredient alternative : this.avaritia$alternatives) {
                if (alternative.test(stack)) {
                    callback.setReturnValue(true);
                    return;
                }
            }
            callback.setReturnValue(false);
        }
    }
}
