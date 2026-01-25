package committee.nova.mods.avaritia.api.common.tile;

import committee.nova.mods.avaritia.api.common.wrapper.BaseItemWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 13:59
 * Version: 1.0
 */
public abstract class BaseInventoryTileEntity extends BaseTileEntity {

    public LazyOptional<IItemHandler> capability = LazyOptional.of(this::getInventory);
    private LockCode lockKey = LockCode.NO_LOCK;


    public BaseInventoryTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static boolean canUnlock(Player pPlayer, LockCode pCode, Component pDisplayName) {
        if (!pPlayer.isSpectator() && !pCode.unlocksWith(pPlayer.getMainHandItem())) {
            pPlayer.displayClientMessage(Component.translatable("container.isLocked", pDisplayName), true);
            pPlayer.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            return false;
        } else {
            return true;
        }
    }

    public abstract @NotNull BaseItemWrapper getInventory();

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.lockKey = LockCode.fromTag(tag);
        this.getInventory().deserializeNBT(tag);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        this.lockKey.addToTag(tag);
        tag.merge(this.getInventory().serializeNBT());
    }

    public boolean canOpen(Player pPlayer) {
        return canUnlock(pPlayer, this.lockKey, this.getDisplayName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return this.canOpen(pPlayer) ? this.createMenu(pContainerId, pPlayerInventory) : null;
    }

    protected abstract AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory);


    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (!this.isRemoved() && cap == ForgeCapabilities.ITEM_HANDLER) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, this.capability);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.capability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.capability = LazyOptional.of(this::getInventory);
    }

    public boolean isUsableByPlayer(Player player) {
        BlockPos pos = this.getBlockPos();
        return player.distanceToSqr((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

}
