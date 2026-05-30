package com.avaritia.common.item.tools.infinity;

import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.iface.item.IUndamageable;
import com.avaritia.common.entity.ImmortalItemEntity;
import com.avaritia.init.config.ModConfig;
import com.avaritia.init.registry.ModEntities;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import com.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 16:33
 * Version: 1.0
 */
public class InfinityShovelItem extends ShovelItem implements ISwitchable, IUndamageable {

    public InfinityShovelItem() {
        super(ModToolTiers.INFINITY,0, ModToolTiers.INFINITY.speed(),
                new Properties()
                        .rarity(ModRarities.COSMIC.getValue())
                        .stacksTo(1)
                        .fireResistant()
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
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        if (isActive(stack, "infinity_shovel_destroyer")) {
            return 5.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 6.0f);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(pLevel, player, hand, "infinity_shovel_destroyer");
            return InteractionResult.SUCCESS;
        }

        //右键发射发射终望珍珠,冷却20s
        if (isActive(stack, "infinity_shovel_destroyer")) {
            ToolUtils.pearlAttack(player, ModItems.endest_pearl.get().getDefaultInstance(), pLevel);//
            player.getCooldowns().addCooldown(stack, 200);
        }

        return super.use(pLevel, player, hand);
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
        if (miningEntity instanceof ServerPlayer player && isActive(stack, "infinity_shovel_destroyer")) {
            ToolUtils.destroyShovelBlocks(player, pos, ModConfig.shovelBreakRange.get());
        }
        return false;
    }
}
