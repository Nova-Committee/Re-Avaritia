package com.avaritia.common.block.cake;

import com.avaritia.api.common.block.BaseBlock;
import com.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2023/12/31 11:28
 * @Description:
 */

public class EndlessCakeBlock extends BaseBlock {
    public static final VoxelShape CAKE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D);

    public EndlessCakeBlock() {
        super(ModBlocks.properties().forceSolidOn().strength(0.5F).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));
    }

    protected static InteractionResult tryEat(LevelAccessor pLevel, BlockPos pPos, Player pPlayer) {
        if (!pPlayer.canEat(true)) {
            return InteractionResult.PASS;
        } else {
            pPlayer.awardStat(Stats.EAT_CAKE_SLICE);
            pPlayer.getFoodData().eat(2, 0.1F);

            if (!pLevel.isClientSide()) {
                pPlayer.removeEffect(MobEffects.POISON);
                pPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
                pPlayer.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 6000, 0));
                pPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
                pPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));

                // 添加黑色药水粒子效果
                if (pLevel instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.WITCH,
                            pPos.getX() + 0.5D,
                            pPos.getY() + 1.0D,
                            pPos.getZ() + 0.5D,
                            10,
                            0.5D, 0.5D, 0.5D,
                            0.1D
                    );
                }
            }

            pLevel.gameEvent(pPlayer, GameEvent.EAT, pPos);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return CAKE_SHAPE;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            if (tryEat(level, pos, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }

            if (stack.isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }

        return tryEat(level, pos, player);
    }

    @Override
    protected @NonNull BlockState updateShape(@NonNull BlockState state,@NonNull LevelReader level,
                                              @NonNull ScheduledTickAccess ticks,@NonNull BlockPos pos, @NonNull Direction directionToNeighbour,
                                              @NonNull BlockPos neighbourPos, @NonNull BlockState neighbourState, @NonNull RandomSource random) {
        return directionToNeighbour == Direction.DOWN && !state.canSurvive(level, neighbourPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NonNull Direction direction) {
        return 14;
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState pState) {
        return true;
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState pState, @NotNull PathComputationType pType) {
        return true;
    }
}
