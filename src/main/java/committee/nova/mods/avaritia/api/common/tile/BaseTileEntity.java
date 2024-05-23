package committee.nova.mods.avaritia.api.common.tile;

import committee.nova.mods.old.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 14:00
 * Version: 1.0
 */
public abstract class BaseTileEntity extends BlockEntity {
    private boolean isChanged = false;

    public BaseTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.isChanged = true;
    }

    public void setChangedFast() {
        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
            this.isChanged = true;
        }
    }

    public void setChangedAndDispatch() {
        this.setChanged();
        TileEntityUtil.dispatchToNearbyPlayers(this);
        this.isChanged = false;
    }

    public void dispatchIfChanged() {
        if (this.isChanged) {
            TileEntityUtil.dispatchToNearbyPlayers(this);
            this.isChanged = false;
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::saveWithFullMetadata);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

}
