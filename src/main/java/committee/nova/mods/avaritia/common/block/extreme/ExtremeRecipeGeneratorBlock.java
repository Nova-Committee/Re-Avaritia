package committee.nova.mods.avaritia.common.block.extreme;

import committee.nova.mods.avaritia.api.common.block.BaseTileEntityBlock;
import committee.nova.mods.avaritia.common.tile.ExtremeCraftingTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ExtremeGeneratorBlock
 * Author cnlimiter
 * Version 1.0
 * Description
 * Date 2024/6/15 下午2:18
 */
public final class ExtremeRecipeGeneratorBlock extends BaseTileEntityBlock {
    public ExtremeRecipeGeneratorBlock() {
        super(MapColor.METAL, SoundType.GLASS, 100f, 2000F, true);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand p_60507_, @NotNull BlockHitResult p_60508_) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            var tile = level.getBlockEntity(pos);
            if (tile instanceof ExtremeCraftingTile table){
                NetworkHooks.openScreen(serverPlayer, table, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            var tile = level.getBlockEntity(pos);

            if (tile instanceof ExtremeCraftingTile table) {
                Containers.dropContents(level, pos, table.getInventory().getStacks());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ExtremeCraftingTile(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_49232_) {
        return RenderShape.MODEL;
    }

}
