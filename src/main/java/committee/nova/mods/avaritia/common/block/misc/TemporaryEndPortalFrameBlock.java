package committee.nova.mods.avaritia.common.block.misc;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** The temporary, harvestable form of a vanilla end portal frame. */
public class TemporaryEndPortalFrameBlock extends EndPortalFrameBlock {
    public TemporaryEndPortalFrameBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide) {
            Block.popResource(level, pos, new ItemStack(Items.END_PORTAL_FRAME));
            if (state.getValue(HAS_EYE)) {
                Block.popResource(level, pos, new ItemStack(Items.ENDER_EYE));
            }
            this.deactivatePortal(level, pos, state);
        }
    }

    private void deactivatePortal(Level level, BlockPos removedPos, BlockState removedState) {
        Direction inward = removedState.getValue(FACING);
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos center = removedPos.relative(inward, 2).relative(inward.getClockWise(), offset);
            if (!this.isPortalRing(level, center, removedPos, removedState)) {
                continue;
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos portalPos = center.offset(x, 0, z);
                    if (level.getBlockState(portalPos).is(Blocks.END_PORTAL)) {
                        level.removeBlock(portalPos, false);
                    }
                }
            }
            return;
        }
    }

    private boolean isPortalRing(Level level, BlockPos center, BlockPos removedPos, BlockState removedState) {
        for (Direction inward : Direction.Plane.HORIZONTAL) {
            for (int offset = -1; offset <= 1; offset++) {
                BlockPos framePos = center.relative(inward.getOpposite(), 2).relative(inward.getClockWise(), offset);
                BlockState frame = framePos.equals(removedPos) ? removedState : level.getBlockState(framePos);
                if (!(frame.is(Blocks.END_PORTAL_FRAME) || frame.is(ModBlocks.fake_end_portal_frame.get()))
                        || frame.getValue(FACING) != inward || !frame.getValue(HAS_EYE)) {
                    return false;
                }
            }
        }
        return true;
    }
}
