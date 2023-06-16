package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.item.StackHelper;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.util.item.BaseItemStackHandler;
import committee.nova.mods.avaritia.util.lang.Localizable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:55
 * Version: 1.0
 */
public class NeutronCollectorTile extends BaseInventoryTileEntity implements MenuProvider {


    public static final int PRODUCTION_TICKS = ModConfig.neutronCollectorProductTick.get();
    private final BaseItemStackHandler inventory;
    private int progress;

    private SimpleContainerData data = new SimpleContainerData(1);

    public NeutronCollectorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.neutron_collector_tile.get(), pos, state);
        this.inventory = createInventoryHandler(null);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NeutronCollectorTile tile) {
        if (level.isClientSide) return;
        if (tile.canWork()) {
            var result = tile.inventory.getStackInSlot(0);
            var stack = new ItemStack(ModItems.neutron_pile.get());
            tile.progress++;
            tile.data.set(0, tile.progress);
            if (tile.progress >= PRODUCTION_TICKS) {
                if (result.isEmpty()) {
                    tile.inventory.setStackInSlot(0, ItemHandlerHelper.copyStackWithSize(stack, 1));
                } else if (result.is(ModItems.neutron_pile.get())) {
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
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.progress = tag.getInt("progress");
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("progress", progress);
    }

    @Override
    public @NotNull BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    protected boolean canWork() {
        return inventory.getStackInSlot(0).isEmpty() || inventory.getStackInSlot(0).getCount() < 64;
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("container.neutron_collector").build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return NeutronCollectorMenu.create(windowId, playerInventory, this::isUsableByPlayer, this.inventory, data, this.getBlockPos());
    }

}
