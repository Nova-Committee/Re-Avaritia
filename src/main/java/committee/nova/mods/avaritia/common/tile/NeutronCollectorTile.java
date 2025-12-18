package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.inventory.OnContentsChangedFunction;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.block.collector.NeutronCollectorBlock;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import committee.nova.mods.avaritia.core.io.TileIOHandler;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
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
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:55
 * Version: 1.0
 */
public class NeutronCollectorTile extends BaseInventoryTileEntity implements ITileIO, WorldlyContainer {
    private SideConfiguration sideConfig = new SideConfiguration();
    // 主动输出操作计时器
    private int activeOutputTick = 0;
    private static final int ACTIVE_OUTPUT_INTERVAL = 20; // 每秒执行一次主动输出

    // IO处理器
    private final TileIOHandler ioHandler = new TileIOHandler(this, NeutronCollectorBlock.FACING);

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
        if (!level.isClientSide()) {
            tile.activeOutputTick++;
            if (tile.activeOutputTick >= ACTIVE_OUTPUT_INTERVAL) {
                tile.activeOutputTick = 0;
                tile.handleActiveOutput();
            }
        }
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
        if (tag.contains("SideConfig")) {
            this.sideConfig = SideConfiguration.fromNBT(tag.getCompound("SideConfig"));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("progress", progress);
        tag.put("SideConfig", sideConfig.toNBT());
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
        return Localizable.of("block.avaritia." + tier.name).build();
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

    private void handleActiveOutput() {
        ioHandler.handleActiveIO(); // IO处理器会自动处理输出
    }

    // IO配置相关方法
    @Override
    public SideConfiguration getSideConfiguration() {
        return this.sideConfig;
    }

    @Override
    public void setSideConfiguration(SideConfiguration config) {
        // 只允许PASSIVE_OUTPUT和ACTIVE_OUTPUT模式
        for (Direction direction : Direction.values()) {
            SideConfiguration.SideMode mode = config.getSideMode(direction);
            if (mode != SideConfiguration.SideMode.PASSIVE_OUTPUT && mode != SideConfiguration.SideMode.ACTIVE_OUTPUT) {
                config.setSideMode(direction, SideConfiguration.SideMode.OFF);
            }
        }
        this.sideConfig = config;
        this.setChangedAndDispatch();

        // 同步给客户端
        if (!this.level.isClientSide()) {
            NetworkHandler.sendSideConfigSync(this.worldPosition, config);
        }
    }

    @Override
    public void setIOChange() {
        this.setChangedAndDispatch();
    }

    /**
     * 只在PASSIVE_OUTPUT和ACTIVE_OUTPUT之间切换
     */
    @Override
    public void cycleSideModeForNeutronCollector(Direction direction) {
        SideConfiguration.SideMode current = sideConfig.getSideMode(direction);
        SideConfiguration.SideMode nextMode;

        if (current == SideConfiguration.SideMode.OFF) {
            nextMode = SideConfiguration.SideMode.PASSIVE_OUTPUT;
        } else if (current == SideConfiguration.SideMode.PASSIVE_OUTPUT) {
            nextMode = SideConfiguration.SideMode.ACTIVE_OUTPUT;
        } else {
            // 默认从PASSIVE_OUTPUT开始
            nextMode = SideConfiguration.SideMode.OFF;
        }

        sideConfig.setSideMode(direction, nextMode);
        this.setChangedAndDispatch();

        // 同步给客户端
        if (!this.level.isClientSide()) {
            NetworkHandler.sendSideConfigSync(this.worldPosition, sideConfig);
        }
    }

    @Override
    public void extractFromHandler(IItemHandler externalHandler, Direction fromSide) {
    }

    @Override
    public void insertToHandler(IItemHandler externalHandler, Direction toSide) {
        // 检查输出槽是否有物品
        var outputSlot = this.inventory.getStackInSlot(0);
        if (outputSlot.isEmpty()) return;

        ItemStack remaining = outputSlot.copy();

        for (int i = 0; i < externalHandler.getSlots() && !remaining.isEmpty(); i++) {
            ItemStack insertResult = externalHandler.insertItem(i, remaining, false);
            int transferred = remaining.getCount() - insertResult.getCount();

            if (transferred > 0) {
                outputSlot.shrink(transferred);
                remaining = insertResult;
            }
        }

        if (!remaining.equals(outputSlot)) {
            this.setChanged();
        }
    }


    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction direction) {
        if (sideConfig.getSideMode(direction).canOutput()) {
            return new int[0];
        } else return new int[]{1};
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return ioHandler.shouldAllowPassiveIO(direction);
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
