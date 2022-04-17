package nova.committee.avaritia.api.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import nova.committee.avaritia.util.TileEntityHelper;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 14:00
 * Version: 1.0
 */
public class BaseTileEntity extends BlockEntity {
    public BaseTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::saveWithFullMetadata);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    public void markDirtyAndDispatch() {
        super.setChanged();
        TileEntityHelper.dispatchToNearbyPlayers(this);
    }
}
