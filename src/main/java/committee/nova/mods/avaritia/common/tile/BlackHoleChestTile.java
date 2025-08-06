package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.tile.BaseTileEntity;
import committee.nova.mods.avaritia.common.menu.ChannelMenu;
import committee.nova.mods.avaritia.common.net.channel.ChannelAction;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelActionPack;
import committee.nova.mods.avaritia.core.channel.*;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/1/31 15:28
 * @Description:
 */
public class BlackHoleChestTile extends BaseTileEntity implements IChannelTerminal {
    private static final Component CONTAINER_NAME = Component.translatable("container.infinity_chest");
    private final int slotIndex;
    @Getter private UUID owner;
    @Getter private boolean locked = false;
    @Getter private boolean craftingMode = false;
    @Getter private String filter = "";
    @Getter private byte sortType = 4;
    @Getter private byte viewType = 0;
    @Getter private UUID channelOwner;
    @Getter private int channelID = -1;
    private boolean waterlogged = false;
    private final HashSet<ServerPlayer> channelSelectors = new HashSet<>();

    @Getter private ServerChannel channel = NullChannel.INSTANCE;
    @Getter private LazyOptional<?> capability = LazyOptional.of(() -> channel);


    public BlackHoleChestTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.black_hole_chest_tile.get(), pos, state);
        this.slotIndex = -2;
        onBlockStateChange();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlackHoleChestTile blockEntity) {
        if (level.isClientSide) return;
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
    public void load(@NotNull CompoundTag pTag) {
        if (pTag.contains("owner")) {
            owner = pTag.getUUID("owner");
            locked = pTag.getBoolean("locked");
        }
        if (pTag.contains("craftingMode")) craftingMode = pTag.getBoolean("craftingMode");
        if (pTag.contains("filter")) filter = pTag.getString("filter");
        if (pTag.contains("sortType")) sortType = pTag.getByte("sortType");
        if (pTag.contains("viewType")) viewType = pTag.getByte("viewType");
        if (pTag.contains("channel")) {
            CompoundTag channel = pTag.getCompound("channel");
            channelOwner = channel.getUUID("channelOwner");
            channelID = channel.getInt("channelID");
        }
        channel = ServerChannelManager.getInstance().getChannel(channelOwner, channelID);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag pTag) {
        if (owner != null) {
            pTag.putUUID("owner", owner);
            pTag.putBoolean("locked", locked);
        }
        pTag.putBoolean("craftingMode", craftingMode);
        pTag.putString("filter", filter);
        pTag.putByte("sortType", sortType);
        pTag.putByte("viewType", viewType);
        if (channelID >= 0) {
            CompoundTag channel =  new CompoundTag();
            channel.putUUID("channelOwner", channelOwner);
            channel.putInt("channelID", channelID);
            pTag.put("channel", channel);
        }
    }

    @Override
    public @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pInventory, @NotNull Player pPlayer) {
        return new ChannelMenu(pContainerId, pInventory.player, this, slotIndex);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return CONTAINER_NAME;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (channel.isRemoved()) return LazyOptional.empty();
        if (cap == ForgeCapabilities.ITEM_HANDLER
                || cap == ForgeCapabilities.FLUID_HANDLER
                || cap == ForgeCapabilities.ENERGY) {
            return capability.cast();
        }
        return LazyOptional.empty();
    }

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
        this.capability = LazyOptional.of(() -> channel);
    }

    @Override
    public void removeChannel(ServerPlayer actor) {
        if (channelOwner == null) return;
        if (channelOwner.equals(actor.getUUID()) || channelOwner.equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
            if (!ServerChannelManager.getInstance().tryRemoveChannel(channelOwner, channelID)) return;
            this.channelID = -1;
            this.channelOwner = null;
            this.setChanged();
            channelSelectors.forEach(player -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CChannelActionPack(ChannelAction.SET, (byte) -1, "", -1)));
            this.channel = NullChannel.INSTANCE;
        }
    }

    @Override
    public void renameChannel(ServerPlayer actor, String name) {
        if (channelID < 0) return;
        if (actor.getUUID().equals(channelOwner) || channelOwner.equals(Const.AVARITIA_FAKE_PLAYER.getId()))
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
        if (channelID >= 0) this.getBlockState().use(level, player, InteractionHand.MAIN_HAND, new BlockHitResult(
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
