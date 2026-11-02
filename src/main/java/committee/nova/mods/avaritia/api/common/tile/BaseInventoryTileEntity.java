package committee.nova.mods.avaritia.api.common.tile;

import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 13:59
 * Version: 1.0
 */
public abstract class BaseInventoryTileEntity extends BaseTileEntity implements committee.nova.mods.avaritia.core.io.NativeInventory {

    private LockCode lockKey = LockCode.NO_LOCK;
    private int[] automationSlots = committee.nova.mods.avaritia.core.io.NativeInventory.EMPTY_SLOTS;

    public BaseInventoryTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static boolean canUnlock(Player pPlayer, LockCode pCode, Component pDisplayName) {
        if (!pPlayer.isSpectator() && !pCode.unlocksWith(pPlayer.getMainHandItem())) {
            pPlayer.sendOverlayMessage(Component.translatable("container.isLocked", pDisplayName));
            pPlayer.playSound(SoundEvents.CHEST_LOCKED, 1.0F, 1.0F);
            return false;
        } else {
            return true;
        }
    }

    public abstract @NotNull ItemStackWrapper getInventory();

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.lockKey = LockCode.fromTag(input);
        this.getInventory().deserialize(input);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        this.lockKey.addToTag(output);
        this.getInventory().serialize(output);
    }

    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        this.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
    }

    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        this.saveAdditional(output);
        tag.merge(output.buildResult());
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
    public committee.nova.mods.avaritia.api.common.wrapper.ItemHandler getItemHandler(Direction side) {
        return isRemoved() ? null : getInventory();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        var handler = getItemHandler(side);
        return handler == null ? committee.nova.mods.avaritia.core.io.NativeInventory.EMPTY_SLOTS : (automationSlots = committee.nova.mods.avaritia.core.io.NativeInventory.slotIndices(handler.getSlots(), automationSlots));
    }

    @Override
    public boolean stillValid(Player player) { return isUsableByPlayer(player) && level != null && level.getBlockEntity(worldPosition) == this; }

    public boolean isUsableByPlayer(Player player) {
        BlockPos pos = this.getBlockPos();
        return player.distanceToSqr((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

}