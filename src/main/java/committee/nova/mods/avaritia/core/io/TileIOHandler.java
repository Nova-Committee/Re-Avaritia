package committee.nova.mods.avaritia.core.io;

import committee.nova.mods.avaritia.api.iface.ITileIO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

/**
 * Base IO handler implementation.
 * Handles shared direction conversion and IO logic.
 */
public class TileIOHandler {
    private final BlockEntity tileEntity;
    private final EnumProperty<Direction> facing;
    public TileIOHandler(BlockEntity tileEntity, EnumProperty<Direction> facing){
        this.tileEntity = tileEntity;
        this.facing = facing;
    }

    protected final Direction getRelativeDirection(Direction relativeSide, Direction blockFacing) {
        return switch (relativeSide) {
            // Front side follows the block facing.
            case NORTH -> blockFacing;
            // Back side is opposite the block facing.
            case SOUTH -> blockFacing.getOpposite();
            // Right side is clockwise relative to the block facing.
            case EAST -> blockFacing.getCounterClockWise();
            // Left side is counter-clockwise relative to the block facing.
            case WEST -> blockFacing.getClockWise();
            // Vertical directions remain absolute.
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default -> null;
        };
    }

    protected final Direction getRelativeDirectionFromAbsolute(Direction absoluteDirection, Direction blockFacing) {
        if (absoluteDirection == blockFacing) return Direction.NORTH;
        if (absoluteDirection == blockFacing.getOpposite()) return Direction.SOUTH;
        if (absoluteDirection == blockFacing.getCounterClockWise()) return Direction.EAST;
        if (absoluteDirection == blockFacing.getClockWise()) return Direction.WEST;
        if (absoluteDirection == Direction.UP) return Direction.UP;
        if (absoluteDirection == Direction.DOWN) return Direction.DOWN;
        return null;
    }

    /**
     * Gets the block's actual facing direction.
     */
    protected final Direction getBlockFacing() {
        Level level = tileEntity.getLevel();
        BlockPos pos = tileEntity.getBlockPos();
        if (level != null && pos != null) {
            BlockState state = level.getBlockState(pos);
            if (facing != null && state.hasProperty(facing)) {
                return state.getValue(facing);
            }
        }
        return null;
    }

    /**
     * Handles active input and output.
     */
    public void handleActiveIO() {
        Level level = tileEntity.getLevel();
        if (level == null) return;

        if (tileEntity instanceof ITileIO ITileIO) {
            SideConfiguration sideConfig = ITileIO.getSideConfiguration();

            for (Direction side : Direction.values()) {
                SideConfiguration.SideMode mode = sideConfig.getSideMode(side);

                if (mode == null || mode == SideConfiguration.SideMode.OFF) continue;

                switch (mode) {
                    case ACTIVE_INPUT -> handleActiveInput(side);
                    case ACTIVE_OUTPUT -> handleActiveOutput(side);
                    case ACTIVE_MIXIN -> handleMixedIO(side);
                    case PASSIVE_INPUT, PASSIVE_OUTPUT, PASSIVE_MIXIN -> {
                    }
                }
            }
        }
    }

    /**
     * Handles active input from a side.
     */
    protected void handleActiveInput(Direction side) {
        Direction blockFacing = getBlockFacing();
        if (blockFacing == null) return;

        Direction actualDirection = getRelativeDirection(side, blockFacing);
        if (actualDirection == null) return;

        BlockPos targetPos = tileEntity.getBlockPos().relative(actualDirection);
        Level level = tileEntity.getLevel();

        if (level != null && tileEntity instanceof ITileIO ITileIO) {
            BlockEntity targetTile = level.getBlockEntity(targetPos);
            if (targetTile != null) {
                var cap = level.getCapability(Capabilities.Item.BLOCK, targetPos, targetTile.getBlockState(), targetTile, actualDirection.getOpposite());
                if (cap != null) ITileIO.extractFromHandler(cap, actualDirection);
            }
        }
    }

    /**
     * Handles active output to a side.
     */
    protected void handleActiveOutput(Direction side) {
        Direction blockFacing = getBlockFacing();
        if (blockFacing == null) return;

        Direction actualDirection = getRelativeDirection(side, blockFacing);
        if (actualDirection == null) return;

        BlockPos targetPos = tileEntity.getBlockPos().relative(actualDirection);
        Level level = tileEntity.getLevel();

        if (level != null && tileEntity instanceof ITileIO ITileIO) {
            BlockEntity targetTile = level.getBlockEntity(targetPos);
            if (targetTile != null) {
                var cap = level.getCapability(Capabilities.Item.BLOCK, targetPos, targetTile.getBlockState(), targetTile, actualDirection.getOpposite());
                if (cap != null) ITileIO.insertToHandler(cap, actualDirection);
            }
        }
    }

    /**
     * Handles mixed IO for a side.
     */
    protected void handleMixedIO(Direction side) {
        Direction blockFacing = getBlockFacing();
        if (blockFacing == null) return;

        Direction actualDirection = getRelativeDirection(side, blockFacing);
        if (actualDirection == null) return;

        BlockPos targetPos = tileEntity.getBlockPos().relative(actualDirection);
        Level level = tileEntity.getLevel();

        if (level != null && tileEntity instanceof ITileIO ITileIO) {
            BlockEntity targetTile = level.getBlockEntity(targetPos);
            if (targetTile != null) {
                var cap = level.getCapability(Capabilities.Item.BLOCK, targetPos, targetTile.getBlockState(), targetTile, actualDirection.getOpposite());
                if (cap != null) {
                    ITileIO.extractFromHandler(cap, actualDirection);
                    ITileIO.insertToHandler(cap, actualDirection);
                }
            }
        }
    }

    /**
     * 检查被动IO配置，用于getCapability调用
     */
    public boolean shouldAllowPassiveIO(@Nullable Direction side) {
        if (side == null) return true;

        Direction blockFacing = getBlockFacing();
        if (blockFacing != null && tileEntity instanceof ITileIO tileIO) {
            Direction relativeSide = getRelativeDirectionFromAbsolute(side, blockFacing);
            if (relativeSide != null) {
                SideConfiguration.SideMode mode = tileIO.getSideConfiguration().getSideMode(relativeSide);
                return mode != SideConfiguration.SideMode.OFF;
            }
        }
        return false;
    }
}
