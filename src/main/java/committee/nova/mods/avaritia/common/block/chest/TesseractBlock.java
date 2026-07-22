package committee.nova.mods.avaritia.common.block.chest;

import com.google.common.collect.ImmutableMap;
import committee.nova.mods.avaritia.api.common.block.BaseTileEntityBlock;
import committee.nova.mods.avaritia.common.menu.provider.ChannelSelectMenuProvider;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TesseractBlock extends BaseTileEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private final ImmutableMap<BlockState, VoxelShape> shapesCache;

    public TesseractBlock() {
        super(Properties.of().mapColor(MapColor.GOLD).instrument(NoteBlockInstrument.BASS)
                .strength(30.0F, 1_200.0F).sound(SoundType.GLASS)
                .lightLevel(TesseractBlock::getLightLevel)
                .isValidSpawn((state, getter, pos, entityType) -> false)
                .isSuffocating((state, getter, pos) -> false).ignitedByLava());
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, true).setValue(SOUTH, true)
                .setValue(WEST, true).setValue(EAST, true)
                .setValue(UP, true).setValue(DOWN, true)
                .setValue(WATERLOGGED, false));
        shapesCache = getShapeForEachState(TesseractBlock::calculateShape);
    }

    private static VoxelShape calculateShape(BlockState state) {
        VoxelShape shape = Shapes.or(
                Block.box(0, 0, 0, 16, 3, 3), Block.box(0, 0, 13, 16, 3, 16),
                Block.box(0, 0, 3, 3, 3, 13), Block.box(13, 0, 3, 16, 3, 13),
                Block.box(0, 13, 0, 16, 16, 3), Block.box(0, 13, 13, 16, 16, 16),
                Block.box(0, 13, 3, 3, 16, 13), Block.box(13, 13, 3, 16, 16, 13),
                Block.box(0, 3, 0, 3, 13, 3), Block.box(13, 3, 0, 16, 13, 3),
                Block.box(13, 3, 13, 16, 13, 16), Block.box(0, 3, 13, 3, 13, 16));
        if (!state.getValue(NORTH)) shape = Shapes.or(shape, Block.box(3, 3, 0, 13, 13, 1));
        if (!state.getValue(SOUTH)) shape = Shapes.or(shape, Block.box(3, 3, 14, 13, 13, 16));
        if (!state.getValue(WEST)) shape = Shapes.or(shape, Block.box(0, 3, 3, 1, 13, 13));
        if (!state.getValue(EAST)) shape = Shapes.or(shape, Block.box(14, 3, 3, 16, 13, 13));
        if (!state.getValue(DOWN)) shape = Shapes.or(shape, Block.box(3, 0, 3, 13, 1, 13));
        if (!state.getValue(UP)) shape = Shapes.or(shape, Block.box(3, 14, 3, 13, 16, 13));
        return shape;
    }

    private static int getLightLevel(BlockState state) {
        return state.getValue(NORTH) || state.getValue(SOUTH) || state.getValue(WEST)
                || state.getValue(EAST) || state.getValue(DOWN) || state.getValue(UP) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                      @NotNull Player player, @NotNull BlockHitResult trace) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(pos) instanceof TesseractTile tile && player instanceof ServerPlayer serverPlayer) {
            if (tile.getOwner() == null) {
                tile.setOwner(player.getUUID());
                tile.setLocked(false);
            }
            if (!tile.canPlayerModify(player)) {
                player.displayClientMessage(Component.translatable("gui.avaritia.noPermission.tip3"), true);
                return InteractionResult.CONSUME;
            }
            if (tile.getChannelInfo() == null) {
                serverPlayer.openMenu(new ChannelSelectMenuProvider(tile));
            } else {
                tile.openMainMenu(serverPlayer);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TesseractTile(pos, state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer != null && !stack.has(DataComponents.BLOCK_ENTITY_DATA)
                && level.getBlockEntity(pos) instanceof TesseractTile tile) {
            tile.setOwner(placer.getUUID());
            tile.setLocked(false);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack stack = new ItemStack(ModBlocks.tesseract.get());
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TesseractTile tile) {
            tile.saveToItem(stack, builder.getLevel().registryAccess());
        }
        return List.of(stack);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
        ItemStack stack = new ItemStack(ModBlocks.tesseract.get());
        if (level.getBlockEntity(pos) instanceof TesseractTile tile) {
            tile.saveToItem(stack, level.registryAccess());
        }
        return stack;
    }

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> type) {
        return createTicker(type, ModTileEntities.tesseract_tile.get(), TesseractTile::tick);
    }

    @Override
    public void entityInside(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (!level.isClientSide && entity instanceof ItemEntity item
                && level.getBlockEntity(pos) instanceof TesseractTile tile) {
            tile.inhaleItem(item);
        }
    }

    @Override public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.block(); }
    @Override public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return shapesCache.getOrDefault(state, Shapes.block()); }
    @Override public @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) { return shapesCache.getOrDefault(state, Shapes.block()); }
    @Override public boolean useShapeForLightOcclusion(BlockState state) { return true; }
    @Override public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) { return state.getFluidState().isEmpty(); }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override public FluidState getFluidState(BlockState state) { return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state); }
    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return 0; }

    @Override
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (level.getBlockEntity(pos) instanceof TesseractTile tile) {
            tile.onBlockStateChange();
        }
    }

    @Override public @NotNull RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
