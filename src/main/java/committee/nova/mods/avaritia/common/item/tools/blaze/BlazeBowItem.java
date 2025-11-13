package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.iface.IBowTransform;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.common.entity.arrow.BurningArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModMobEffects;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author cnlimiter
 */
public class BlazeBowItem extends BowItem implements ITooltip, ISwitchable, IBowTransform {
    public BlazeBowItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModRarities.EPIC)
                .fireResistant());
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, level, player, hand, true);
        if (ret != null) return ret;
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, "blaze_bow_burning");
            return InteractionResultHolder.success(itemstack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.success(itemstack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int drawTime = this.getUseDuration(stack) - timeLeft;
            drawTime = ForgeEventFactory.onArrowLoose(stack, level, player, drawTime, true);
            if (drawTime < 0) {
                return;
            }

            float VELOCITY_MULTIPLIER = 1.2F;
            float DAMAGE_MULTIPLIER = 5000.0F;
            float draw = getPowerForTime(drawTime);//蓄力时间
            float powerForTime = draw * VELOCITY_MULTIPLIER;
            if (powerForTime >= 0.1D) {
                if (!level.isClientSide) {
                    AbstractArrow abstractarrow = this.createArrow(level, stack, player);
                    abstractarrow = customArrow(abstractarrow);

                    if (isActive(stack, "blaze_bow_burning")) {//灼烧模式
                        abstractarrow = this.customArrow(new BurningArrowEntity(player));
                    }

                    abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.5F);

                    if (draw == 1.0F) {
                        abstractarrow.setCritArrow(true);//蓄力满必暴击
                    }
                    abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() * (double) DAMAGE_MULTIPLIER);

                    addEnchant(stack, level, player, abstractarrow, powerForTime);
                    level.addFreshEntity(abstractarrow);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        Arrow arrow = new Arrow(level, shooter);
        arrow.setEffectsFromItem(stack);
        arrow.addEffect(new MobEffectInstance(ModMobEffects.BURNING.get()));
        return arrow;
    }

    private void addEnchant(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity player, AbstractArrow arrowEntity, float powerForTime) {
        int j = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.POWER_ARROWS, stack);//力量箭矢
        if (j > 0) {
            arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + (double) j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (k > 0) {
            arrowEntity.setKnockback(k);
        }

        if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {//火焰箭矢
            arrowEntity.setSecondsOnFire(100);
        }
        stack.hurtAndBreak(1, player, (livingEntity) -> livingEntity.broadcastBreakEvent(player.getUsedItemHand()));
        arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
    }
}
