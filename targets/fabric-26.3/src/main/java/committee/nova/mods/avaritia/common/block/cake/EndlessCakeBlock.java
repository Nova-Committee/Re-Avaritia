package committee.nova.mods.avaritia.common.block.cake;

import committee.nova.mods.avaritia.api.common.block.BaseBlock;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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

/**
 * @Project: Avaritia-forge
 * @author cnlimiter
 * @CreateTime: 2023/12/31 11:28
 * @Description:
 */

public class EndlessCakeBlock extends BaseBlock {
    public static final VoxelShape CAKE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D);

    public EndlessCakeBlock() {
        super(ModBlocks.properties().forceSolidOn().strength(0.5F).sound(SoundType.WOOL).pushReaction(PushReaction.POPPED));
    }

    protected static InteractionResult tryEat(Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!pPlayer.canEat(true)) {
            return InteractionResult.PASS;
        } else {
            pPlayer.awardStat(Stats.EAT_CAKE_SLICE);
            pPlayer.getFoodData().eat(2, 0.1F);
            pPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));
            pPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
            pPlayer.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 6000, 0));
            pPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
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
//        if (stack.is(ItemTags.CANDLES) && state.is(ModBlocks.endless_cake.get())) {
//            Block block = Block.byItem(stack.getItem());
//            if (block instanceof CandleBlock candleBlock) {
//                if (!player.isCreative()) {
//                    stack.shrink(1);
//                }
//
//                level.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
//                level.setBlockAndUpdate(pos, EndlessCandleCakeBlock.getCandleCakeFromCandle(candleBlock));
//                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
//                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
//                return InteractionResult.SUCCESS;
//            }
//        }

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
    public @NotNull BlockState updateShape(@NotNull BlockState pState, @NotNull LevelReader pLevel, @NotNull ScheduledTickAccess scheduledTickAccess,
                                           @NotNull BlockPos pCurrentPos, @NotNull Direction pFacing, @NotNull BlockPos pFacingPos,
                                           @NotNull BlockState pFacingState, @NotNull RandomSource random) {
        return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pLevel, scheduledTickAccess, pCurrentPos, pFacing, pFacingPos, pFacingState, random);
    }


    @Override
    public int getAnalogOutputSignal(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Direction direction) {
        return 14;
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState pState) {
        return true;
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState pState, @NotNull PathComputationType pType) {
        return true;
    }

}
