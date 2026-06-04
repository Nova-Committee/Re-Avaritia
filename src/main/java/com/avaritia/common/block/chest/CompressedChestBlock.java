package com.avaritia.common.block.chest;

import com.avaritia.common.tile.CompressedChestTile;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import org.jspecify.annotations.NonNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 下午12:38
 * @Description:
 */
public class CompressedChestBlock extends ChestBlock {
    public static final Identifier CONTENTS = Identifier.withDefaultNamespace("contents");

    public CompressedChestBlock() {
        super(() -> ModTileEntities.compressed_chest_tile.get(), SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE,
                Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava());
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult trace) {
        if (!level.isClientSide()) {
            var tile = level.getBlockEntity(pos);

            if (tile instanceof CompressedChestTile chestTile) {
                player.openMenu(chestTile, pos);
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
    protected void affectNeighborsAfterRemoval(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    @Override
    public void playerDestroy(@NotNull Level pLevel, @NotNull Player player, @NotNull BlockPos pPos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack tool) {
        if (pLevel instanceof ServerLevel serverLevel && blockEntity instanceof CompressedChestTile chestTile && serverLevel.getGameRules().get(GameRules.BLOCK_DROPS)) {
            var pStack = new ItemStack(ModBlocks.compressed_chest.get().asItem());
            pStack.applyComponents(chestTile.collectComponents());
            popResource(serverLevel, pPos, pStack);
            state.spawnAfterBreak(serverLevel, pPos, tool, false);
        }
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state, boolean includeData, @NonNull Player player) {
        ItemStack itemstack = super.getCloneItemStack(level, pos, state, includeData, player);
        level.getBlockEntity(pos, ModTileEntities.compressed_chest_tile.get()).ifPresent(chestTile -> itemstack.applyComponents(chestTile.collectComponents()));
        return itemstack;
    }
}
