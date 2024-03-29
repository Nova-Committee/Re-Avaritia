package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.math.RayTracer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:07
 * Version: 1.0
 */
public class BowInfinityItem extends BowItem {
    public BowInfinityItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModItems.COSMIC_RARITY)
                .fireResistant()
        );
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeHurtBy(@NotNull DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.getAge() >= 0) {
            entity.setExtendedLifetime();
        }
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack p_41456_) {
        return true;
    }
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
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
        //无限箭矢
        var itemstack = player.getItemInHand(hand);
        InteractionResultHolder<ItemStack> ret = EventHooks.onArrowNock(itemstack, level, player, hand, true);
        if (ret != null) return ret;
        if (player.isCrouching()) {
            CompoundTag tags = itemstack.getOrCreateTag();
            tags.putBoolean("tracer", !tags.getBoolean("tracer"));
            player.setMainArm(HumanoidArm.RIGHT);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.success(itemstack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (!level.isClientSide) {
            if (entity instanceof Player player) {
                AbstractArrow arrowEntity;
                ItemStack ammoStack = player.getProjectile(stack);
                int drawTime = this.getUseDuration(stack) - timeLeft;
                drawTime = EventHooks.onArrowLoose(stack, level, player, drawTime, true);
                if (drawTime < 0) {
                    return;
                }
                float VELOCITY_MULTIPLIER = 1.2F;
                float DAMAGE_MULTIPLIER = 5000.0F;
                float draw = getPowerForTime(drawTime);
                float powerForTime = draw * VELOCITY_MULTIPLIER;
                if (ammoStack.isEmpty()) {
                    ammoStack = new ItemStack(Items.ARROW);
                }
                if (stack.getOrCreateTag().getBoolean("tracer")) {

                    if ((double)powerForTime >= 0.1D) {
                        ArrowItem arrowitem = (ArrowItem)(ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
                        arrowEntity = this.customArrow(arrowitem.createArrow(level, ammoStack, entity), ammoStack);
                        if (arrowEntity instanceof Arrow arrow2) {
                            arrow2.setEffectsFromItem(ammoStack);
                        } else if (arrowEntity instanceof TraceArrowEntity infinityArrow) {
                            infinityArrow.setEffectsFromItem(ammoStack);
                        }

                        if (draw == 1.0F) {
                            arrowEntity.setCritArrow(true);
                        }

                        arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() * (double)DAMAGE_MULTIPLIER);
                        addEnchant(stack, level, entity, arrowEntity, powerForTime);

                    }

                }
                else {
                    arrowEntity = HeavenArrowEntity.create(level, entity);
                    arrowEntity.setPos(entity.getX() - 0.3, entity.getEyeY() - 0.1, entity.getZ());
                    arrowEntity.setCritArrow(true);
                    addEnchant(stack, level, entity, arrowEntity, powerForTime);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
                player.awardStat(Stats.ITEM_USED.get(this));

            }
        }
    }

    private void addEnchant(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, AbstractArrow arrowEntity, float powerForTime) {
        arrowEntity.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, powerForTime * 3.0F, 0.01F);
        int j = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (j > 0) {
            arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + (double)j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (k > 0) {
            arrowEntity.setKnockback(k);
        }

        if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrowEntity.setSecondsOnFire(100);
        }
        arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        level.addFreshEntity(arrowEntity);
    }

}
