package com.avaritia.common.item.tools.infinity;

import com.avaritia.api.common.enchant.InitEnchantment;
import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.iface.item.IUndamageable;
import com.avaritia.api.iface.item.InitEnchantItem;
import com.avaritia.api.iface.transform.IBowTransform;
import com.avaritia.common.entity.ImmortalItemEntity;
import com.avaritia.common.entity.arrow.HeavenArrowEntity;
import com.avaritia.common.entity.arrow.TraceArrowEntity;
import com.avaritia.init.registry.ModEntityTypes;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static net.neoforged.neoforge.event.EventHooks.onArrowLoose;
import static net.neoforged.neoforge.event.EventHooks.onArrowNock;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:07
 * Version: 1.0
 */
public class InfinityBowItem extends BowItem implements ISwitchable, InitEnchantItem, IUndamageable, IBowTransform {
    private final InitEnchantment initEnchantment;
    public InfinityBowItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModRarities.COSMIC.getValue())
                .fireResistant()
        );
        this.initEnchantment = new InitEnchantment(Enchantments.INFINITY, 10);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(@NotNull ItemStack stack, ItemEntity entity) {
        if (entity.getAge() >= 0) {
            entity.setExtendedLifetime();
        }
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public int getEnchantmentLevel(@NonNull ItemInstance stack, @NonNull Holder<Enchantment> enchantment) {
       return 99;
    }//附魔系数

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 1200;
    }//使用时间

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack pStack) {
        return ItemUseAnimation.BOW;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return this.initEnchantment.getLevel(enchantment);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        InteractionResult ret = onArrowNock(itemstack, level, player, hand, true);
        if (ret != null) return ret;
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, "infinity_bow_tracer");
            return InteractionResult.SUCCESS;
        }
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (!level.isClientSide()) {
            if (entity instanceof Player player) {
                int drawTime = this.getUseDuration(stack, player) - timeLeft;
                drawTime = onArrowLoose(stack, level, player, drawTime, true);
                if (drawTime < 0) {
                    return false;
                }

                float VELOCITY_MULTIPLIER = 1.2F;
                float DAMAGE_MULTIPLIER = 5000.0F;
                float draw = getPowerForTime(drawTime);//蓄力时间
                float powerForTime = draw * VELOCITY_MULTIPLIER;

                AbstractArrow arrowEntity = new HeavenArrowEntity(player);

                if (isActive(stack, "infinity_bow_tracer")) {//追踪模式
                    if ((double) powerForTime >= 0.1D) {
                        arrowEntity = new TraceArrowEntity(player);
                    }
                }

                arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.01F);
                if (draw == 1.0F) {
                    arrowEntity.setCritArrow(true);//蓄力满必暴击
                }
                double baseDamage = 2.0D * (double) DAMAGE_MULTIPLIER;
                arrowEntity.setBaseDamage(baseDamage);
                addEnchant(stack, level, player, arrowEntity, powerForTime, baseDamage);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
                player.awardStat(Stats.ITEM_USED.get(this));
                return true;
            }
        }
        return false;
    }

    private void addEnchant(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity player, AbstractArrow arrowEntity, float powerForTime, double baseDamage) {
        Holder<Enchantment> POWER =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.POWER);
        Holder<Enchantment> FLAMING =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.FLAME);

        int j = EnchantmentHelper.getTagEnchantmentLevel(POWER, stack);//力量箭矢
        if (j > 0) {
            arrowEntity.setBaseDamage(baseDamage + (double) j * 0.5D + 0.5D);
        }
        if (EnchantmentHelper.getTagEnchantmentLevel(FLAMING, stack) > 0) {//火焰箭矢
            arrowEntity.setRemainingFireTicks(100);
        }
        stack.hurtAndBreak(1, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        level.addFreshEntity(arrowEntity);
    }
}
