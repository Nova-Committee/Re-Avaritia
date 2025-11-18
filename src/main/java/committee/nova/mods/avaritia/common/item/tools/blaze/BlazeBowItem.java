package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.transform.IBowTransform;
import committee.nova.mods.avaritia.common.entity.arrow.BurningArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.world.entity.LivingEntity.getSlotForHand;
import static net.neoforged.neoforge.event.EventHooks.onArrowLoose;
import static net.neoforged.neoforge.event.EventHooks.onArrowNock;

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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        InteractionResultHolder<ItemStack> ret = onArrowNock(itemstack, level, player, hand, true);
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
            int drawTime = this.getUseDuration(stack, entity) - timeLeft;
            drawTime = onArrowLoose(stack, level, player, drawTime, true);
            if (drawTime < 0) {
                return;
            }

            float VELOCITY_MULTIPLIER = 1.2F;
            float DAMAGE_MULTIPLIER = 2.0F;
            float draw = getPowerForTime(drawTime);//蓄力时间
            float powerForTime = draw * VELOCITY_MULTIPLIER;
            if (powerForTime >= 0.1D) {
                if (!level.isClientSide) {
                    if (isActive(stack, "blaze_bow_burning")) {//灼烧模式
                        var burningBall = ModEntities.BURNING_BALL.get().create(level);
                        if (burningBall != null) {
                            burningBall.setOwner(player);
                            burningBall.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                            burningBall.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.5F);
                            level.addFreshEntity(burningBall);
                            player.getCooldowns().addCooldown(stack.getItem(), 20);
                        }
                    } else {
                        var abstractarrow = this.customArrow(new BurningArrowEntity(player), Items.ARROW.getDefaultInstance(),  stack);
                        abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.5F);

                        if (draw == 1.0F) {
                            abstractarrow.setCritArrow(true);//蓄力满必暴击
                        }
                        abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() * (double) DAMAGE_MULTIPLIER);

                        addEnchant(stack, level, player, abstractarrow, powerForTime);
                        level.addFreshEntity(abstractarrow);
                    }
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        isActive(stack, "blaze_bow_burning")? SoundEvents.SNOWBALL_THROW : SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                        1.0F, 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    private void addEnchant(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity player, AbstractArrow arrowEntity, float powerForTime) {
        Holder<Enchantment> POWER =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.POWER);
        Holder<Enchantment> FLAMING =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.FLAME);

        int j = EnchantmentHelper.getTagEnchantmentLevel(POWER, stack);//力量箭矢
        if (j > 0) {
            arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + (double) j * 0.5D + 0.5D);
        }
        if (EnchantmentHelper.getTagEnchantmentLevel(FLAMING, stack) > 0) {//火焰箭矢
            arrowEntity.setRemainingFireTicks(100);
        }
        stack.hurtAndBreak(1, player, getSlotForHand(player.getUsedItemHand()));
        arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        level.addFreshEntity(arrowEntity);
    }
}
