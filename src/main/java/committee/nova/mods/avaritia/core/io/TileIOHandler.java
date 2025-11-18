package committee.nova.mods.avaritia.core.io;

import committee.nova.mods.avaritia.api.iface.ITileIO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

/**
 * IO处理器基础实现类
 * 包含所有通用的方向转换和IO处理逻辑
 */
public class TileIOHandler {
    private final BlockEntity tileEntity;
    private final DirectionProperty facing;
    public TileIOHandler(BlockEntity tileEntity, DirectionProperty facing){
        this.tileEntity = tileEntity;
        this.facing = facing;
    }

    protected final Direction getRelativeDirection(Direction relativeSide, Direction blockFacing) {
        return switch (relativeSide) {
            // 前方 = 方块朝向
            case NORTH -> blockFacing;
            // 后方 = 方块朝向的对面
            case SOUTH -> blockFacing.getOpposite();
            // 右侧 = 方块朝向顺时针90度
            case EAST -> blockFacing.getCounterClockWise();
            // 左侧 = 方块朝向逆时针90度
            case WEST -> blockFacing.getClockWise();
            // 上下方向保持不变
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default -> null;
        };
    }

    protected final Direction getRelativeDirectionFromAbsolute(Direction absoluteDirection, Direction blockFacing) {
        // 如果绝对方向与方块朝向相同，则它是前方
        if (absoluteDirection == blockFacing) return Direction.NORTH;
        if (absoluteDirection == blockFacing.getOpposite()) return Direction.SOUTH;
        if (absoluteDirection == blockFacing.getCounterClockWise()) return Direction.EAST;
        if (absoluteDirection == blockFacing.getClockWise()) return Direction.WEST;
        if (absoluteDirection == Direction.UP) return Direction.UP;
        if (absoluteDirection == Direction.DOWN) return Direction.DOWN;
        return null;
    }

    /**
     * 获取方块的实际朝向
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
     * 处理主动输入输出操作
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
                    // 被动模式不在这里处理
                    case PASSIVE_INPUT, PASSIVE_OUTPUT, PASSIVE_MIXIN -> {
                    }
                }
            }
        } else return;
    }

    /**
     * 处理从指定方向的主动输入
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
                var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, tileEntity.getBlockPos(), tileEntity.getBlockState(), tileEntity, actualDirection.getOpposite());
                if (cap != null) ITileIO.extractFromHandler(cap, actualDirection);
            }
        }
    }

    /**
     * 处理到指定方向的主动输出
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
                var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, tileEntity.getBlockPos(), tileEntity.getBlockState(), tileEntity, actualDirection.getOpposite());
                if (cap != null) ITileIO.insertToHandler(cap, actualDirection);
            }
        }
    }

    /**
     * 处理指定方向的混合IO（同时支持输入和输出）
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
                // 先尝试输入，再尝试输出
                var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, tileEntity.getBlockPos(), tileEntity.getBlockState(), tileEntity, actualDirection.getOpposite());
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
            // 将绝对方向转换为相对方向进行配置检查
            Direction relativeSide = getRelativeDirectionFromAbsolute(side, blockFacing);
            if (relativeSide != null) {
                SideConfiguration.SideMode mode = tileIO.getSideConfiguration().getSideMode(relativeSide);
                return mode != SideConfiguration.SideMode.OFF;//除了关闭状态都能连接
            }
        }
        return false;
    }
}
