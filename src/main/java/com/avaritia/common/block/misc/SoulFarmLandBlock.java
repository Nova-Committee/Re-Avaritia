package com.avaritia.common.block.misc;

import com.avaritia.api.common.block.BaseBlock;
import com.avaritia.init.config.ModConfig;
import com.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/5 00:55
 * @Description:
 */
public class SoulFarmLandBlock extends BaseBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public SoulFarmLandBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(0.6F)
                .randomTicks()
                .sound(SoundType.SOUL_SOIL));
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState pState, @NotNull LevelReader pLevel, @NotNull ScheduledTickAccess scheduledTickAccess, @NotNull BlockPos pCurrentPos, @NotNull Direction pFacing, @NotNull BlockPos pFacingPos, @NotNull BlockState pFacingState, @NotNull RandomSource random) {
        if (pFacing == Direction.UP && !pState.canSurvive(pLevel, pCurrentPos)) {
            scheduledTickAccess.scheduleTick(pCurrentPos, this, 1);
        }

        return super.updateShape(pState, pLevel, scheduledTickAccess, pCurrentPos, pFacing, pFacingPos, pFacingState, random);
    }

    @Override
    public boolean useShapeForLightOcclusion(@NotNull BlockState pState) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    @Nullable
    public BlockState getToolModifiedState(@NotNull BlockState state, @NotNull UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility.equals(ItemAbilities.HOE_TILL) && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
            return ModBlocks.soul_farmland.get().defaultBlockState();
        }
        return null;
    }


    @Override
    public @NotNull TriState canSustainPlant(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Direction facing, BlockState plantable) {
        return TriState.TRUE;
    }


    @Override
    public void randomTick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource rand) {
        if (!level.isClientSide()) {
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            Block aboveBlock = aboveState.getBlock();

            if (aboveBlock instanceof TallFlowerBlock) {
                return;
            }

            if (ModConfig.growthSoulFarmland.get() == 0.0) {
                return;
            }

            if (aboveBlock instanceof SugarCaneBlock || level.getBlockState(pos.above(2)).getBlock() instanceof SugarCaneBlock
                    && rand.nextFloat() <= ModConfig.growthSoulFarmland.get()
            ){
                if (aboveState.getValue(SugarCaneBlock.AGE) < 11) {
                    level.setBlock(abovePos, aboveState.setValue(SugarCaneBlock.AGE, aboveState.getValue(SugarCaneBlock.AGE) + 5), 4);
                }
            }


            if (aboveBlock instanceof BonemealableBlock growable
                    && rand.nextFloat() <= ModConfig.growthSoulFarmland.get()
            ) {
                if (growable.isValidBonemealTarget(level, pos.above(), aboveState)) {
                    growable.performBonemeal(level, rand, pos.above(), aboveState);
                    level.levelEvent(2005, pos.above(), 0);
                }
            }
        }
    }
}
