package nova.committee.avaritia.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import nova.committee.avaritia.api.common.item.StackHelper;
import nova.committee.avaritia.api.common.tile.BaseInventoryTileEntity;
import nova.committee.avaritia.common.menu.NeutronCollectorMenu;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.init.registry.ModTileEntities;
import nova.committee.avaritia.util.BaseItemStackHandler;
import nova.committee.avaritia.util.Localizable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:55
 * Version: 1.0
 */
public class NeutronCollectorTile extends BaseInventoryTileEntity implements MenuProvider {


    public static final int PRODUCTION_TICKS = 3600;
    private final BaseItemStackHandler inventory;
    private int progress;


    public NeutronCollectorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.neutron_collector_tile, pos, state);
        this.inventory = createInventoryHandler(null);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NeutronCollectorTile tile) {
        if (level.isClientSide) return;
        if (tile.canWork()) {
            var result = tile.inventory.getStackInSlot(0);
            ItemStack stack = new ItemStack(ModItems.neutron_pile);
            if (++tile.progress >= PRODUCTION_TICKS) {
                if (result.isEmpty()) {
                    tile.inventory.setStackInSlot(0, ItemHandlerHelper.copyStackWithSize(stack, 1));
                } else if (result.is(ModItems.neutron_pile)) {
                    if (result.getCount() < 64) {
                        tile.inventory.setStackInSlot(0, StackHelper.grow(result, 1));
                    }
                }
                tile.progress = 0;
                tile.markDirtyAndDispatch();
            }
        }


    }

    public static BaseItemStackHandler createInventoryHandler(Runnable onContentsChanged) {
        var inventory = new BaseItemStackHandler(1, onContentsChanged);
        inventory.setOutputSlots(0);
        return inventory;
    }

    @Override
    public @NotNull BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    protected boolean canWork() {
        return inventory.getStackInSlot(0).isEmpty() || inventory.getStackInSlot(0).getCount() < 64;
    }


    @Override
    public Component getDisplayName() {
        return Localizable.of("container.neutron_collector").build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return NeutronCollectorMenu.create(windowId, playerInventory, this::isUsableByPlayer, this.inventory, new SimpleContainerData(0), this.getBlockPos());
    }

}
