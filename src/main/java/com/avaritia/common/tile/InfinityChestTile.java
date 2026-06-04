package com.avaritia.common.tile;

import java.util.List;

import com.avaritia.common.component.ClusterContainerContents;
import com.avaritia.common.menu.InfinityChestMenu;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class InfinityChestTile extends BlockEntity implements LidBlockEntity {

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {
            InfinityChestTile.this.signalOpenCount(level, pos, state, count, openCount);
        }

        @Override
        public boolean isOwnContainer(Player player) {
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
        public void fillStackedContents(StackedItemContents stackedContents) {
            for (ItemStack itemStack : this.getItems()) {
                stackedContents.accountStack(itemStack, Integer.MAX_VALUE);
            }
        }

        @Override
        public void startOpen(ContainerUser containerUser) {
            if (!InfinityChestTile.this.remove && !containerUser.getLivingEntity().isSpectator()) {
                InfinityChestTile.this.openersCounter.incrementOpeners(containerUser.getLivingEntity(), InfinityChestTile.this.getLevel(), InfinityChestTile.this.getBlockPos(), InfinityChestTile.this.getBlockState(), containerUser.getContainerInteractionRange());
            }
        }

        @Override
        public void stopOpen(ContainerUser containerUser) {
            if (!InfinityChestTile.this.remove && !containerUser.getLivingEntity().isSpectator()) {
                InfinityChestTile.this.openersCounter.decrementOpeners(containerUser.getLivingEntity(), InfinityChestTile.this.getLevel(), InfinityChestTile.this.getBlockPos(), InfinityChestTile.this.getBlockState());
            }
        }
    };

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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.chest.clearContent();
        ContainerHelper.loadAllItems(input, this.chest.getItems());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.chest.getItems(), false);
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
