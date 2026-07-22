package committee.nova.mods.avaritia.common.block.chest;

import committee.nova.mods.avaritia.api.common.block.BaseTileEntityBlock;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Tesseract terminal block; the open center accepts item entities and automation. */
public class TesseractBlock extends BaseTileEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape FRAME = Shapes.or(
            Block.box(0, 0, 0, 16, 3, 3), Block.box(0, 0, 13, 16, 3, 16),
            Block.box(0, 0, 3, 3, 3, 13), Block.box(13, 0, 3, 16, 3, 13),
            Block.box(0, 13, 0, 16, 16, 3), Block.box(0, 13, 13, 16, 16, 16),
            Block.box(0, 13, 3, 3, 16, 13), Block.box(13, 13, 3, 16, 16, 13),
            Block.box(0, 3, 0, 3, 13, 3), Block.box(13, 3, 0, 16, 13, 3),
            Block.box(13, 3, 13, 16, 13, 16), Block.box(0, 3, 13, 3, 13, 16));

    public TesseractBlock() {
        super(ModBlocks.properties().mapColor(MapColor.GOLD).instrument(NoteBlockInstrument.BASS)
                .strength(30.0F, 1_200.0F).sound(SoundType.GLASS)
                .lightLevel(state -> 15).isValidSpawn((state, getter, pos, type) -> false)
                .isSuffocating((state, getter, pos) -> false));
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, true).setValue(SOUTH, true)
                .setValue(WEST, true).setValue(EAST, true)
                .setValue(UP, true).setValue(DOWN, true)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN, WATERLOGGED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isSpectator()) return InteractionResult.CONSUME;
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof TesseractTile tile) {
            if (tile.getOwner() == null) {
                tile.setOwner(player.getUUID());
                tile.setLocked(false);
            }
            if (!tile.canPlayerModify(player)) {
                player.sendOverlayMessage(Component.translatable("gui.avaritia.noPermission.tip3"));
            } else if (tile.getChannelInfo() == null) {
                tile.openSelector(serverPlayer);
            } else {
                tile.openMainMenu(serverPlayer);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WATERLOGGED,
                context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TesseractTile(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer != null && !stack.has(DataComponents.BLOCK_ENTITY_DATA)
                && level.getBlockEntity(pos) instanceof TesseractTile tile) {
            tile.setOwner(placer.getUUID());
            tile.setLocked(false);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack stack = new ItemStack(this);
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TesseractTile tile) {
            tile.saveToItem(stack, builder.getLevel().registryAccess());
            stack.applyComponents(tile.collectComponents());
        }
        return List.of(stack);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos,
                                                @NotNull BlockState state, boolean includeData,
                                                @NotNull Player player) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData, player);
        if (level.getBlockEntity(pos) instanceof TesseractTile tile) {
            tile.saveToItem(stack, level.registryAccess());
            stack.applyComponents(tile.collectComponents());
        }
        return stack;
    }

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> type) {
        return createTicker(type, ModTileEntities.TESSERACT_TILE.get(), TesseractTile::tick);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean canTriggerEffects) {
        if (!level.isClientSide() && entity instanceof ItemEntity item
                && level.getBlockEntity(pos) instanceof TesseractTile tile) {
            tile.inhaleItem(item);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos currentPos, Direction facing, BlockPos facingPos,
                                     BlockState facingState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override public FluidState getFluidState(BlockState state) { return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.block(); }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return FRAME; }
    @Override public VoxelShape getOcclusionShape(BlockState state) { return FRAME; }
    @Override public boolean useShapeForLightOcclusion(BlockState state) { return true; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
