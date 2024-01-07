package committee.nova.mods.avaritia.init.event;

import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.util.ToolUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.Set;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 22:55
 * Version: 1.0
 */
public class MatterCollectEvent {
    final Level world;
    final Player player;
    final ItemStack stack;
    final int steps;
    final BlockPos origin;
    final boolean leaves;
    final boolean force;
    final Set<BlockPos> posChecked;

    public MatterCollectEvent(Level world, Player player, ItemStack stack, BlockPos origin, int steps, boolean leaves, boolean force, Set<BlockPos> posChecked) {
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
        ToolUtil.removeBlockWithDrops(player, stack, world, origin, null, ToolUtil.materialsAxe);
        if (steps == 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            BlockPos stepPos = origin.relative(dir);
            if (posChecked.contains(stepPos)) { //prevent manipulate duplicate coordinates
                continue;
            }
            BlockState stepState = world.getBlockState(stepPos);
            Block stepBlock = stepState.getBlock();
            boolean log = stepState.getMapColor(world, stepPos) == MapColor.WOOD;
            boolean leaf = stepBlock instanceof LeavesBlock;
            if (log || leaf) {
                int steps = this.steps - 1;
                steps = leaf ? leaves ? steps : 3 : steps;
                InfinityHandler.startCrawlerTask(world, player, stack, stepPos, steps, leaf, false, posChecked);//recurring event
                posChecked.add(stepPos);
            }
        }
    }

}
