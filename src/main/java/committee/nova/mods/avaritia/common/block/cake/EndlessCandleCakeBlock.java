package committee.nova.mods.avaritia.common.block.cake;

import com.google.common.collect.ImmutableList;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2023/12/31 11:38
 * @Description:
 */

public class EndlessCandleCakeBlock extends CandleCakeBlock {
    private final EndlessCakeBlock cake;
    private static final Map<EndlessCakeBlock, Map<CandleBlock, EndlessCandleCakeBlock>> COMBINER = new HashMap<>();

    public EndlessCandleCakeBlock(Block candle, Properties settings) {
        super(candle, settings);
        this.cake = (EndlessCakeBlock) ModBlocks.endless_cake.get();
        if (COMBINER.containsKey(cake)) {
            COMBINER.get(cake).put((CandleBlock) candle, this);
        } else {
            Map<CandleBlock, EndlessCandleCakeBlock> candleCakeBlockMap = new HashMap<>();
            candleCakeBlockMap.put((CandleBlock) candle, this);
            COMBINER.put(cake, candleCakeBlockMap);
        }

    }

    public static BlockState getCandleCakeFromCandle(EndlessCakeBlock cake, CandleBlock candle) {
        return COMBINER.get(cake).get(candle).defaultBlockState();
    }


    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        if ((itemStack.is(Items.FLINT_AND_STEEL) || itemStack.is(Items.FIRE_CHARGE)) && isHittingCandle(hit) && !state.getValue(LIT)) {
            setLit(world, state, pos);
            if (state.getBlock() instanceof EndlessCakeBlock abstractCandleBlock) {
                var PARTICLE_OFFSETS = ImmutableList.of(new Vec3(0.5D, 1.0D, 0.5D));
                PARTICLE_OFFSETS.forEach((vec3) -> {
                    world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + vec3.x(), (double)pos.getY() + vec3.y(), (double)pos.getZ() + vec3.z(), 0.0D, 0.1F, 0.0D);
                });
            }

            world.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.sidedSuccess(world.isClientSide);

        } else {
            if (itemStack.isEmpty()) {
                if (!state.getValue(LIT)) {
                    dropResources(state, world, pos);
                    world.setBlockAndUpdate(pos, ModBlocks.endless_cake.get().defaultBlockState());
                }
            } else {
                return EndlessCakeBlock.tryEat(world, pos, player);
            }
            return InteractionResult.PASS;
        }
    }

    private static void setLit(LevelAccessor pLevel, BlockState pState, BlockPos pPos) {
        pLevel.setBlock(pPos, pState.setValue(LIT, true), 11);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new ItemStack(cake);
    }


    protected static boolean isHittingCandle(BlockHitResult hitResult) {
        return hitResult.getLocation().y - (double)hitResult.getBlockPos().getY() > 0.5D;
    }
}
