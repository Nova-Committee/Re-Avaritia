package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.iface.IBowTransform;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.common.entity.arrow.BurningArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * @author cnlimiter
 */
public class BlazeBowItem extends BowItem implements ISwitchable, IBowTransform, ITooltip {
    public BlazeBowItem() {
        super(ModItems.properties()
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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack pStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, "blaze_bow_burning");
            return InteractionResult.SUCCESS;
        }
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int drawTime = this.getUseDuration(stack, player) - timeLeft;
            if (drawTime < 0) {
                return false;
            }

            float VELOCITY_MULTIPLIER = 1.2F;
            float DAMAGE_MULTIPLIER = 2.0F;
            float draw = getPowerForTime(drawTime);//蓄力时间
            float powerForTime = draw * VELOCITY_MULTIPLIER;
            if (powerForTime >= 0.1D) {
                if (!level.isClientSide()) {
                    if (isActive(stack, "blaze_bow_burning")) {//灼烧模式
                        var burningBall = ModEntities.BURNING_BALL.get().create(level, EntitySpawnReason.EVENT);
                        if (burningBall != null) {
                            burningBall.setOwner(player);
                            burningBall.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                            burningBall.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.5F);
                            level.addFreshEntity(burningBall);
                            player.getCooldowns().addCooldown(stack, 20);
                        }
                    } else {
                        var abstractarrow = new BurningArrowEntity(player);
                        abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.5F);

                        if (draw == 1.0F) {
                            abstractarrow.setCritArrow(true);//蓄力满必暴击
                        }
                        double baseDamage = 2.0D * (double) DAMAGE_MULTIPLIER;
                        abstractarrow.setBaseDamage(baseDamage);

                        addEnchant(stack, level, player, abstractarrow, baseDamage);
                        level.addFreshEntity(abstractarrow);
                    }
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        isActive(stack, "blaze_bow_burning") ? SoundEvents.SNOWBALL_THROW : SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                        1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
                player.awardStat(Stats.ITEM_USED.get(this));
                return true;
            }
        }
        return false;
    }

    private void addEnchant(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity player, AbstractArrow arrowEntity, double baseDamage) {
        Holder<Enchantment> power = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER);
        Holder<Enchantment> flame = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FLAME);

        int j = EnchantmentHelper.getItemEnchantmentLevel(power, stack);//力量箭矢
        if (j > 0) {
            arrowEntity.setBaseDamage(baseDamage + (double) j * 0.5D + 0.5D);
        }

        //26.3 中冲击（PUNCH）击退由 firedFromWeapon 上的附魔自动结算，无 setKnockback API

        if (EnchantmentHelper.getItemEnchantmentLevel(flame, stack) > 0) {//火焰箭矢
            arrowEntity.igniteForSeconds(100);
        }
        stack.hurtAndBreak(1, player, player.getUsedItemHand());
        arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
    }
}
