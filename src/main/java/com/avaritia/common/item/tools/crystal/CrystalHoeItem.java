package com.avaritia.common.item.tools.crystal;

import com.avaritia.api.iface.ITooltip;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalHoeItem extends HoeItem implements ITooltip {

    public CrystalHoeItem() {
        super(ModToolTiers.CRYSTAL,
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .attributes(createAttributes(ModToolTiers.CRYSTAL, 0, ModToolTiers.BLAZE.getSpeed()))
        );
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        var stack = context.getItemInHand();
        var world = context.getLevel();
        var blockpos = context.getClickedPos();
        var targetBlock = world.getBlockState(blockpos).getBlock();
        var player = context.getPlayer();
        var blockstate = Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7);
        int range = 1; // 3x3 area
        var minPos = blockpos.offset(-range, 0, -range);
        var maxPos = blockpos.offset(range, 0, range);

        if (context.getClickedFace() != Direction.DOWN && world.isEmptyBlock(blockpos.above()) &&
                (targetBlock instanceof GrassBlock || targetBlock.equals(Blocks.DIRT) || targetBlock.equals(Blocks.COARSE_DIRT))) {
            if (player != null && !world.isClientSide) {
                var boxMutable = BlockPos.betweenClosed(minPos, maxPos);
                for (BlockPos pos : boxMutable) {
                    var block = world.getBlockState(pos).getBlock();
                    if (world.isEmptyBlock(pos.above()) && (block instanceof GrassBlock || block.equals(Blocks.DIRT) || block.equals(
                            Blocks.COARSE_DIRT))) {
                        world.setBlock(pos, blockstate, 11);
                    }
                }
            }
            world.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        // Handle bonemeal functionality
        var targetState = world.getBlockState(blockpos);
        if (world instanceof ServerLevel serverLevel && targetBlock instanceof BonemealableBlock growable) {
            if (growable.isValidBonemealTarget(serverLevel, blockpos, targetState) && CommonHooks.canCropGrow(serverLevel, blockpos, targetState, true)) {
                growable.performBonemeal(serverLevel, world.random, blockpos, targetState);
                serverLevel.levelEvent(2005, blockpos, 0);
                CommonHooks.fireCropGrowPost(serverLevel, blockpos, targetState);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity entity) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 取消攻击冷却
            serverPlayer.resetAttackStrengthTicker();
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
