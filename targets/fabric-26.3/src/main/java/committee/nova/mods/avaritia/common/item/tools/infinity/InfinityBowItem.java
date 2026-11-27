package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.IBowTransform;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemInstance;
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
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 20:07
 * Version: 1.0
 */
public class InfinityBowItem extends BowItem implements ISwitchable, InitEnchantItem, IUndamageable, IBowTransform {

    public InfinityBowItem() {
        super(ModItems.properties()
                .stacksTo(1)
                .rarity(ModRarities.COSMIC)
                .fireResistant()
        );
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 1200;
    }//使用时间

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack pStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.is(Enchantments.INFINITY) ? 10 : 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        var registries = context.registries();
        if (registries != null) {
            Holder<Enchantment> infinity = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY);
            tooltipComponents.accept(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(infinity, 10)).build());
        }
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, "infinity_bow_tracer");
            return InteractionResult.SUCCESS;
        }
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return false;
        }
        int drawTime = this.getUseDuration(stack, player) - timeLeft;
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
        //26.3 中 AbstractArrow#baseDamage 为私有，默认值固定为 2.0
        double baseDamage = 2.0D * (double) DAMAGE_MULTIPLIER;
        arrowEntity.setBaseDamage(baseDamage);
        addEnchant(stack, level, player, arrowEntity, baseDamage);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
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
        level.addFreshEntity(arrowEntity);
    }
}
