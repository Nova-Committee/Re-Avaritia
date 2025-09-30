package committee.nova.mods.avaritia.common.block.chest;

import committee.nova.mods.avaritia.api.common.block.BaseTileEntityBlock;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.core.chest.ServerChestManager;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.util.StorageUtils;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * @author: cnlimiter
 */
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
                .lightLevel((b) -> 15)
                .isValidSpawn((state, getter, pos, entityType) -> false)
                .isSuffocating((state, getter, pos) -> false)
                .ignitedByLava()

        );
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
        boolean flag = context.isSecondaryUseActive();
        Direction direction1 = context.getClickedFace();
        if (direction1.getAxis().isHorizontal() && flag) {
            Direction direction2 = this.candidatePartnerFacing(context, direction1.getOpposite());
            if (direction2 != null && direction2.getAxis() != direction1.getAxis()) {
                direction = direction2;
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
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable BlockGetter pLevel, @NotNull List<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        if (Minecraft.getInstance().player == null) return;
        if (!pStack.hasTag()) return;
        if (pStack.getTag().contains("BlockEntityTag")) {
            CompoundTag nbt = pStack.getTag().getCompound("BlockEntityTag");
            if (nbt.contains("owner") && nbt.contains("channelID")) {
                var owner = nbt.getUUID("owner");
                var channelID = nbt.getUUID("channelID");
                var channel = ServerChestManager.getInstance().getChannel(owner, channelID);
                int i = 0;
                int j = 0;
                for (var item : channel.storageItems.keySet()) {
                    ++j;
                    if (i <= 4) {
                        ++i;
                        MutableComponent textComponent = StorageUtils.getItem(item).getDefaultInstance().getHoverName().copy();
                        textComponent.append(" x").append(String.format("%,d",  channel.storageItems.get(item)));
                        pTooltip.add(textComponent);
                    }
                }
                if (j - i > 0) {
                    pTooltip.add((Component.translatable("container.shulkerBox.more", j - i)).withStyle(ChatFormatting.ITALIC));
                }
            }
        }
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable LivingEntity pPlacer, @NotNull ItemStack pStack) {
        if (pPlacer instanceof ServerPlayer player && !pStack.getOrCreateTag().contains("BlockEntityTag")) {
            InfinityChestTile blockEntity = (InfinityChestTile) pLevel.getBlockEntity(pPos);
            if (blockEntity != null) {
                blockEntity.setOwner(player.getUUID());
                blockEntity.setChannelId(UUID.randomUUID());
                ServerChestManager.getInstance().tryAddChannel(player, blockEntity.getChannelID());
            }
        }
    }

    @Override
    public void playerWillDestroy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player pPlayer) {
        if (!pLevel.isClientSide() && pPlayer.isCreative() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof InfinityChestTile infinityChestTile) {
                ItemStack stack = new ItemStack(this);
                infinityChestTile.saveToItem(stack);
                popResource(pLevel, pPos, stack);
            }
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult trace) {
        if (!level.isClientSide() && !player.isSpectator()) {
            var tile = level.getBlockEntity(pos);

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
}
