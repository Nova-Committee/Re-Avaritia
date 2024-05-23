package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:07
 * Version: 1.0
 */
public class InfinityCrossBowItem extends CrossbowItem {
    public InfinityCrossBowItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModItems.COSMIC_RARITY)
                .fireResistant()
        );
    }

//    @Override
//    public boolean isDamageable(ItemStack stack) {
//        return false;
//    }

    @Override
    public boolean canBeHurtBy(@NotNull DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

//    @Override
//    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
//        if (entity.getAge() >= 0) {
//            entity.setExtendedLifetime();
//        }
//        return super.onEntityItemUpdate(stack, entity);
//    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }
    @Override
    public int getEnchantmentValue() {
        return 99;
    }
    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 1200;
    }
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onArrowNock(itemstack, level, player, hand, true);
        if (ret != null) return ret;
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
            float draw = getPowerForTime(drawTime, stack);//蓄力时间
            float powerForTime = draw * VELOCITY_MULTIPLIER;
            if (!level.isClientSide) {
                ItemStack ammoStack = player.getProjectile(stack).isEmpty() ? new ItemStack(Items.ARROW) : player.getProjectile(stack);//无限箭矢

                AbstractArrow arrowEntity;
                if (stack.getOrCreateTag().getBoolean("tracer")) {//追踪模式
                    if ((double)powerForTime >= 0.1D) {
                        ArrowItem arrowitem = (ammoStack.getItem() instanceof ArrowItem arrowItem ? arrowItem : (ArrowItem) Items.ARROW);
                        arrowEntity = this.customTraceArrow(arrowitem.createArrow(level, ammoStack, player));
                        if (arrowEntity instanceof Arrow arrow2) {
                            arrow2.setEffectsFromItem(ammoStack);
                        } else if (arrowEntity instanceof TraceArrowEntity infinityArrow) {
                            infinityArrow.setEffectsFromItem(ammoStack);
                        }

                        arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.01F);

                        if (draw == 1.0F) {
                            arrowEntity.setCritArrow(true);//蓄力满必暴击
                        }

                        arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() * (double)DAMAGE_MULTIPLIER);
                        addEnchant(stack, level, player, arrowEntity, powerForTime);

                    }

                }
                else {
                    arrowEntity = HeavenArrowEntity.create(level, player);
                    arrowEntity.setPos(player.getX() - 0.3, player.getEyeY() - 0.1, player.getZ());
                    arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.01F);
                    if (draw == 1.0F) {
                        arrowEntity.setCritArrow(true);//蓄力满必暴击
                    }
                    addEnchant(stack, level, player, arrowEntity, powerForTime);
                }
            }
            level.playSound(null, entity.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    private void addEnchant(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity player, AbstractArrow arrowEntity, float powerForTime) {
        int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (j > 0) {
            arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + (double)j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (k > 0) {
            arrowEntity.setKnockback(k);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrowEntity.setSecondsOnFire(100);
        }
        stack.hurtAndBreak(1, player, (livingEntity) -> livingEntity.broadcastBreakEvent(player.getUsedItemHand()));
        arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        level.addFreshEntity(arrowEntity);
    }

    public @NotNull AbstractArrow customTraceArrow(AbstractArrow arrow) {
        if (arrow.getType() != EntityType.ARROW && arrow.getType() != EntityType.SPECTRAL_ARROW) {
            return arrow;
        } else {
            Entity owner = arrow.getOwner();
            if (!(owner instanceof LivingEntity)) {
                return new TraceArrowEntity(ModEntities.TRACE_ARROW.get(), arrow.level());
            } else {
                TraceArrowEntity newArrow = new TraceArrowEntity(arrow.level(), (LivingEntity)arrow.getOwner());
                if (arrow instanceof SpectralArrow spectralArrow) {
                    newArrow.setSpectral(spectralArrow.duration);
                }
                return newArrow;
            }
        }
    }

    private static float getPowerForTime(int pUseTime, ItemStack pCrossbowStack) {
        float f = (float)pUseTime / (float)getChargeDuration(pCrossbowStack);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

}
