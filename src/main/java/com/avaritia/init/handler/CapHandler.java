package com.avaritia.init.handler;

import com.avaritia.Const;
import com.avaritia.api.iface.item.IItemCapability;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

/**
 * CapHandler
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/7 1:48
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class CapHandler {
    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Holder<Item> entry : ModItems.ITEMS.getEntries()) {
            Item item = entry.value();
            if (item instanceof IItemCapability iItemCapability) {
                iItemCapability.attachCapabilities(event);
            }
        }

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModTileEntities.compressed_chest_tile.get(),
                (be, side) -> new ContainerItemHandler(be)
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModTileEntities.INFINITY_CHEST_TILE.get(),
                (be, side) -> new ContainerItemHandler(be.chest)
        );

        var sidedVanillaContainers = List.of(
                ModTileEntities.neutron_collector_tile.get(),
                ModTileEntities.neutron_compressor_tile.get()
        );

        for (var type : sidedVanillaContainers) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (sidedContainer, side) -> {
                return new ContainerItemHandler(sidedContainer, side);
            });
        }

    }

    private record ContainerItemHandler(Container container, net.minecraft.core.Direction side) implements IItemHandler {
        private ContainerItemHandler(Container container) {
            this(container, null);
        }

        @Override
        public int getSlots() {
            if (this.container instanceof WorldlyContainer sidedContainer && this.side != null) {
                return sidedContainer.getSlotsForFace(this.side).length;
            }
            return this.container.getContainerSize();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            int containerSlot = this.toContainerSlot(slot);
            return this.isValidSlot(containerSlot) ? this.container.getItem(containerSlot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int containerSlot = this.toContainerSlot(slot);
            if (stack.isEmpty() || !this.isValidSlot(containerSlot) || !this.canPlace(containerSlot, stack)) {
                return stack;
            }

            ItemStack existing = this.container.getItem(containerSlot);
            int limit = Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                    return stack;
                }
                limit -= existing.getCount();
            }

            if (limit <= 0) {
                return stack;
            }

            int inserted = Math.min(limit, stack.getCount());
            if (!simulate) {
                ItemStack result = existing.isEmpty() ? stack.copyWithCount(inserted) : existing.copyWithCount(existing.getCount() + inserted);
                this.container.setItem(containerSlot, result);
                this.container.setChanged();
            }

            return stack.copyWithCount(stack.getCount() - inserted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int containerSlot = this.toContainerSlot(slot);
            if (amount <= 0 || !this.isValidSlot(containerSlot)) {
                return ItemStack.EMPTY;
            }

            ItemStack existing = this.container.getItem(containerSlot);
            if (existing.isEmpty() || !this.canTake(containerSlot, existing)) {
                return ItemStack.EMPTY;
            }

            int extracted = Math.min(amount, existing.getCount());
            ItemStack result = existing.copyWithCount(extracted);
            if (!simulate) {
                existing.shrink(extracted);
                if (existing.isEmpty()) {
                    this.container.setItem(containerSlot, ItemStack.EMPTY);
                }
                this.container.setChanged();
            }

            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return this.container.getMaxStackSize(this.getStackInSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            int containerSlot = this.toContainerSlot(slot);
            return this.isValidSlot(containerSlot) && this.canPlace(containerSlot, stack);
        }

        private int toContainerSlot(int slot) {
            if (this.container instanceof WorldlyContainer sidedContainer && this.side != null) {
                int[] slots = sidedContainer.getSlotsForFace(this.side);
                return slot >= 0 && slot < slots.length ? slots[slot] : -1;
            }
            return slot;
        }

        private boolean isValidSlot(int slot) {
            return slot >= 0 && slot < this.container.getContainerSize();
        }

        private boolean canPlace(int slot, ItemStack stack) {
            if (this.container instanceof WorldlyContainer sidedContainer && this.side != null) {
                return sidedContainer.canPlaceItemThroughFace(slot, stack, this.side);
            }
            return this.container.canPlaceItem(slot, stack);
        }

        private boolean canTake(int slot, ItemStack stack) {
            return !(this.container instanceof WorldlyContainer sidedContainer && this.side != null) || sidedContainer.canTakeItemThroughFace(slot, stack, this.side);
        }
    }

}
