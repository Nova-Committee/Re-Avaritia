package com.avaritia.common.tile;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.avaritia.Const;
import com.avaritia.common.component.ClusterContainerContents;
import com.avaritia.common.menu.InfinityChestMenu;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponentGetter;

public class InfinityChestTile extends BlockEntity implements LidBlockEntity {

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {
            InfinityChestTile.this.signalOpenCount(level, pos, state, count, openCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof InfinityChestMenu)) {
                return false;
            } else {
                InfinityChestMenu menu = (InfinityChestMenu) player.containerMenu;
                InfinityChestTile tile = menu.getTile();
                return tile == InfinityChestTile.this;
            }
        }
    };

    private final ChestLidController chestLidController = new ChestLidController();

    public final SimpleContainer chest = new SimpleContainer(300) {
        @Override
        public void setChanged() {
            InfinityChestTile.this.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return this.getMaxStackSize();
        }

        @Override
        public void fillStackedContents(StackedContents stackedContents) {
            for (ItemStack itemStack : this.getItems()) {
                stackedContents.accountStack(itemStack, Integer.MAX_VALUE);
            }
        }

        @Override
        public void startOpen(Player player) {
            if (!InfinityChestTile.this.remove && !player.isSpectator()) {
                InfinityChestTile.this.openersCounter.incrementOpeners(player, InfinityChestTile.this.getLevel(), InfinityChestTile.this.getBlockPos(), InfinityChestTile.this.getBlockState());
            }
        }

        @Override
        public void stopOpen(Player player) {
            if (!InfinityChestTile.this.remove && !player.isSpectator()) {
                InfinityChestTile.this.openersCounter.decrementOpeners(player, InfinityChestTile.this.getLevel(), InfinityChestTile.this.getBlockPos(), InfinityChestTile.this.getBlockState());
            }
        }
    };

    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder), ExtraCodecs.intRange(1, Integer.MAX_VALUE).fieldOf("count").orElse(1).forGetter(ItemStack::getCount), DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(itemStack -> itemStack.getComponentsPatch())).apply(instance, ItemStack::new)));

    public InfinityChestTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.INFINITY_CHEST_TILE.get(), pos, state);
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, InfinityChestTile blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int eventId, int eventParam) {
        Block block = state.getBlock();
        level.blockEvent(pos, block, 1, eventParam);
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        BlockItem.setBlockEntityData(stack, this.getType(), tag);
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.chestLidController.getOpenness(partialTicks);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains("Items", Tag.TAG_LIST)) {
            ListTag tagList = tag.getList("Items", Tag.TAG_COMPOUND);

            for (int i = 0; i < tagList.size(); ++i) {
                CompoundTag compoundTag = tagList.getCompound(i);
                int j = compoundTag.getInt("Slot");
                if (j >= 0 && j < this.chest.getItems().size()) {
                    ItemStack itemStack = CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), compoundTag).resultOrPartial(error -> Const.LOGGER.error("Tried to load invalid item: '{}'", error)).orElse(ItemStack.EMPTY);
                    this.chest.getItems().set(j, itemStack);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        ListTag tagList = new ListTag();

        for (int i = 0; i < this.chest.getItems().size(); ++i) {
            ItemStack itemStack = this.chest.getItems().get(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putInt("Slot", i);
                CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), itemStack)
                            .resultOrPartial(error -> Const.LOGGER.error("Failed to encode item: '{}'", error))
                            .ifPresent(tagElement -> compoundTag.put("Item", tagElement));
                tagList.add(compoundTag);
            }
        }

        if (!tagList.isEmpty()) {
            tag.put("Items", tagList);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        super.applyImplicitComponents(componentInput);

        NonNullList<ItemStack> itemStacks = NonNullList.withSize(300, ItemStack.EMPTY);
        componentInput.getOrDefault(ModDataComponents.CLUSTER_CONTAINER, ClusterContainerContents.EMPTY).copyInto(itemStacks);

        if (!itemStacks.isEmpty()) {
            for (int i = 0; i < itemStacks.size(); i++) {
                this.chest.getItems().set(i, itemStacks.get(i));
            }
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        List<ItemStack> itemStacks = this.chest.getItems();
        builder.set(ModDataComponents.CLUSTER_CONTAINER, ClusterContainerContents.fromItems(itemStacks));
    }}