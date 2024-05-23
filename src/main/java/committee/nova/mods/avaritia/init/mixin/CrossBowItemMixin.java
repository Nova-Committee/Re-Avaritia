package committee.nova.mods.avaritia.init.mixin;

import committee.nova.mods.avaritia.api.init.event.ModEventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BowItemMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午3:51
 */
@Mixin(CrossbowItem.class)
public abstract class CrossBowItemMixin extends Item {
    public CrossBowItemMixin(Properties properties) {
        super(properties);
    }


    @Inject(
            method = "performShooting",
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void performShooting1(Level level, LivingEntity pShooter, InteractionHand interactionHand, ItemStack pCrossbowStack, float f, float g, CallbackInfo ci) {
        if (pShooter instanceof Player player && ModEventFactory.onArrowLoose(pCrossbowStack, pShooter.level(), player, 1, true) < 0) ci.cancel();
    }
}
