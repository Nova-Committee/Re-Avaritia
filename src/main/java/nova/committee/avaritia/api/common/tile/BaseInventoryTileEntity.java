package nova.committee.avaritia.api.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nova.committee.avaritia.util.BaseItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:59
 * Version: 1.0
 */
public abstract class BaseInventoryTileEntity extends BaseTileEntity {

    private final LazyOptional<IItemHandler> capability = LazyOptional.of(this::getInventory);


    public BaseInventoryTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract @NotNull BaseItemStackHandler getInventory();

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.getInventory().deserializeNBT(tag);
    }

    public void saveAdditional(CompoundTag tag) {
        tag.merge(this.getInventory().serializeNBT());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return !this.isRemoved() && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, this.capability) : super.getCapability(cap, side);
    }

    public boolean isUsableByPlayer(Player player) {
        BlockPos pos = this.getBlockPos();
        return player.distanceToSqr((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

}
