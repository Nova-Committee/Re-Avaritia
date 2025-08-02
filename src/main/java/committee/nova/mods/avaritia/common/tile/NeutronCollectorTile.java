package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.init.registry.enums.CollectorTier;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
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
public class NeutronCollectorTile extends BaseInventoryTileEntity {


    public final ItemStackWrapper inventory;
    public SimpleContainerData data = new SimpleContainerData(1);
    private int progress;
    private CollectorTier tier;

    public NeutronCollectorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.neutron_collector_tile.get(), pos, state);
        this.inventory = createInventoryHandler();
        if (state.is(ModBlocks.neutron_collector.get())) {
            tier = CollectorTier.DEFAULT;
        } else if (state.is(ModBlocks.dense_neutron_collector.get())) {
            tier = CollectorTier.DENSE;
        } else if (state.is(ModBlocks.denser_neutron_collector.get())) {
            tier = CollectorTier.DENSER;
        } else if (state.is(ModBlocks.densest_neutron_collector.get())) {
            tier = CollectorTier.DENSEST;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NeutronCollectorTile tile) {
        if (tile.canWork()) {
            var result = tile.inventory.getStackInSlot(0);
            var stack = tile.tier.production.getItems()[0];
            tile.progress++;
            tile.data.set(0, tile.progress);
            if (tile.progress >= tile.tier.production_ticks) {
                if (result.isEmpty()) {
                    tile.inventory.setStackInSlot(0, ItemHandlerHelper.copyStackWithSize(stack, 1));
                } else if (result.is(stack.getItem())) {
                    if (result.getCount() < 64) {
                        tile.inventory.setStackInSlot(0, ItemUtils.grow(result, 1));
                    }
                }
                tile.progress = 0;
                tile.setChangedAndDispatch();
            }
        }


    }

    public static ItemStackWrapper createInventoryHandler() {
        var inventory = new ItemStackWrapper(1, Integer.MAX_VALUE);
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
    public @NotNull ItemStackWrapper getInventory() {
        return this.inventory;
    }

    protected boolean canWork() {
        return inventory.getStackInSlot(0).isEmpty() || inventory.getStackInSlot(0).getCount() < 64;
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("container." + tier.name).build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory) {
        return new NeutronCollectorMenu(windowId, playerInventory, this.getBlockPos(), data);
    }

    public CollectorTier getTier() {
        return tier;
    }

    public void setTier(CollectorTier tier) {
        this.tier = tier;
    }

    public int getProductionTicks() {
        return tier.production_ticks;
    }

    public Item getProduction() {
        return tier.production.getItems()[0].getItem();
    }
}
