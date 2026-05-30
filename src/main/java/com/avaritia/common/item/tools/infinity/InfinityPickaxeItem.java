package com.avaritia.common.item.tools.infinity;

import com.avaritia.api.common.enchant.InitEnchantment;
import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.iface.item.IUndamageable;
import com.avaritia.api.iface.item.InitEnchantItem;
import com.avaritia.api.utils.ItemUtils;
import com.avaritia.common.entity.ImmortalItemEntity;
import com.avaritia.init.config.ModConfig;
import com.avaritia.init.registry.ModEntities;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import com.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class InfinityPickaxeItem extends Item implements InitEnchantItem, ISwitchable, IUndamageable {
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.FORTUNE, 10);

    private static final float SMASH_FALL_THRESHOLD = 1.5F;
    private static final double SMASH_RADIUS = 3.5D;

    public InfinityPickaxeItem() {
        super(new Properties()
                        .rarity(ModRarities.COSMIC.getValue())
                        .stacksTo(1)
                        .fireResistant()
                        .pickaxe(ModToolTiers.INFINITY, 0, ModToolTiers.INFINITY.speed())
        );
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
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public int getEnchantmentLevel(@NonNull ItemInstance stack, @NonNull Holder<Enchantment> enchantment) {
        return 0;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        if (isActive(stack, "infinity_pickaxe_hammer")) {
            return 8888.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 9999.0F);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        Holder<Enchantment> SILK_TOUCH =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH);
        if (player.isShiftKeyDown()) {
            switchMode(world, player, hand, "infinity_pickaxe_hammer");
            return InteractionResult.SUCCESS;
        }
        if (EnchantmentHelper.getTagEnchantmentLevel(SILK_TOUCH, stack) > 0) {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
                serverPlayer.sendSystemMessage(Component.translatable("tooltip.infinity_pickaxe.enchant_1"), true);
            ItemUtils.clearEnchants(stack, SILK_TOUCH);
        }else {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
                serverPlayer.sendSystemMessage(Component.translatable("tooltip.infinity_pickaxe.enchant_2"), true);
            stack.enchant(SILK_TOUCH, 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity victim, @NotNull LivingEntity player) {
        if (isActive(stack, "infinity_pickaxe_hammer")) {
            if (!(victim instanceof Player)) {
                int i = 10;
                victim.setDeltaMovement(-Mth.sin(player.yBodyRot * (float) Math.PI / 180.0F) * i * 0.5F, 2.0D, Mth.cos(player.yBodyRot * (float) Math.PI / 180.0F) * i * 0.5F);

            }
        }
    }


    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
        if (miningEntity instanceof ServerPlayer player && isActive(stack, "infinity_pickaxe_hammer")) {
            ToolUtils.destroyMaterialBlocks(player, pos, ModConfig.pickAxeBreakRange.get());
        }
        return false;
    }


    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.FORTUNE)) {
            return 10;
        }
        return 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }
}
