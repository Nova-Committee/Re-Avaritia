package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.api.util.lang.Localizable;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 13:55
 * Version: 1.0
 */
public class NeutronCollectorTile extends BaseInventoryTileEntity implements ITileIO {

    // IO配置系统
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
        // 处理主动输出操作
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
        // 加载IO配置
        if (tag.contains("SideConfig")) {
            this.sideConfig = SideConfiguration.fromNBT(tag.getCompound("SideConfig"));
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("progress", progress);
        // 保存IO配置
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


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        // 检查被动输出配置
        if (side != null && cap == ForgeCapabilities.ITEM_HANDLER) {
            if (!ioHandler.shouldAllowPassiveIO(side)) {
                return net.minecraftforge.common.util.LazyOptional.empty();
            }
        }
        return super.getCapability(cap, side);
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




    /**
     * 处理主动输出操作
     */
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
        this.sideConfig = config;
        this.setChangedAndDispatch();

        // 同步给客户端
        if (!this.level.isClientSide()) {
            NetworkHandler.sendSideConfigSync(this.level, this.worldPosition, config);
        }
    }

    @Override
    public void setIOChange() {
        this.setChangedAndDispatch();
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
}
