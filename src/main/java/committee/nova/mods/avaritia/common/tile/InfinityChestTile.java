package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.tile.BaseTileEntity;
import committee.nova.mods.avaritia.api.util.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Infinity chest block entity backed by fixed local storage.
 */
public class InfinityChestTile extends BaseTileEntity implements LidBlockEntity {
    public static final int SLOT_COUNT = 243;
    public static final int MAX_STACK_SIZE = Integer.MAX_VALUE;
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_SLOT = "Slot";

    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, int eventId, int eventParam) {
            InfinityChestTile.this.signalOpenCount(level, pos, state, eventId, eventParam);
        }

        @Override
        protected boolean isOwnContainer(@NotNull Player player) {
            return player.containerMenu instanceof InfinityChestMenu menu && menu.getTile() == InfinityChestTile.this;
        }
    };

    public final SimpleContainer chest = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            InfinityChestTile.this.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return MAX_STACK_SIZE;
        }

        @Override
        public void fillStackedContents(@NotNull StackedContents contents) {
            for (int slot = 0; slot < this.getContainerSize(); slot++) {
                ItemStack stack = this.getItem(slot);
                contents.accountStack(stack, MAX_STACK_SIZE);
            }
        }

        @Override
        public void startOpen(@NotNull Player player) {
            if (!InfinityChestTile.this.remove && !player.isSpectator()) {
                InfinityChestTile.this.openersCounter.incrementOpeners(player, InfinityChestTile.this.getLevel(), InfinityChestTile.this.getBlockPos(), InfinityChestTile.this.getBlockState());
            }
        }

        @Override
        public void stopOpen(@NotNull Player player) {
            if (!InfinityChestTile.this.remove && !player.isSpectator()) {
                InfinityChestTile.this.openersCounter.decrementOpeners(player, InfinityChestTile.this.getLevel(), InfinityChestTile.this.getBlockPos(), InfinityChestTile.this.getBlockState());
            }
        }
    };

    public InfinityChestTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.infinity_chest_tile.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("block.avaritia.infinity_chest").build();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new InfinityChestMenu(containerId, playerInventory, this);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.chest.clearContent();
        if (!tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            return;
        }

        ListTag items = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); ++i) {
            CompoundTag itemTag = items.getCompound(i);
            int slot = itemTag.getInt(TAG_SLOT);
            if (slot < 0 || slot >= this.chest.getContainerSize()) {
                Const.LOGGER.warn("Skipping infinity chest item with invalid slot {} at {}", slot, this.getBlockPos());
                continue;
            }
            ItemStack stack = loadStoredItem(itemTag);
            if (!stack.isEmpty()) {
                this.chest.setItem(slot, stack);
            }
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        saveItems(tag);
    }

    @Override
    public void saveToItem(@NotNull ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        BlockItem.setBlockEntityData(stack, this.getType(), tag);
    }

    public void loadFromItem(ItemStack stack) {
        CompoundTag itemTag = stack.getTag();
        if (itemTag == null || !itemTag.contains(BlockItem.BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        this.load(itemTag.getCompound(BlockItem.BLOCK_ENTITY_TAG));
    }

    public int getStoredStackCount() {
        int count = 0;
        for (int slot = 0; slot < this.chest.getContainerSize(); slot++) {
            if (!this.chest.getItem(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, InfinityChestTile blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.chestLidController.getOpenness(partialTicks);
    }

    private void signalOpenCount(Level level, BlockPos pos, BlockState state, int eventId, int eventParam) {
        Block block = state.getBlock();
        level.blockEvent(pos, block, 1, eventParam);
    }

    private void saveItems(CompoundTag tag) {
        ListTag items = new ListTag();
        for (int slot = 0; slot < this.chest.getContainerSize(); ++slot) {
            ItemStack stack = this.chest.getItem(slot);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt(TAG_SLOT, slot);
                saveStoredItem(stack, itemTag);
                items.add(itemTag);
            }
        }
        if (!items.isEmpty()) {
            tag.put(TAG_ITEMS, items);
        }
    }

    private ItemStack loadStoredItem(CompoundTag tag) {
        String itemId = tag.getString("id");
        ResourceLocation itemName = ResourceLocation.tryParse(itemId);
        if (itemName == null) {
            Const.LOGGER.warn("Skipping infinity chest item with invalid id {} at {}", itemId, this.getBlockPos());
            return ItemStack.EMPTY;
        }

        Item item = ForgeRegistries.ITEMS.getValue(itemName);
        if (item == null) {
            Const.LOGGER.warn("Skipping infinity chest item with unknown id {} at {}", itemId, this.getBlockPos());
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item);
        stack.setCount(Math.max(1, tag.getInt("Count")));
        if (tag.contains("tag", Tag.TAG_COMPOUND)) {
            stack.setTag(tag.getCompound("tag"));
        }
        return stack;
    }

    private void saveStoredItem(ItemStack stack, CompoundTag tag) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) {
            Const.LOGGER.warn("Skipping infinity chest item with unregistered item {} at {}", stack, this.getBlockPos());
            return;
        }

        tag.putString("id", itemId.toString());
        tag.putInt("Count", stack.getCount());
        if (stack.getTag() != null) {
            tag.put("tag", stack.getTag().copy());
        }
    }
}
