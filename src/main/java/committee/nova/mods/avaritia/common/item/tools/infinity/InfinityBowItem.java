package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.common.item.iface.IItemEnchant;
import committee.nova.mods.avaritia.api.common.item.iface.mode.IItemMode;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.modes.InfinityMode;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.world.entity.LivingEntity.getSlotForHand;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:07
 * Version: 1.0
 */
public class InfinityBowItem extends BowItem implements ITooltip, IItemMode<InfinityMode>, IItemEnchant {
    private final InitEnchantment initEnchantment;
    public InfinityBowItem() {
        super(new Properties()
                .component(ModDataComponents.INFINITY_MODE, InfinityMode.DEFAULT)
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
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(@NotNull ItemStack stack) {
        return 99;
    }//附魔系数

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 1200;
    }//使用时间

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        if (player.isCrouching() && !pLevel.isClientSide) {
            changeMode(player, itemstack, hand);
            return InteractionResultHolder.success(itemstack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.success(itemstack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (!level.isClientSide) {
            if (entity instanceof Player player) {
                int drawTime = this.getUseDuration(stack, player) - timeLeft;
                drawTime = EventHooks.onArrowLoose(stack, level, player, drawTime, true);
                if (drawTime < 0) {
                    return;
                }

                float VELOCITY_MULTIPLIER = 1.2F;
                float DAMAGE_MULTIPLIER = 5000.0F;
                float draw = getPowerForTime(drawTime);//蓄力时间
                float powerForTime = draw * VELOCITY_MULTIPLIER;

                AbstractArrow arrowEntity = new HeavenArrowEntity(player);

                if (getMode(stack).equals(InfinityMode.RANGE)) {//追踪模式
                    if ((double) powerForTime >= 0.1D) {
                        arrowEntity = new TraceArrowEntity(player);
                    }
                }

                arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 0.01F);
                if (draw == 1.0F) {
                    arrowEntity.setCritArrow(true);//蓄力满必暴击
                }
                arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() * (double) DAMAGE_MULTIPLIER);
                addEnchant(stack, level, player, arrowEntity, powerForTime);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
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

    @Override
    public DataComponentType<InfinityMode> getDataComponentType() {
        return ModDataComponents.INFINITY_MODE.get();
    }

    @Override
    public InfinityMode getDefaultMode() {
        return InfinityMode.DEFAULT;
    }
}
