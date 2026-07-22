package committee.nova.mods.avaritia.common.tile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.tile.BaseTileEntity;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.common.menu.provider.ChannelMenuProvider;
import committee.nova.mods.avaritia.common.menu.provider.ChannelSelectMenuProvider;
import committee.nova.mods.avaritia.core.channel.ChannelInfo;
import committee.nova.mods.avaritia.core.channel.IChannelTerminal;
import committee.nova.mods.avaritia.core.channel.NullChannel;
import committee.nova.mods.avaritia.core.channel.ServerChannel;
import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Persistent terminal metadata with stable dynamically delegated capabilities. */
public class TesseractTile extends BaseTileEntity implements IChannelTerminal {
    private static final byte DEFAULT_SORT_TYPE = 4;
    private static final Codec<ChannelInfo> LEGACY_CHANNEL_INFO_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("channelOwner").forGetter(ChannelInfo::owner),
            Codec.intRange(0, ChannelInfo.MAX_ID).fieldOf("channelID").forGetter(ChannelInfo::id)
    ).apply(instance, ChannelInfo::new));

    private final ResourceHandler<ItemResource> itemHandler = new DynamicItemHandler();
    private final ResourceHandler<FluidResource> fluidHandler = new DynamicFluidHandler();
    private final EnergyHandler energyHandler = new DynamicEnergyHandler();

    private @Nullable UUID owner;
    private boolean locked;
    private boolean craftingMode;
    private String filter = "";
    private byte sortType = DEFAULT_SORT_TYPE;
    private byte viewType;
    private @Nullable ChannelInfo channelInfo;

    public TesseractTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.TESSERACT_TILE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TesseractTile tile) {
        if (level.isClientSide()) return;
        ServerChannel channel = tile.getChannel();
        if (channel.isRemoved()) {
            if (tile.channelInfo != null) tile.setChannel(null);
            return;
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            channel.insert(new FluidStack(Fluids.WATER, 1_000));
        }
    }

    public void inhaleItem(ItemEntity entity) {
        if (level == null || level.isClientSide() || entity.isRemoved()) return;
        ItemStack stack = entity.getItem();
        getChannel().insert(stack);
        if (stack.isEmpty()) {
            entity.discard();
        } else {
            entity.teleportTo(worldPosition.getX() + 0.5, worldPosition.getY() - 0.26,
                    worldPosition.getZ() + 0.5);
            entity.setDeltaMovement(0.0, -0.1, 0.0);
        }
    }

    public ResourceHandler<ItemResource> itemHandler() {
        return itemHandler;
    }

    public ResourceHandler<FluidResource> fluidHandler() {
        return fluidHandler;
    }

    public EnergyHandler energyHandler() {
        return energyHandler;
    }

    public ServerChannel getChannel() {
        if (level == null || level.isClientSide() || channelInfo == null) return NullChannel.INSTANCE;
        ServerChannelManager manager = ServerChannelManager.getInstance();
        return manager == null ? NullChannel.INSTANCE : manager.getChannel(channelInfo);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
        locked = input.getBooleanOr("locked", false);
        craftingMode = input.getBooleanOr("craftingMode", false);
        filter = sanitizeFilter(input.getStringOr("filter", ""));
        sortType = sanitizeSort((byte) input.getIntOr("sortType", DEFAULT_SORT_TYPE));
        viewType = sanitizeView((byte) input.getIntOr("viewType", 0));
        channelInfo = input.read("channel", ChannelInfo.CODEC)
                .or(() -> input.read("channel", LEGACY_CHANNEL_INFO_CODEC))
                .orElse(null);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (owner != null) output.store("owner", UUIDUtil.CODEC, owner);
        output.putBoolean("locked", locked);
        output.putBoolean("craftingMode", craftingMode);
        output.putString("filter", filter);
        output.putInt("sortType", sortType);
        output.putInt("viewType", viewType);
        if (channelInfo != null) output.store("channel", ChannelInfo.CODEC, channelInfo);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.avaritia.tesseract");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return channelInfo == null ? new TesseractChannelMenu(id, inventory, this, worldPosition)
                : new TesseractMenu(id, inventory, this);
    }

    public void openSelector(ServerPlayer player) {
        player.openMenu(new ChannelSelectMenuProvider(this), buffer -> {
            buffer.writeBlockPos(worldPosition);
            buffer.writeUUID(getTerminalOwner());
        });
    }

    @Override
    public void openMainMenu(ServerPlayer player) {
        if (channelInfo == null) {
            openSelector(player);
            return;
        }
        player.openMenu(new ChannelMenuProvider(this), buffer -> {
            buffer.writeBlockPos(worldPosition);
            buffer.writeUUID(getTerminalOwner());
            buffer.writeBoolean(locked);
            buffer.writeBoolean(craftingMode);
            buffer.writeUtf(filter, 64);
            buffer.writeByte(sortType);
            buffer.writeByte(viewType);
        });
    }

    @Override
    public UUID getTerminalOwner() {
        return owner == null ? Const.AVARITIA_FAKE_PLAYER.id() : owner;
    }

    @Override
    public @Nullable ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        stack.set(DataComponents.BLOCK_ENTITY_DATA,
                TypedEntityData.of(getType(), saveCustomOnly(registries)));
    }

    @Override
    public void setChannel(@Nullable ChannelInfo channel) {
        channelInfo = channel;
        setChanged();
    }

    @Override
    public boolean canPlayerModify(Player player) {
        return owner == null || !locked || owner.equals(player.getUUID());
    }

    @Override
    public boolean stillValid(Player player) {
        return !isRemoved() && level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    public @Nullable UUID getOwner() { return owner; }
    public boolean isLocked() { return locked; }
    public boolean isCraftingMode() { return craftingMode; }
    public String getFilter() { return filter; }
    public byte getSortType() { return sortType; }
    public byte getViewType() { return viewType; }

    public void setOwner(@Nullable UUID owner) { this.owner = owner; setChanged(); }
    public void setLocked(boolean locked) { this.locked = locked; setChanged(); }
    public void setCraftingMode(boolean craftingMode) { this.craftingMode = craftingMode; setChanged(); }
    public void setFilter(String filter) { this.filter = sanitizeFilter(filter); setChanged(); }
    public void setSortType(byte sortType) { this.sortType = sanitizeSort(sortType); setChanged(); }
    public void setViewType(byte viewType) { this.viewType = sanitizeView(viewType); setChanged(); }

    private static String sanitizeFilter(String value) {
        String safe = value == null ? "" : value;
        return safe.substring(0, Math.min(64, safe.length()));
    }

    private static byte sanitizeSort(byte value) {
        return value >= 0 && value <= 7 ? value : DEFAULT_SORT_TYPE;
    }
    private static byte sanitizeView(byte value) { return value >= 0 && value <= 2 ? value : 0; }

    private final class DynamicItemHandler implements ResourceHandler<ItemResource> {
        private ResourceHandler<ItemResource> target() { return getChannel().items(); }
        @Override public int size() { return target().size(); }
        @Override public ItemResource getResource(int index) { return target().getResource(index); }
        @Override public long getAmountAsLong(int index) { return target().getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return target().getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, ItemResource resource) { return target().isValid(index, resource); }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return target().insert(index, resource, amount, transaction); }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return target().extract(index, resource, amount, transaction); }
    }

    private final class DynamicFluidHandler implements ResourceHandler<FluidResource> {
        private ResourceHandler<FluidResource> target() { return getChannel().fluids(); }
        @Override public int size() { return target().size(); }
        @Override public FluidResource getResource(int index) { return target().getResource(index); }
        @Override public long getAmountAsLong(int index) { return target().getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, FluidResource resource) { return target().getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, FluidResource resource) { return target().isValid(index, resource); }
        @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) { return target().insert(index, resource, amount, transaction); }
        @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) { return target().extract(index, resource, amount, transaction); }
    }

    private final class DynamicEnergyHandler implements EnergyHandler {
        private EnergyHandler target() { return getChannel().energy(); }
        @Override public long getAmountAsLong() { return target().getAmountAsLong(); }
        @Override public long getCapacityAsLong() { return target().getCapacityAsLong(); }
        @Override public int insert(int amount, TransactionContext transaction) { return target().insert(amount, transaction); }
        @Override public int extract(int amount, TransactionContext transaction) { return target().extract(amount, transaction); }
    }
}
