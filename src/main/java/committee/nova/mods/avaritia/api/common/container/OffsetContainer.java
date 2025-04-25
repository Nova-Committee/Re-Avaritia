package committee.nova.mods.avaritia.api.common.container;

import committee.nova.mods.avaritia.api.common.wrapper.OffsetItemStackWrapper;
import committee.nova.mods.avaritia.common.wrappers.StorageItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/14 22:58
 * @Description: 多页容器
 */
public interface OffsetContainer extends Container {

    static OffsetContainer dummy(int length) {
        final OffsetItemStackWrapper itemHandler = OffsetItemStackWrapper.dummy(length);
        return new OffsetContainer() {
            @Override
            public OffsetItemStackWrapper getItemHandler() {
                return itemHandler;
            }
            @Override
            public void setChanged() {
            }
            @Override
            public boolean stillValid(@NotNull Player pPlayer) {
                return OffsetContainer.super.stillValid(pPlayer);
            }
        };
    }

    static OffsetContainer create(Int2ObjectMap<StorageItem> containers, int offset, int length) {
        final OffsetItemStackWrapper itemHandler = OffsetItemStackWrapper.create(containers, offset, length);
        return new OffsetContainer() {
            @Override
            public OffsetItemStackWrapper getItemHandler() {
                return itemHandler;
            }
            @Override
            public void setChanged() {
            }
            @Override
            public boolean stillValid(@NotNull Player pPlayer) {
                return OffsetContainer.super.stillValid(pPlayer);
            }
        };
    }

    OffsetItemStackWrapper getItemHandler();
    default StorageItem getItemInSlot(int index) {
        return this.getItemHandler().getContainerInSlot(index);
    }

    default ContainerData getItemCount() {
        return new ContainerData() {
            @Override public int get(int index) {
                return index >= 0 && index < OffsetContainer.this.getContainerSize() ? (int) OffsetContainer.this.getItemInSlot(index).getCount() : 0;
            }
            @Override public void set(int index, int value) {
                if (index >= 0 && index < OffsetContainer.this.getContainerSize()) {
                    OffsetContainer.this.getItemInSlot(index).setCount(Integer.toUnsignedLong(value));
                }
            }
            @Override public int getCount() {
                return OffsetContainer.this.getContainerSize();
            }
        };
    }

    @Override
    default int getContainerSize() {
        return this.getItemHandler().getSlots();
    }

    @Override
    default boolean isEmpty() {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            if (!this.getItemInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default @NotNull ItemStack getItem(int index) {
        return this.getItemInSlot(index).getStack().copy();
    }

    @Override
    default @NotNull ItemStack removeItem(int index, int count) {
        return this.getItemHandler().extractItem(index, count, false);
    }

    @Override
    default @NotNull ItemStack removeItemNoUpdate(int index) {
        StorageItem container = this.getItemHandler().removeContainerInSlot(index);
        if (container.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack stack = container.getStack();
            stack.setCount((int) container.getCount());
            //int size = (int)Math.min(container.getCount(), stack.getMaxStackSize());
            return stack;
        }
    }

    @Override
    default void setItem(int index, @NotNull ItemStack stack) {
        this.getItemHandler().setStackInSlot(index, stack);
    }

    @Override
    default int getMaxStackSize() {
        return (int)Math.min(Integer.MAX_VALUE , ModConfig.slotStackLimit.get());
    }

    @Override
    default void clearContent() {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            this.getItemHandler().removeContainerInSlot(i);
        }
    }

    @Override
    default boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }
}
