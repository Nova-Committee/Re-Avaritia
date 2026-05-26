package com.avaritia.common.item.tools.blaze;

import com.avaritia.api.common.enchant.InitEnchantment;
import com.avaritia.api.iface.ITooltip;
import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.iface.item.InitEnchantItem;
import com.avaritia.common.entity.ball.FireBallEntity;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModEntities;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import com.avaritia.init.registry.modes.ToolMode;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazeSwordItem extends Item implements ITooltip, ISwitchable, InitEnchantItem {
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.FIRE_ASPECT, 10);

    public BlazeSwordItem() {
        super(new Properties()
                        .component(ModDataComponents.TOOL_MODE, ToolMode.DEFAULT)
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .sword(ModToolTiers.BLAZE, 0, ModToolTiers.BLAZE.getSpeed())
        );
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.FIRE_ASPECT)) {
            return 10;
        }
        return 0;
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isCrouching()) {
                switchMode(level, player, hand, "fire_ball");
                return InteractionResult.SUCCESS;
            }
            if (isActive(stack, "fire_ball")) {
                FireBallEntity fireBallEntity = ModEntities.FIRE_BALL.get().create(level);
                if (fireBallEntity != null) {
                    fireBallEntity.setOwner(player);
                    fireBallEntity.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                    fireBallEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                    level.playSound(player, player.getOnPos(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
                    level.addFreshEntity(fireBallEntity);
                    player.getCooldowns().addCooldown(heldItem.getItem(), 40);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player player, Entity entity) {
        entity.setInvulnerable(false);
        return super.onLeftClickEntity(stack, player, entity);
    }
}
