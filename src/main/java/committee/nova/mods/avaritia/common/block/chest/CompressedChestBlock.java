package committee.nova.mods.avaritia.common.block.chest;

import committee.nova.mods.avaritia.common.tile.CompressedChestTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/7/13 下午12:38
 * @Description:
 */
public class CompressedChestBlock extends ChestBlock {

    public CompressedChestBlock() {
        super(() -> ModTileEntities.compressed_chest_tile.get(), SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE,
                ModBlocks.properties().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava());
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult trace) {
        if (!level.isClientSide()) {
            var tile = level.getBlockEntity(pos);

            if (tile instanceof CompressedChestTile chestTile) {
                committee.nova.mods.avaritia.init.handler.NetworkHandler.openScreen((ServerPlayer) player, chestTile, pos);
                player.awardStat(Stats.OPEN_CHEST);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new CompressedChestTile(pPos, pState);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext pContext) {
        ChestType chesttype = ChestType.SINGLE;
        Direction direction = pContext.getHorizontalDirection().getOpposite();
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        return this.defaultBlockState().setValue(FACING, direction).setValue(TYPE, chesttype).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public boolean chestCanConnectTo(BlockState blockState) {
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction directionToNeighbour, BlockPos neighbourPos,
                                     BlockState neighbourState, RandomSource random) {
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random)
                .setValue(TYPE, ChestType.SINGLE);
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull LivingEntity pPlacer, @NotNull ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (pLevel.isClientSide()) return;
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        CustomData data = pStack.get(DataComponents.CUSTOM_DATA);
        if (data != null && blockentity instanceof CompressedChestTile chestTile) {
            chestTile.setChestTag(data.copyTag());
        }
    }

    @Override
    public void onPlace(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pOldState, boolean pMovedByPiston) {
        if (pLevel.isClientSide()) return;
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        CompoundTag chestTag, stacksTag = null, nameTag = null, countTag = null, nbtTag = null;
        if (blockentity instanceof CompressedChestTile chestTile && chestTile.getChestTag() != null) {
            chestTag = chestTile.getChestTag();
            if (chestTag.contains("stacks")) stacksTag = chestTag.getCompoundOrEmpty("stacks");
            if (chestTag.contains("name")) nameTag = chestTag.getCompoundOrEmpty("name");
            if (chestTag.contains("count")) countTag = chestTag.getCompoundOrEmpty("count");
            if (chestTag.contains("nbt")) nbtTag = chestTag.getCompoundOrEmpty("nbt");
        }
        if (stacksTag != null) {
            // 新格式：完整组件快照
            Container container = (Container) blockentity;
            for (String index : stacksTag.keySet()) {
                ItemStack is = StorageUtils.parseStack(stacksTag.get(index));
                if (is.isEmpty()) continue;
                container.setItem(Integer.parseInt(index), is);
            }
        } else if (nameTag != null && countTag != null) {
            // 旧格式回退：name/count/nbt（仅 CUSTOM_DATA）
            Container container = (Container) blockentity;
            for (String index : nameTag.keySet()) {
                var name = nameTag.getStringOr(index, "");
                var itemRef = BuiltInRegistries.ITEM.get(Identifier.parse(name));
                if (itemRef.isEmpty()) continue;
                ItemStack is = new ItemStack(itemRef.get());
                is.setCount(countTag.getIntOr(index, 1));
                if (nbtTag != null && !nbtTag.getCompoundOrEmpty(index).isEmpty()) {
                    CustomData.set(DataComponents.CUSTOM_DATA, is, nbtTag.getCompoundOrEmpty(index));
                }
                container.setItem(Integer.parseInt(index), is);
            }
        }
    }

    @Override
    public void playerDestroy(@NotNull ServerLevel serverLevel, @NotNull ServerPlayer player, @NotNull BlockPos pPos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack tool) {
        if (blockEntity instanceof CompressedChestTile chestTile && serverLevel.getGameRules().get(GameRules.BLOCK_DROPS)) {
            var pStack = new ItemStack(ModBlocks.compressed_chest.get().asItem());
            if (chestTile.getChestTag() != null) {
                CustomData.set(DataComponents.CUSTOM_DATA, pStack, chestTile.getChestTag());
            }
            popResource(serverLevel, pPos, pStack);
            state.spawnAfterBreak(serverLevel, pPos, tool, false);
        }
    }
}
