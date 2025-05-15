package committee.nova.mods.avaritia.common.block.chest;

import com.google.common.collect.ImmutableMap;
import committee.nova.mods.avaritia.addons._channel.BlackHoleTile;
import committee.nova.mods.avaritia.addons._channel.ChannelMenuProvider;
import committee.nova.mods.avaritia.addons._channel.ClientChannelManager;
import committee.nova.mods.avaritia.api.common.block.BaseTileEntityBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 下午12:38
 * @Description:
 */
public class BlackHoleBlock extends BaseTileEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    private final ImmutableMap<BlockState, VoxelShape> shapesCache;
    public BlackHoleBlock() {
        super(Properties.of()
                .mapColor(MapColor.GOLD)
                .instrument(NoteBlockInstrument.BASS)
                .strength(30.0F, 1200.0F)
                .sound(SoundType.GLASS)
                .lightLevel(BlackHoleBlock::getLightLevel)
                .isValidSpawn((state, getter, pos, entityType) -> false)
                .isSuffocating((state, getter, pos) -> false)
                .ignitedByLava());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, Boolean.TRUE)
                .setValue(SOUTH, Boolean.TRUE)
                .setValue(WEST, Boolean.TRUE)
                .setValue(EAST, Boolean.TRUE)
                .setValue(UP, Boolean.TRUE)
                .setValue(DOWN, Boolean.TRUE)
                .setValue(WATERLOGGED, Boolean.FALSE)
        );
        this.shapesCache = this.getShapeForEachState(BlackHoleBlock::calculateShape);
    }

    private static VoxelShape calculateShape(BlockState state) {
        VoxelShape voxelshape = Shapes.or(
                Block.box(0, 0, 0, 16, 3, 3),
                Block.box(0, 0, 13, 16, 3, 16),
                Block.box(0, 0, 3, 3, 3, 13),
                Block.box(13, 0, 3, 16, 3, 13),
                Block.box(0, 13, 0, 16, 16, 3),
                Block.box(0, 13, 13, 16, 16, 16),
                Block.box(0, 13, 3, 3, 16, 13),
                Block.box(13, 13, 3, 16, 16, 13),
                Block.box(0, 3, 0, 3, 13, 3),
                Block.box(13, 3, 0, 16, 13, 3),
                Block.box(13, 3, 13, 16, 13, 16),
                Block.box(0, 3, 13, 3, 13, 16)
        );
        if (!state.getValue(NORTH)) voxelshape = Shapes.or(voxelshape, Block.box(3, 3, 0, 13, 13, 1));
        if (!state.getValue(SOUTH)) voxelshape = Shapes.or(voxelshape, Block.box(3, 3, 14, 13, 13, 16));
        if (!state.getValue(WEST)) voxelshape = Shapes.or(voxelshape, Block.box(0, 3, 3, 1, 13, 13));
        if (!state.getValue(EAST)) voxelshape = Shapes.or(voxelshape, Block.box(14, 3, 3, 16, 13, 13));
        if (!state.getValue(DOWN)) voxelshape = Shapes.or(voxelshape, Block.box(3, 0, 3, 13, 1, 13));
        if (!state.getValue(UP)) voxelshape = Shapes.or(voxelshape, Block.box(3, 14, 3, 13, 16, 13));
        return voxelshape;
    }

    private static int getLightLevel(BlockState value) {
        if (value.getValue(NORTH)
                || value.getValue(SOUTH)
                || value.getValue(WEST)
                || value.getValue(EAST)
                || value.getValue(DOWN)
                || value.getValue(UP)) return 15;
        return 0;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext context, @NotNull List<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        if (Minecraft.getInstance().player == null) return;
        if (!pStack.hasTag()) return;
        if (pStack.getTag().contains("BlockEntityTag")) {
            CompoundTag nbt = pStack.getTag().getCompound("BlockEntityTag");
            if (nbt.contains("owner")) {
                UUID selfUUID = Minecraft.getInstance().player.getUUID();
                UUID ownerUUID = nbt.getUUID("owner");
                String ownerName = ClientChannelManager.getInstance().getUserName(nbt.getUUID("owner"));
                boolean lock = nbt.getBoolean("locked");
                if (selfUUID.equals(ownerUUID)) pTooltip.add(Component.translatable("gui.avaritia.owner", "§a" + ownerName));
                else if (lock) pTooltip.add(Component.translatable("gui.avaritia.owner", "§c" + ownerName));
                else pTooltip.add(Component.translatable("gui.avaritia.owner", ownerName));
            }
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull BlockHitResult pHit) {
        if (!pLevel.isClientSide() && !pPlayer.isSpectator()) {
            var tile = pLevel.getBlockEntity(pPos);

            if (tile instanceof BlackHoleTile chestTile) {
                if (chestTile.getOwner() == null) {
                    chestTile.setOwner(pPlayer.getUUID());
                    chestTile.setLocked(false);
                }

                if (chestTile.getChannelOwner() == null || chestTile.getChannelID() < 0) {
                    chestTile.setChannelOwner(pPlayer.getUUID());
                    chestTile.setChannelId(0);
                }

                if (chestTile.getChannelInfo() == null)
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, new ChannelMenuProvider(chestTile), buf -> {
                    });
                else {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, new ChannelMenuProvider(chestTile), buf -> {
                        buf.writeBlockPos(pPos);
                        buf.writeInt(-2);
                        buf.writeUUID(chestTile.getOwner());
                        buf.writeBoolean(chestTile.isLocked());
                        buf.writeBoolean(chestTile.isCraftingMode());
                        buf.writeUtf(chestTile.getFilter(), 64);
                        buf.writeByte(chestTile.getSortType());
                        buf.writeByte(chestTile.getViewType());
                        buf.writeUUID(chestTile.getChannelOwner());
                        buf.writeInt(chestTile.getChannelID());
                    });
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        BlockPos blockpos = placeContext.getClickedPos();
        FluidState fluidstate = placeContext.getLevel().getFluidState(blockpos);
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new BlackHoleTile(pPos, pState);
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable LivingEntity pPlacer, @NotNull ItemStack pStack) {
        if (pPlacer instanceof ServerPlayer player && !pStack.getOrCreateTag().contains("BlockEntityTag")) {
            BlackHoleTile blockEntity = (BlackHoleTile) pLevel.getBlockEntity(pPos);
            if (blockEntity != null) {
                blockEntity.setOwner(pPlacer.getUUID());
            }
        }
    }

    @Override
    public void entityInside(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Entity pEntity) {
        if (pLevel.isClientSide) return;
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof BlackHoleTile blackHoleTile && pEntity instanceof ItemEntity itemEntity) {
            blackHoleTile.inhaleItem(itemEntity);
        }
    }


    @Override
    public @NotNull VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.block();
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = shapesCache.get(pState);
        if (shape != null) return shape;
        return Shapes.block();
    }

    @Override
    public @NotNull VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        VoxelShape shape = shapesCache.get(pState);
        if (shape != null) return shape;
        return Shapes.block();
    }

    @Override
    public boolean useShapeForLightOcclusion(@NotNull BlockState pState) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
        BlackHoleTile blockEntity = (BlackHoleTile) level.getBlockEntity(pos);
        if (blockEntity != null) blockEntity.onBlockStateChange();
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }


    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

}
