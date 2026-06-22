package committee.nova.mods.avaritia.common.block.chest;

import committee.nova.mods.avaritia.api.common.block.BaseTileEntityBlock;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.core.chest.ServerChestManager;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class InfinityChestBlock extends BaseTileEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public InfinityChestBlock() {
        super(Properties.of()
                .mapColor(MapColor.GOLD)
                .instrument(NoteBlockInstrument.BASS)
                .strength(30.0F, 1200.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 15)
                .isValidSpawn((state, getter, pos, entityType) -> false)
                .isSuffocating((state, getter, pos) -> false)
                .ignitedByLava());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return AABB;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean secondaryUse = context.isSecondaryUseActive();
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal() && secondaryUse) {
            Direction partnerFacing = this.candidatePartnerFacing(context, clickedFace.getOpposite());
            if (partnerFacing != null && partnerFacing.getAxis() != clickedFace.getAxis()) {
                direction = partnerFacing;
            }
        }
        return this.defaultBlockState().setValue(FACING, direction).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Nullable
    private Direction candidatePartnerFacing(BlockPlaceContext context, Direction direction) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().relative(direction));
        return blockstate.is(this) ? blockstate.getValue(FACING) : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new InfinityChestTile(pos, state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(placer instanceof ServerPlayer player) || hasSavedChannel(stack)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof InfinityChestTile tile) {
            tile.setOwner(player.getUUID());
            tile.setChannelId(UUID.randomUUID());
            ServerChestManager manager = ServerChestManager.getInstance();
            if (manager != null) {
                manager.tryAddChest(player, tile.getChannelID());
            }
        }
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos,
                              @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.Builder builder) {
        ItemStack stack = new ItemStack(ModBlocks.infinity_chest.get().asItem());
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof InfinityChestTile tile) {
            tile.saveToItem(stack);
        }
        return List.of(stack);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult trace) {
        if (!level.isClientSide() && !player.isSpectator()) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof InfinityChestTile chestTile) {
                if (chestTile.getOwner() == null) {
                    chestTile.setOwner(player.getUUID());
                    chestTile.setLocked(false);
                }
                NetworkHooks.openScreen((ServerPlayer) player, chestTile, buf -> {
                    buf.writeBlockPos(pos);
                    buf.writeUUID(chestTile.getOwner());
                    buf.writeBoolean(chestTile.isLocked());
                    buf.writeUtf(chestTile.getFilter(), 64);
                    buf.writeByte(chestTile.getSortType());
                    buf.writeUUID(chestTile.getChannelID());
                });
                player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getClientTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(type, ModTileEntities.infinity_chest_tile.get(), InfinityChestTile::lidAnimateTick);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType type) {
        return false;
    }

    private static boolean hasSavedChannel(ItemStack stack) {
        return stack.getTag() != null
                && stack.getTag().contains(BlockItem.BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND);
    }
}
