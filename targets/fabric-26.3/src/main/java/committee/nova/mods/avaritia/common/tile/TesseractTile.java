package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.tile.BaseTileEntity;
import committee.nova.mods.avaritia.api.util.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.common.net.channel.ChannelAction;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelActionPack;
import committee.nova.mods.avaritia.core.channel.*;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import committee.nova.mods.avaritia.core.io.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/1/31 15:28
 * @Description:
 */
public class TesseractTile extends BaseTileEntity implements IChannelTerminal, committee.nova.mods.avaritia.core.io.NativeInventory, committee.nova.mods.avaritia.core.io.FluidStorageProvider, committee.nova.mods.avaritia.core.io.EnergyStorageProvider {
    private final int slotIndex;
    @Getter
    private UUID owner;
    @Getter
    private boolean locked = false;
    @Getter
    private boolean craftingMode = false;
    @Getter
    private String filter = "";
    @Getter
    private byte sortType = 4;
    @Getter
    private byte viewType = 0;
    @Getter
    private UUID channelOwner;
    @Getter
    private int channelID = -1;
    private boolean waterlogged = false;
    private final HashSet<ServerPlayer> channelSelectors = new HashSet<>();

    private ServerChannel channel = NullChannel.INSTANCE;
    private int[] automationSlots = committee.nova.mods.avaritia.core.io.NativeInventory.EMPTY_SLOTS;


