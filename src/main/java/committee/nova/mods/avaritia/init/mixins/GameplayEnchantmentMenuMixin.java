package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.block.ResourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentMenu.class)
public abstract class GameplayEnchantmentMenuMixin {
    @Unique private float avaritia$additionalEnchantmentPower;

    // 26.3: the bookshelf scan lambda is the first lambda of slotsChanged; it captures the enchanted stack.
    @Inject(method = "lambda$slotsChanged$0", at = @At("HEAD"))
    private void avaritia$resetEnchantmentPower(ItemStack stack, Level level, BlockPos pos, CallbackInfo ci) {
        avaritia$additionalEnchantmentPower = 0;
    }

    @Redirect(method = "lambda$slotsChanged$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/EnchantingTableBlock;isValidBookShelf(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean avaritia$accumulateEnchantmentPower(Level level, BlockPos pos, BlockPos offset) {
        boolean valid = EnchantingTableBlock.isValidBookShelf(level, pos, offset);
        if (valid && level.getBlockState(pos.offset(offset)).getBlock() instanceof ResourceBlock resource) {
            // Vanilla already contributes one for each valid provider.
            avaritia$additionalEnchantmentPower += resource.getEnchantmentPower() - 1;
        }
        return valid;
    }

    @ModifyArg(method = "lambda$slotsChanged$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"), index = 2)
    private int avaritia$weightedEnchantmentPower(int providers) {
        return (int) (providers + avaritia$additionalEnchantmentPower);
    }
}
