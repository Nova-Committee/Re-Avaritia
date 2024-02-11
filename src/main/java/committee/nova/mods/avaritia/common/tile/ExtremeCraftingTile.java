package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.item.BaseItemStackHandler;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.common.menu.ExtremeCraftingMenu;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.util.lang.Localizable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 8:44
 * Version: 1.0
 */
public class ExtremeCraftingTile extends BaseInventoryTileEntity{

    private final BaseItemStackHandler inventory;


    public ExtremeCraftingTile(BlockPos pos, BlockState blockState) {
        super(ModTileEntities.extreme_crafting_tile.get(), pos, blockState);
        this.inventory = createInventoryHandler(this::setChangedAndDispatch);

    }

    public static BaseItemStackHandler createInventoryHandler() {
        return createInventoryHandler(null);
    }

    public static BaseItemStackHandler createInventoryHandler(Runnable onContentsChanged) {
        return new BaseItemStackHandler(81, onContentsChanged);
    }

    @Override
    public @NotNull BaseItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    protected Component getDefaultName() {
        return Localizable.of("container.extreme_crafting_table").build();
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return ExtremeCraftingMenu.create(pContainerId, pInventory, this.inventory, this.getBlockPos());
    }


}
