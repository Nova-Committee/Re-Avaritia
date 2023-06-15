package nova.committee.avaritia.api.common.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:06
 * Version: 1.0
 */
public abstract class BaseTileEntityBlock extends BaseBlock implements EntityBlock {
    public BaseTileEntityBlock(Function<Properties, Properties> properties) {
        super(properties);
    }

    public BaseTileEntityBlock(SoundType sound, float hardness, float resistance) {
        super(sound, hardness, resistance);
    }

    public BaseTileEntityBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        super(sound, hardness, resistance, tool);
    }

    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTicker(BlockEntityType<A> typeA, BlockEntityType<E> typeB, BlockEntityTicker<? super E> ticker) {
        return typeA == typeB ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? this.getClientTicker(level, state, type)
                : this.getServerTicker(level, state, type);
    }

    protected <T extends BlockEntity> BlockEntityTicker<T> getClientTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
