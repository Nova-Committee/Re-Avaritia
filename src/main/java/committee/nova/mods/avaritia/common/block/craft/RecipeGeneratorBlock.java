package committee.nova.mods.avaritia.common.block.craft;

import committee.nova.mods.avaritia.api.common.block.BaseTileEntityBlock;
import committee.nova.mods.avaritia.common.tile.InfiniteChestBlockEntity;
import committee.nova.mods.avaritia.common.tile.RecipeGeneratorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: cnlimiter
 */
public class RecipeGeneratorBlock extends BaseTileEntityBlock {
    public RecipeGeneratorBlock() {
        super(MapColor.METAL, SoundType.METAL, 50f, 2000f, true);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new RecipeGeneratorTile(blockPos, blockState);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult trace) {
        if (!level.isClientSide() && !player.isSpectator()) {
            var tile = level.getBlockEntity(pos);

            if (tile instanceof RecipeGeneratorTile chestTile) {
                NetworkHooks.openScreen((ServerPlayer) player, chestTile, buf -> {buf.writeBlockPos(pos);});
            }
        }

        return InteractionResult.SUCCESS;
    }

}
