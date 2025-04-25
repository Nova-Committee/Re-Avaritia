package committee.nova.mods.avaritia.common.block.misc.infinity_clock;

import committee.nova.mods.avaritia.api.common.block.BaseTileEntityBlock;
import committee.nova.mods.avaritia.common.tile.InfinityClockTile;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/4/4 00:35
 * @Description:
 */
public class InfinityClockBlock extends BaseTileEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;;
    //public static final EnumProperty<ClockSpeed> SPEED = EnumProperty.create("speed", ClockSpeed.class);
    protected static final VoxelShape AABB = Block.box(2.0D, 0.0D, 6.0D, 16.0D, 12.0D, 10.0D);
    public InfinityClockBlock() {
        super(Properties.of()
                .mapColor(MapColor.GOLD)
                .instrument(NoteBlockInstrument.BASS)
                .strength(30.0F, 1200.0F)
                .sound(SoundType.GLASS));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                //.setValue(SPEED, ClockSpeed.COMMON)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING
                //, SPEED
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new InfinityClockTile(blockPos, blockState);
    }

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(type, ModTileEntities.infinity_clock_tile.get(), InfinityClockTile::serverTick);
    }

    @Override
    public VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return AABB;
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult trace) {
        if (!level.isClientSide()) {
            var tile = level.getBlockEntity(pos);

            if (tile instanceof InfinityClockTile clockTile) {
                //NetworkHooks.openScreen((ServerPlayer) player, clockTile, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }


}
