package nova.committee.avaritia.init.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import nova.committee.avaritia.init.handler.InfinityHandler;
import nova.committee.avaritia.util.ToolHelper;

import java.util.Set;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 22:55
 * Version: 1.0
 */
public class AEOCrawlerTask {
    final Level world;
    final Player player;
    final ItemStack stack;
    final int steps;
    final BlockPos origin;
    final boolean leaves;
    final boolean force;
    final Set<BlockPos> posChecked;

    public AEOCrawlerTask(Level world, Player player, ItemStack stack, BlockPos origin, int steps, boolean leaves, boolean force, Set<BlockPos> posChecked) {
        this.world = world;
        this.player = player;
        this.stack = stack;
        this.origin = origin;
        this.steps = steps;
        this.leaves = leaves;
        this.force = force;
        this.posChecked = posChecked;
    }

    public void tick() {
        BlockState originState = world.getBlockState(origin);
        Block originBlock = originState.getBlock();
        if (!force && originState.isAir()) {
            return;
        }
        ToolHelper.removeBlockWithDrops(player, stack, world, origin, null, ToolHelper.materialsAxe);
        if (steps == 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            BlockPos stepPos = origin.relative(dir);
            if (posChecked.contains(stepPos)) {
                continue;
            }
            BlockState stepState = world.getBlockState(stepPos);
            boolean log = canBeReplacedByLogs(stepState);
            boolean leaf = canBeReplacedByLeaves(stepState);
            if (log || leaf) {
                int steps = this.steps - 1;
                steps = leaf ? leaves ? steps : 3 : steps;
                InfinityHandler.startCrawlerTask(world, player, stack, stepPos, steps, leaf, false, posChecked);
                posChecked.add(stepPos);
            }
        }
    }

    boolean canBeReplacedByLogs(BlockState state) {
        return (state.isAir() || state.is(BlockTags.LEAVES)) || state.getBlock() == Blocks.GRASS_BLOCK || state.is(BlockTags.DIRT)
                || state.is(BlockTags.LOGS) || state.is(BlockTags.SAPLINGS) || state.getBlock() == Blocks.VINE;
    }

    boolean canBeReplacedByLeaves(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES);
    }

}