    public TesseractTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.tesseract_tile.get(), pos, state);
        this.slotIndex = -2;
        onBlockStateChange();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TesseractTile blockEntity) {
        if (level.isClientSide()) return;
        blockEntity.bindChannel();
        blockEntity.onBlockStateChange();
        if (blockEntity.channel.isRemoved()) {
            if (blockEntity.channelID >= 0) blockEntity.setChannel(null, -1);
        } else {
            if (blockEntity.waterlogged) blockEntity.channel.addFluid(new FluidStack(Fluids.WATER, 1000));
        }
    }

    public void onBlockStateChange() {
        BlockState state = getBlockState();
        waterlogged = state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        input.read("owner", UUIDUtil.CODEC).ifPresent(uuid -> {
            owner = uuid;
            locked = input.getBooleanOr("locked", false);
        });
        craftingMode = input.getBooleanOr("craftingMode", false);
        filter = input.getStringOr("filter", "");
        sortType = input.getByteOr("sortType", (byte) 4);
        viewType = input.getByteOr("viewType", (byte) 0);
        ValueInput channelData = input.childOrEmpty("channel");
        channelOwner = channelData.read("channelOwner", UUIDUtil.CODEC).orElse(null);
        channelID = channelData.getIntOr("channelID", -1);
        if (channelID >= 0 && channelOwner != null && level != null && !level.isClientSide() && ServerChannelManager.getInstance() != null)
            channel = ServerChannelManager.getInstance().getChannel(channelOwner, channelID);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (owner != null) {
            output.store("owner", UUIDUtil.CODEC, owner);
            output.putBoolean("locked", locked);
        }
        output.putBoolean("craftingMode", craftingMode);
        output.putString("filter", filter);
        output.putByte("sortType", sortType);
        output.putByte("viewType", viewType);
        if (channelID >= 0) {
            ValueOutput channel = output.child("channel");
            channel.store("channelOwner", UUIDUtil.CODEC, channelOwner);
            channel.putInt("channelID", channelID);
        }
    }

    /**
     * 26.3 中方块实体移除前钩子。不调用 super：默认实现检测到本类是 Container
     * （NativeInventory）会调用 Containers.dropContents，把虚拟频道存储中每种物品
     * 最多散落一组（64 个）到世界；频道数据在 ServerChannelManager 中不受影响，
     * 散落纯属复制。
     */
    @Override
    public void preRemoveSideEffects(@NotNull BlockPos pos, @NotNull BlockState state) {
    }

    @Override
    public @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pInventory, @NotNull Player pPlayer) {
        return new TesseractMenu(pContainerId, pInventory.player, this, slotIndex);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("block.avaritia.tesseract").build();
    }

    private void bindChannel() {
        if (level != null && !level.isClientSide() && ServerChannelManager.getInstance() != null)
            channel = ServerChannelManager.getInstance().getChannel(channelOwner, channelID);
    }

    public ServerChannel getChannel() { bindChannel(); return channel; }

    @Override
    public committee.nova.mods.avaritia.api.common.wrapper.ItemHandler getItemHandler(Direction side) { bindChannel(); return isRemoved() || channel.isRemoved() ? null : channel; }
    @Override
    public int[] getSlotsForFace(Direction side) {
        var handler = getItemHandler(side);
        return handler == null ? committee.nova.mods.avaritia.core.io.NativeInventory.EMPTY_SLOTS : (automationSlots = committee.nova.mods.avaritia.core.io.NativeInventory.slotIndices(handler.getSlots(), automationSlots));
    }
    @Override
    public committee.nova.mods.avaritia.core.io.FluidHandler getFluidHandler(Direction side) { bindChannel(); return isRemoved() || channel.isRemoved() ? null : channel; }
    @Override
    public committee.nova.mods.avaritia.core.io.EnergyStorage getEnergyHandler(Direction side) { bindChannel(); return isRemoved() || channel.isRemoved() ? null : channel; }
    @Override
    public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64; }

    @Override
    public UUID getTerminalOwner() {
        return owner;
    }

    @Override
    public @Nullable ChannelInfo getChannelInfo() {
        if (channelID >= 0) return new ChannelInfo(channelOwner, channelID);
        return null;
    }

    @Override
    public void setChannel(UUID channelOwner, int channelID) {
        this.channelOwner = channelOwner;
        this.channelID = channelID;
        this.setChanged();
        channelSelectors.forEach(player -> ServerChannelManager.sendChannelSet(player, owner, channelOwner, channelID));
        this.channel = ServerChannelManager.getInstance().getChannel(channelOwner, channelID);
    }

    @Override
    public void removeChannel(ServerPlayer actor) {
        if (channelOwner == null) return;
        if (channelOwner.equals(actor.getUUID()) || channelOwner.equals(Const.AVARITIA_FAKE_PLAYER.id())) {
            if (!ServerChannelManager.getInstance().tryRemoveChannel(channelOwner, channelID)) return;
            clearChannel();
        }
    }

    @Override
    public void clearChannel() {
        this.channelID = -1;
        this.channelOwner = null;
        this.setChanged();
        channelSelectors.forEach(player -> NetworkHandler.CHANNEL.sendTo(player, new S2CChannelActionPack(ChannelAction.SET, (byte) -1, "", -1)));
        this.channel = NullChannel.INSTANCE;
    }

    @Override
    public void renameChannel(ServerPlayer actor, String name) {
        if (channelID < 0) return;
        if (actor.getUUID().equals(channelOwner) || channelOwner.equals(Const.AVARITIA_FAKE_PLAYER.id()))
            ServerChannelManager.getInstance().renameChannel(new ChannelInfo(channelOwner, channelID), name);
    }

    @Override
    public void addChannelSelector(ServerPlayer player) {
        channelSelectors.add(player);
        if (channelID < 0) return;
        ServerChannelManager.sendChannelSet(player, owner, channelOwner, channelID);
    }

    @Override
    public void removeChannelSelector(ServerPlayer player) {
        channelSelectors.remove(player);
    }

    @Override
    public boolean stillValid() {
        return !isRemoved();
    }

    @Override
    public void tryReOpenMenu(ServerPlayer player) {
        if (channelID >= 0) this.getBlockState().useWithoutItem(level, player, new BlockHitResult(
                new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5), Direction.UP, worldPosition, false));
    }

    public void inhaleItem(ItemEntity itemEntity) {
        if (channel.isRemoved()) return;
        ItemStack itemStack = itemEntity.getItem();
        channel.addItem(itemStack);
        if (!itemStack.isEmpty()) {
            BlockPos blockPos = getBlockPos();
            itemEntity.teleportTo(blockPos.getX() + 0.5, blockPos.getY() - 0.26, blockPos.getZ() + 0.5);
            itemEntity.setDeltaMovement(0, -0.1, 0);
        }
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        this.setChanged();
    }

    public void setCraftingMode(Boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.setChanged();
    }

    public void setFilter(String filter) {
        this.filter = filter;
        this.setChanged();
    }

    public void setSortType(byte sortType) {
        this.sortType = sortType;
        this.setChanged();
    }

    public void setViewType(byte viewType) {
        this.viewType = viewType;
        this.setChanged();
    }

    public void setChannelOwner(UUID owner) {
        this.channelOwner = owner;
        this.setChanged();
    }

    public void setChannelId(int id) {
        this.channelID = id;
        this.setChanged();
    }
}
