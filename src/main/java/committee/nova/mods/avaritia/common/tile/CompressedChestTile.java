package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / CompressChestTile
 * Author: cnlimiter
 * CreateTime: 2023/11/21 3:34
 * Description:
 */
public final class CompressedChestTile extends ChestBlockEntity {
    private CompressedChestTile(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        setItems(NonNullList.withSize(81, ItemStack.EMPTY));
    }

    public CompressedChestTile(BlockPos pos, BlockState blockState) {
        this(ModTileEntities.compressed_chest_tile.get(), pos, blockState);
    }

    @Override
    public int getContainerSize() {
        return 81;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.avaritia.compressed_chest");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pInventory) {
        return new ChestMenu(ModMenus.GENERIC_9x9.get(), pContainerId, pInventory, this, 9);
    }
}
