package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.inventory.OnContentsChangedFunction;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.init.registry.enums.CollectorTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:55
 * Version: 1.0
 */
public class NeutronCollectorTile extends BaseInventoryTileEntity implements WorldlyContainer {
    public final ItemStackWrapper inventory;
    public SimpleContainerData data = new SimpleContainerData(1);
    private int progress;
    private CollectorTier tier;

    public NeutronCollectorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.neutron_collector_tile.get(), pos, state);
        this.inventory = createInventoryHandler((slot) -> this.setChangedAndDispatch());
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
                    tile.inventory.setStackInSlot(0, stack.copyWithCount(1));
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

    public static ItemStackWrapper createInventoryHandler(OnContentsChangedFunction onContentsChanged) {
        return ItemStackWrapper.create(9, onContentsChanged, builder -> {});
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
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

    public ItemStack getProduction() {
        return tier.production.getItems()[0];
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction direction) {
        if (direction == Direction.DOWN) {
            return new int[] { 0 };
        } else {
            return new int[] { 1 };
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack itemStack, @NotNull Direction direction) {
        if (itemStack.isEmpty()) {
            return false;
        }
        if (index == 0) { //output
            var result = this.getInventory().getStackInSlot(0);
            return ItemStack.isSameItemSameComponents(result, getProduction());
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return this.getInventory().getSlots();
    }

    @Override
    public boolean isEmpty() {
        return this.getInventory().getStacks().isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot == 0) {
            return this.getInventory().toRecipeInventory().getItem(0);
        } else return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (slot == 0) {
            return this.getInventory().toRecipeInventory().removeItem(0, amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0) {
            return this.getInventory().toRecipeInventory().removeItemNoUpdate(0);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack itemStack) {
        if (slot == 0) {
            this.getInventory().toRecipeInventory().setItem(0, itemStack);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        BlockPos blockPos = this.getBlockPos();
        return player.distanceToSqr(blockPos.getX() + 0.5D,
                blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {

    }
}
