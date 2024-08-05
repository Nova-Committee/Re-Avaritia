package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.common.menu.CompressedChestMenu;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / CompressChestTile
 * Author: cnlimiter
 * CreateTime: 2023/11/21 3:34
 * Description:
 */

public class CompressedChestTile extends ChestBlockEntity {

    protected final int SIZE = 243;
    protected CompoundTag chestTag;
    private final ContainerOpenersCounter openersCounter;

    protected CompressedChestTile(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.openersCounter = new ContainerOpenersCounter() {
            @Override
            protected void onOpen(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState) {
                playSound(pLevel, pPos, pState, SoundEvents.CHEST_OPEN);
            }

            @Override
            protected void onClose(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState) {
                playSound(pLevel, pPos, pState, SoundEvents.CHEST_CLOSE);
            }

            @Override
            protected void openerCountChanged(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, int pEventId, int pEventParam) {
                signalOpenCount(pLevel, pPos, pState, pEventId, pEventParam);
            }

            @Override
            protected boolean isOwnContainer(@NotNull Player pPlayer) {
                if( pPlayer.containerMenu instanceof CompressedChestMenu chestMenu) {
                    Container container = chestMenu.getContainer();
                    return container == CompressedChestTile.this || container instanceof CompoundContainer && ((CompoundContainer)container).contains(CompressedChestTile.this);
                } else {
                    return false;
                }
            }
        };
        setItems(NonNullList.withSize(SIZE, ItemStack.EMPTY));
    }

    public CompressedChestTile(BlockPos pos, BlockState blockState) {
        this(ModTileEntities.compressed_chest_tile.get(), pos, blockState);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.avaritia.compressed_chest");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pInventory) {
        return new CompressedChestMenu(ModMenus.GENERIC_9x27.get(), pContainerId, pInventory, this, 9);
    }


    @Override
    public void startOpen(@NotNull Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    @Override
    public void stopOpen(@NotNull Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    protected void signalOpenCount(Level pLevel, BlockPos pPos, BlockState pState, int pEventId, int pEventParam) {
        Block block = pState.getBlock();
        pLevel.blockEvent(pPos, block, 1, pEventParam);
    }

    static void playSound(Level pLevel, BlockPos pPos, BlockState pState, SoundEvent pSound) {
        ChestType chesttype = pState.getValue(ChestBlock.TYPE);
        if (chesttype != ChestType.LEFT) {
            double d0 = (double)pPos.getX() + 0.5;
            double d1 = (double)pPos.getY() + 0.5;
            double d2 = (double)pPos.getZ() + 0.5;
            if (chesttype == ChestType.RIGHT) {
                Direction direction = ChestBlock.getConnectedDirection(pState);
                d0 += (double)direction.getStepX() * 0.5;
                d2 += (double)direction.getStepZ() * 0.5;
            }

            pLevel.playSound(null, d0, d1, d2, pSound, SoundSource.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    public CompoundTag getChestTag() {
        return chestTag;
    }

    public void setChestTag(CompoundTag chestTag) {
        this.chestTag = chestTag;
    }
}
