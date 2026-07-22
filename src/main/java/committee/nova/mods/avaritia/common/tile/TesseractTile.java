package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.tile.BaseTileEntity;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.common.menu.provider.ChannelMenuProvider;
import committee.nova.mods.avaritia.common.net.channel.ChannelAction;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelActionPack;
import committee.nova.mods.avaritia.core.channel.ChannelInfo;
import committee.nova.mods.avaritia.core.channel.IChannelTerminal;
import committee.nova.mods.avaritia.core.channel.NullChannel;
import committee.nova.mods.avaritia.core.channel.ServerChannel;
import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Tesseract terminal metadata and a stable, dynamically delegated capability proxy. */
public class TesseractTile extends BaseTileEntity implements IChannelTerminal, IItemHandler, IFluidHandler, IEnergyStorage {
    private static final int MAX_FILTER_LENGTH = 64;

    @Nullable
    private UUID owner;
    private boolean locked;
    private boolean craftingMode;
    private String filter = "";
    private byte sortType = 4;
    private byte viewType;
    @Nullable
    private UUID channelOwner;
    private int channelID = -1;
    private boolean waterlogged;
    private final Set<UUID> channelSelectors = new HashSet<>();

    public TesseractTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.tesseract_tile.get(), pos, state);
        onBlockStateChange();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TesseractTile tile) {
        if (level.isClientSide) {
            return;
        }
        ServerChannel channel = tile.getChannel();
        if (channel.isRemoved()) {
            if (tile.channelID >= 0) {
                tile.clearChannel();
            }
        } else if (tile.waterlogged) {
            channel.addFluid(new FluidStack(Fluids.WATER, 1_000));
        }
    }

    public void onBlockStateChange() {
        BlockState state = getBlockState();
        waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    public ServerChannel getChannel() {
        if (level == null || level.isClientSide || channelOwner == null || channelID < 0) {
            return NullChannel.INSTANCE;
        }
        ServerChannelManager manager = ServerChannelManager.getInstance();
        return manager == null ? NullChannel.INSTANCE : manager.getChannel(channelOwner, channelID);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.contains("owner") ? tag.getUUID("owner") : null;
        locked = tag.getBoolean("locked");
        craftingMode = tag.getBoolean("craftingMode");
        filter = sanitizeFilter(tag.getString("filter"));
        sortType = sanitizeSort(tag.contains("sortType") ? tag.getByte("sortType") : (byte) 4);
        viewType = sanitizeView(tag.getByte("viewType"));
        channelOwner = null;
        channelID = -1;
        if (tag.contains("channel")) {
            CompoundTag channel = tag.getCompound("channel");
            if (channel.contains("channelOwner") && channel.contains("channelID")) {
                channelOwner = channel.getUUID("channelOwner");
                channelID = channel.getInt("channelID");
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putBoolean("locked", locked);
        tag.putBoolean("craftingMode", craftingMode);
        tag.putString("filter", filter);
        tag.putByte("sortType", sortType);
        tag.putByte("viewType", viewType);
        if (channelOwner != null && channelID >= 0) {
            CompoundTag channel = new CompoundTag();
            channel.putUUID("channelOwner", channelOwner);
            channel.putInt("channelID", channelID);
            tag.put("channel", channel);
        }
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        BlockItem.setBlockEntityData(stack, getType(), tag);
    }

    @Override
    public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new TesseractMenu(containerId, player, this, -2);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.avaritia.tesseract");
    }

    public void openMainMenu(ServerPlayer player) {
        player.openMenu(new ChannelMenuProvider(this), buffer -> writeMenuData(buffer, player));
    }

    public void writeMenuData(net.minecraft.network.FriendlyByteBuf buffer, Player player) {
        UUID safeOwner = owner == null ? player.getUUID() : owner;
        UUID safeChannelOwner = channelOwner == null ? safeOwner : channelOwner;
        buffer.writeBlockPos(worldPosition);
        buffer.writeInt(-2);
        buffer.writeUUID(safeOwner);
        buffer.writeBoolean(locked);
        buffer.writeBoolean(craftingMode);
        buffer.writeUtf(filter, MAX_FILTER_LENGTH);
        buffer.writeByte(sortType);
        buffer.writeByte(viewType);
        buffer.writeUUID(safeChannelOwner);
        buffer.writeInt(channelID);
    }

    @Override
    public UUID getTerminalOwner() {
        return owner == null ? Const.AVARITIA_FAKE_PLAYER.getId() : owner;
    }

    @Override
    public @Nullable ChannelInfo getChannelInfo() {
        return channelOwner != null && channelID >= 0 ? new ChannelInfo(channelOwner, channelID) : null;
    }

    @Override
    public void setChannel(UUID newOwner, int newChannelId) {
        channelOwner = newOwner;
        channelID = newChannelId;
        setChanged();
        if (level != null && !level.isClientSide) {
            for (UUID viewer : Set.copyOf(channelSelectors)) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(viewer);
                if (player != null) {
                    ServerChannelManager.sendChannelSet(player, getTerminalOwner(), newOwner, newChannelId);
                }
            }
        }
    }

    private void clearChannel() {
        channelOwner = null;
        channelID = -1;
        setChanged();
    }

    @Override
    public void removeChannel(ServerPlayer actor) {
        if (channelOwner == null || channelID < 0 || !canManageChannel(actor, channelOwner)) {
            return;
        }
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (manager != null && manager.tryRemoveChannel(channelOwner, channelID)) {
            clearChannel();
            channelSelectors.stream()
                    .map(id -> actor.server.getPlayerList().getPlayer(id))
                    .filter(java.util.Objects::nonNull)
                    .forEach(player -> PacketDistributor.sendToPlayer(player,
                            new S2CChannelActionPack(ChannelAction.SET, (byte) -1, "", -1)));
        }
    }

    @Override
    public void renameChannel(ServerPlayer actor, String name) {
        if (channelOwner == null || channelID < 0 || !canManageChannel(actor, channelOwner)) {
            return;
        }
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (manager != null) {
            manager.renameChannel(new ChannelInfo(channelOwner, channelID), name);
        }
    }

    private static boolean canManageChannel(ServerPlayer actor, UUID selectedOwner) {
        if (selectedOwner.equals(actor.getUUID())) {
            return true;
        }
        return selectedOwner.equals(Const.AVARITIA_FAKE_PLAYER.getId()) && actor.hasPermissions(2);
    }

    @Override
    public void addChannelSelector(ServerPlayer player) {
        channelSelectors.add(player.getUUID());
        if (channelOwner != null && channelID >= 0) {
            ServerChannelManager.sendChannelSet(player, getTerminalOwner(), channelOwner, channelID);
        }
    }

    @Override
    public void removeChannelSelector(ServerPlayer player) {
        channelSelectors.remove(player.getUUID());
    }

    @Override
    public boolean stillValid() {
        return !isRemoved() && level != null && level.getBlockEntity(worldPosition) == this;
    }

    public boolean stillValid(Player player) {
        return stillValid()
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D
                && canPlayerModify(player);
    }

    public boolean canPlayerModify(Player player) {
        return !locked || owner == null || owner.equals(player.getUUID());
    }

    @Override
    public void tryReOpenMenu(ServerPlayer player) {
        if (channelOwner != null && channelID >= 0 && stillValid(player)) {
            openMainMenu(player);
        }
    }

    public void inhaleItem(ItemEntity entity) {
        ServerChannel channel = getChannel();
        if (channel.isRemoved()) {
            return;
        }
        ItemStack stack = entity.getItem();
        channel.addItem(stack);
        if (!stack.isEmpty()) {
            entity.teleportTo(worldPosition.getX() + 0.5D, worldPosition.getY() - 0.26D, worldPosition.getZ() + 0.5D);
            entity.setDeltaMovement(0.0D, -0.1D, 0.0D);
        }
    }

    public @Nullable UUID getOwner() { return owner; }
    public boolean isLocked() { return locked; }
    public boolean isCraftingMode() { return craftingMode; }
    public String getFilter() { return filter; }
    public byte getSortType() { return sortType; }
    public byte getViewType() { return viewType; }
    public @Nullable UUID getChannelOwner() { return channelOwner; }
    public int getChannelID() { return channelID; }

    public void setOwner(@Nullable UUID owner) { this.owner = owner; setChanged(); }
    public void setLocked(boolean locked) { this.locked = locked; setChanged(); }
    public void setCraftingMode(Boolean craftingMode) { this.craftingMode = Boolean.TRUE.equals(craftingMode); setChanged(); }
    public void setFilter(String filter) { this.filter = sanitizeFilter(filter); setChanged(); }
    public void setSortType(byte sortType) { this.sortType = sanitizeSort(sortType); setChanged(); }
    public void setViewType(byte viewType) { this.viewType = sanitizeView(viewType); setChanged(); }
    public void setChannelOwner(@Nullable UUID owner) { this.channelOwner = owner; setChanged(); }
    public void setChannelId(int id) { this.channelID = id; setChanged(); }

    private static String sanitizeFilter(String value) {
        String safe = value == null ? "" : value;
        return safe.substring(0, Math.min(MAX_FILTER_LENGTH, safe.length()));
    }

    private static byte sanitizeSort(byte value) { return value >= 0 && value <= 7 ? value : 4; }
    private static byte sanitizeView(byte value) { return value >= 0 && value <= 2 ? value : 0; }

    private ServerChannel dynamicChannel() { return getChannel(); }

    @Override public int getSlots() { return dynamicChannel().getSlots(); }
    @Override public @NotNull ItemStack getStackInSlot(int slot) { return dynamicChannel().getStackInSlot(slot); }
    @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return dynamicChannel().insertItem(slot, stack, simulate); }
    @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return dynamicChannel().extractItem(slot, amount, simulate); }
    @Override public int getSlotLimit(int slot) { return dynamicChannel().getSlotLimit(slot); }
    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return dynamicChannel().isItemValid(slot, stack); }
    @Override public int getTanks() { return dynamicChannel().getTanks(); }
    @Override public @NotNull FluidStack getFluidInTank(int tank) { return dynamicChannel().getFluidInTank(tank); }
    @Override public int getTankCapacity(int tank) { return dynamicChannel().getTankCapacity(tank); }
    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return dynamicChannel().isFluidValid(tank, stack); }
    @Override public int fill(FluidStack resource, FluidAction action) { return dynamicChannel().fill(resource, action); }
    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return dynamicChannel().drain(resource, action); }
    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return dynamicChannel().drain(maxDrain, action); }
    @Override public int receiveEnergy(int maxReceive, boolean simulate) { return dynamicChannel().receiveEnergy(maxReceive, simulate); }
    @Override public int extractEnergy(int maxExtract, boolean simulate) { return dynamicChannel().extractEnergy(maxExtract, simulate); }
    @Override public int getEnergyStored() { return dynamicChannel().getEnergyStored(); }
    @Override public int getMaxEnergyStored() { return dynamicChannel().getMaxEnergyStored(); }
    @Override public boolean canExtract() { return dynamicChannel().canExtract(); }
    @Override public boolean canReceive() { return dynamicChannel().canReceive(); }
}
