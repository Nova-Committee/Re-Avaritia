package committee.nova.mods.avaritia.common.tile.collector;

import committee.nova.mods.avaritia.api.common.item.BaseItemStackHandler;
import committee.nova.mods.avaritia.api.common.tile.InventoryTileEntity;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.util.ItemStackUtil;
import committee.nova.mods.avaritia.util.lang.Localizable;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:55
 * Version: 1.0
 */
public abstract class AbsNeutronCollectorTile extends InventoryTileEntity implements MenuProvider {


    public final BaseItemStackHandler inventory;
    private int progress;
    private int production_ticks;
    private Item production;
    private String name;

    public SimpleContainerData data = new SimpleContainerData(1);

    public AbsNeutronCollectorTile(BlockEntityType<?> entityType, BlockPos pos, BlockState state, Item production, int production_ticks, String name) {
        super(entityType, pos, state);
        this.production_ticks = production_ticks;
        this.production = production;
        this.name = name;
        this.inventory = createInventoryHandler(null);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AbsNeutronCollectorTile tile) {
        if (level.isClientSide) return;
        if (tile.canWork()) {
            var result = tile.inventory.getStackInSlot(0);
            var stack = new ItemStack(tile.production);
            tile.progress++;
            tile.data.set(0, tile.progress);
            if (tile.progress >= tile.production_ticks) {
                if (result.isEmpty()) {
                    tile.inventory.setStackInSlot(0, ItemHandlerHelper.copyStackWithSize(stack, 1));
                } else if (result.is(tile.production)) {
                    if (result.getCount() < 64) {
                        tile.inventory.setStackInSlot(0, ItemStackUtil.grow(result, 1));
                    }
                }
                tile.progress = 0;
                tile.setChangedAndDispatch();
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
        return Localizable.of("container." + name).build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return NeutronCollectorMenu.create(windowId, playerInventory, this.inventory, data);
    }

    public int getProductionTicks() {
        return production_ticks;
    }

    public Item getProduction() {
        return production;
    }
}
