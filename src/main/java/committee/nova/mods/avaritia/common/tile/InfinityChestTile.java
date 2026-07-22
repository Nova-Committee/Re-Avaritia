package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.tile.BaseTileEntity;
import committee.nova.mods.avaritia.common.component.InfinityChestReference;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.core.chest.ServerChestHandler;
import committee.nova.mods.avaritia.core.chest.ServerChestManager;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class InfinityChestTile extends BaseTileEntity implements LidBlockEntity, IItemHandler {
    private static final int DEFAULT_SORT_TYPE = 4;
    private static final int MAX_FILTER_LENGTH = 64;

    @Nullable
    private UUID owner;
    private boolean locked;
    private String filter = "";
    private byte sortType = DEFAULT_SORT_TYPE;
    private UUID channelID = UUID.randomUUID();
    private ServerChestHandler chest = new ServerChestHandler();

    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F,
                    level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F,
                    level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), 1, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof InfinityChestMenu menu && menu.isBoundTo(InfinityChestTile.this);
        }
    };

    public InfinityChestTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.INFINITY_CHEST_TILE.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.avaritia.infinity_chest");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new InfinityChestMenu(containerId, player, this);
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getFilter() {
        return filter;
    }

    public byte getSortType() {
        return sortType;
    }

    public UUID getChannelID() {
        return channelID;
    }

    public UUID getChestID() {
        return channelID;
    }

    public ServerChestHandler getChest() {
        if (level != null && !level.isClientSide && owner != null) {
            ServerChestManager manager = ServerChestManager.getInstance();
            if (manager != null) {
                chest = manager.getOrCreateChest(owner, channelID);
            }
        }
        return chest;
    }

    @Override
    public int getSlots() {
        return getChest().getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return getChest().getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return getChest().insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return getChest().extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return getChest().getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return getChest().isItemValid(slot, stack);
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        setChanged();
    }

    public void setFilter(String filter) {
        this.filter = sanitizeFilter(filter);
        setChanged();
    }

    public void setSortType(byte sortType) {
        this.sortType = sortType >= 0 && sortType <= 7 ? sortType : DEFAULT_SORT_TYPE;
        setChanged();
    }

    public void setChannelId(UUID channelID) {
        this.channelID = channelID;
        setChanged();
    }

    public void applyReference(InfinityChestReference reference) {
        owner = reference.owner();
        locked = reference.locked();
        filter = sanitizeFilter(reference.filter());
        sortType = reference.sortType();
        channelID = reference.channelId();
        getChest();
        setChanged();
    }

    public @Nullable InfinityChestReference createReference() {
        return owner == null ? null : new InfinityChestReference(owner, locked, filter, sortType, channelID);
    }

    public static @Nullable InfinityChestReference readReference(ItemStack stack) {
        InfinityChestReference reference = stack.get(ModDataComponents.INFINITY_CHEST_REFERENCE);
        if (reference != null) {
            return reference;
        }
        CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        reference = blockEntityData == null ? null : readReference(blockEntityData.copyTag());
        if (reference != null) {
            return reference;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : readReference(customData.copyTag());
    }

    public static @Nullable InfinityChestReference readReference(CompoundTag tag) {
        if (!tag.contains("owner")) {
            return null;
        }
        String channelKey = tag.contains("channelID") ? "channelID"
                : tag.contains("chestID") ? "chestID"
                : tag.contains("channelId") ? "channelId" : null;
        if (channelKey == null) {
            return null;
        }
        try {
            return new InfinityChestReference(
                    tag.getUUID("owner"),
                    tag.getBoolean("locked"),
                    tag.getString("filter"),
                    tag.contains("sortType") ? tag.getByte("sortType") : (byte) DEFAULT_SORT_TYPE,
                    tag.getUUID(channelKey)
            );
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        InfinityChestReference reference = readReference(tag);
        if (reference != null) {
            applyReference(reference);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putBoolean("locked", locked);
        tag.putString("filter", filter);
        tag.putByte("sortType", sortType);
        tag.putUUID("channelID", channelID);
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider provider) {
        InfinityChestReference reference = createReference();
        if (reference != null) {
            stack.set(ModDataComponents.INFINITY_CHEST_REFERENCE, reference);
        }
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        BlockItem.setBlockEntityData(stack, getType(), tag);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        InfinityChestReference reference = componentInput.get(ModDataComponents.INFINITY_CHEST_REFERENCE);
        if (reference != null) {
            applyReference(reference);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        InfinityChestReference reference = createReference();
        if (reference != null) {
            builder.set(ModDataComponents.INFINITY_CHEST_REFERENCE, reference);
        }
    }

    public void startOpen(Player player) {
        if (!remove && level != null && !player.isSpectator()) {
            openersCounter.incrementOpeners(player, level, worldPosition, getBlockState());
        }
    }

    public void stopOpen(Player player) {
        if (!remove && level != null && !player.isSpectator()) {
            openersCounter.decrementOpeners(player, level, worldPosition, getBlockState());
        }
    }

    public void recheckOpen() {
        if (!remove && level != null) {
            openersCounter.recheckOpeners(level, worldPosition, getBlockState());
        }
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, InfinityChestTile blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            chestLidController.shouldBeOpen(type > 0);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return chestLidController.getOpenness(partialTicks);
    }

    private static String sanitizeFilter(@Nullable String filter) {
        if (filter == null) {
            return "";
        }
        return filter.length() <= MAX_FILTER_LENGTH ? filter : filter.substring(0, MAX_FILTER_LENGTH);
    }
}
