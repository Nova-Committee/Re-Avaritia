package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.container.OffsetContainer;
import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.api.iface.IChangePage;
import committee.nova.mods.avaritia.api.utils.InventoryUtils;
import committee.nova.mods.avaritia.common.wrappers.RingStorageWrapper;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/6 上午12:38
 * @Description:
 */
public class _NeutronRingMenu extends BaseMenu implements IChangePage {
    public ItemStack ring = ItemStack.EMPTY;
    public int slot;
    private final Inventory playerInventory;
    private RingStorageWrapper storageWrapper;
    private DataSlot pageData;
    private ContainerData countData;
    private int mainInventorySize;
    private final int swapIndex;

    public _NeutronRingMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readInt(), OffsetContainer.dummy(54));
    }

    public _NeutronRingMenu(int id, Inventory playerInventory, int slot, OffsetContainer container) {
        super(ModMenus.neutron_ring.get(), id, playerInventory);
        this.swapIndex = -1;
        this.playerInventory = playerInventory;
        this.slot = slot;
        if (slot > -1) {
            this.ring = playerInventory.getItem(slot);
        }
        if (ring.isEmpty()) {
            this.ring = InventoryUtils.findItemInInv(playerInventory.player, stack -> stack.is(ModItems.neutron_ring.get()), stack -> stack);
        }
        ring.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
            this.storageWrapper = (RingStorageWrapper) itemHandler;
            this.pageData = this.storageWrapper.pageData;
            this.countData = this.storageWrapper.countData;
            this.mainInventorySize = playerInventory.items.size() + this.storageWrapper.getSlots();
        });

        int rows = ModConfig.inventoryRows.get();
        int offset = (rows - 4) * 18;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(container, j + i * 9, 8 + j * 18, 36 + i * 18));
            }
        }
        createInventorySlots(playerInventory, 0, 38 + offset);

        this.addDataSlot(this.pageData);
        for (int i2 = 0; i2 < this.countData.getCount(); ++i2) {
            int finalI = i2;
            this.addDataSlot(new DataSlot() {
                private int lastKnownPage = -1;

                @Override
                public int get() {
                    return _NeutronRingMenu.this.countData.get(finalI);
                }

                @Override
                public void set(int value) {
                    _NeutronRingMenu.this.countData.set(finalI, value);
                }

                @Override
                public boolean checkAndClearUpdateFlag() {
                    if (super.checkAndClearUpdateFlag()) {
                        return true;
                    } else {
                        int page = _NeutronRingMenu.this.storageWrapper.getPage();
                        boolean flag = page != this.lastKnownPage;
                        this.lastKnownPage = page;
                        return flag;
                    }
                }
            });
        }
    }

    @Override
    public void changePage(int page) {
        Const.LOGGER.info("Changing page to {}", page);
        Const.LOGGER.info("Current page is {}", this.pageData.get());
        int nextPage = Mth.clamp(page, 0, ModConfig.maxPageLimit.get() - 1);
        if (nextPage != this.pageData.get()) {
            this.pageData.set(nextPage);
            Const.LOGGER.info("Page updated to {}", nextPage);
        } else {
            Const.LOGGER.info("Page remains unchanged at {}", nextPage);
        }
    }

    @Environment(EnvType.CLIENT)
    public int getMaxPage() {
        return ModConfig.maxPageLimit.get();
    }

    @Environment(EnvType.CLIENT)
    public int getCurrentPage() {
        return this.pageData.get();
    }

    @Environment(EnvType.CLIENT)
    public long getItemCount(int slot) {
        return Integer.toUnsignedLong(this.countData.get(slot));
    }

    @Environment(EnvType.CLIENT)
    public Inventory getPlayerInventory() {
        return this.playerInventory;
    }

    @Environment(EnvType.CLIENT)
    public int getSwapIndex() {
        return this.swapIndex;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int pIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot1 = this.slots.get(pIndex);
        if (slot1.hasItem()) {
            ItemStack slot1Item = slot1.getItem();
            itemStack = slot1Item.copy();

            if (pIndex < 6 * 9) {
                if (!this.moveItemStackTo(slot1Item, 6 * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slot1Item, 0, 6 * 9, false)) {
                return ItemStack.EMPTY;
            }


            if (slot1Item.isEmpty()) {
                slot1.setByPlayer(ItemStack.EMPTY);
            } else {
                slot1.setChanged();
            }
        }

        return itemStack;
    }

//    private void sort(Comparator<StorageItem> comparator, int index1, int index2) {
//        if (index1 >= 0 && index2 >= 0 && index1 < index2) {
//            int length = index2 - index1;
//            int h = length;
//            boolean loop = false;
//
//            while(h > 1 || loop) {
//                if (h > 1) {
//                    h = h * 10 / 13;
//                }
//
//                loop = false;
//
//                for(int i = 0; i < length - h; ++i) {
//                    int j = index1 + i;
//                    int k = j + h;
//                    StorageItem container1 = this.storageWrapper.getContainerInSlot(j);
//                    StorageItem container2 = this.storageWrapper.getContainerInSlot(k);
//                    boolean swap = container1.isEmpty() && !container2.isEmpty();
//                    if (!container1.isEmpty() && !container2.isEmpty()) {
//                        if (ItemHandlerHelper.canItemStacksStack(container1.getStack(), container2.getStack())) {
//                            long freeSpace = this.storageWrapper.getSlotFreeSpace(j);
//                            if (freeSpace > 0L) {
//                                long size = Math.min(container2.getCount(), freeSpace);
//                                container1.grow(size);
//                                container2.shrink(size);
//                                loop = true;
//                            }
//                        }
//
//                        swap = comparator.compare(container1, container2) > 0;
//                    }
//
//                    if (swap) {
//                        this.swap(j, k);
//                        loop = true;
//                    }
//                }
//            }
//        }
//    }
//
//    private void swap(int index1, int index2) {
//        StorageItem container1 = this.storageWrapper.removeContainerInSlot(index1);
//        StorageItem container2 = this.storageWrapper.removeContainerInSlot(index2);
//        if (!container2.isEmpty()) {
//            this.storageWrapper.setContainerInSlot(index1, container2);
//        }
//
//        if (!container1.isEmpty()) {
//            this.storageWrapper.setContainerInSlot(index2, container1);
//        }
//
//    }
//
//    @Override
//    public void clicked(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull Player player) {
//        Slot slot = slotId >= 0 && slotId < this.mainInventorySize ? this.getSlot(slotId) : null;
//        if (slot == null) {
//            super.clicked(slotId, dragType, clickTypeIn, player);
//        } else {
//            ItemStack grabbedStack;
//            ItemStack stackInSlot;
//            ItemStack copy;
//            int i;
//            if (slotId < this.storageWrapper.getSlots()) {
//                this.swapIndex = clickTypeIn == ClickType.PICKUP ? this.swapIndex : -1;
//                int size;
//                if (clickTypeIn == ClickType.PICKUP) {
//                    grabbedStack = getCarried().copy();
//                    stackInSlot = slot.getItem();
//                    if (this.swapIndex != -1) {
//                        dragType = 2;
//                    }
//
//                    if (dragType == 0) {
//                        if (grabbedStack.isEmpty()) {
//                            if (!stackInSlot.isEmpty()) {
//                                copy = slot.remove(stackInSlot.getMaxStackSize());
//                                setCarried(copy);
//                            }
//                        } else {
//                            if (stackInSlot.isEmpty()) {
//                                slot.set(grabbedStack);
//                                setCarried(ItemStack.EMPTY);
//                            }
//
//                            if (ItemHandlerHelper.canItemStacksStack(grabbedStack, stackInSlot) && this.moveItemStackTo(grabbedStack, slotId, slotId + 1, false)) {
//                                setCarried(grabbedStack);
//                            }
//                        }
//                    } else if (dragType == 1) {
//                        if (grabbedStack.isEmpty()) {
//                            if (!stackInSlot.isEmpty()) {
//                                long count = this.storageWrapper.getContainerInSlot(slotId).getCount();
//                                size = (int)Math.min(count, stackInSlot.getMaxStackSize()) / 2;
//                                ItemStack result = slot.remove(size);
//                                setCarried(result);
//                            }
//                        } else {
//                            if (stackInSlot.isEmpty()) {
//                                slot.set(grabbedStack.split(1));
//                                setCarried(grabbedStack);
//                            }
//
//                            if (ItemHandlerHelper.canItemStacksStack(grabbedStack, stackInSlot) && this.storageWrapper.getSlotFreeSpace(slotId) > 0L) {
//                                grabbedStack.shrink(1);
//                                this.storageWrapper.getContainerInSlot(slotId).grow(1L);
//                                setCarried(grabbedStack);
//                            }
//                        }
//                    } else if (dragType == 2) {
//                        if (this.swapIndex >= 0) {
//                            //this.swap(this.swapIndex, slotId);
//                            this.swapIndex = -1;
//                        } else {
//                            this.swapIndex = slotId;
//                        }
//                    } else if (dragType == 3 && !stackInSlot.isEmpty()) {
//                        copy = ItemHandlerHelper.copyStackWithSize(stackInSlot, 1);
//                        if (this.moveItemStackTo(copy, this.storageWrapper.getSlots(), this.mainInventorySize, true)) {
//                            this.storageWrapper.getContainerInSlot(slotId).shrink(1L);
//                            if (this.storageWrapper.getContainerInSlot(slotId).isEmpty()) {
//                                slot.set(ItemStack.EMPTY);
//                            }
//                        }
//                    }
//                } else if (clickTypeIn == ClickType.THROW) {
//                    grabbedStack = getCarried();
//                    if (grabbedStack.isEmpty()) {
//                        stackInSlot = slot.getItem();
//                        if (!stackInSlot.isEmpty()) {
//                            StorageItem container = this.storageWrapper.getContainerInSlot(slotId);
//                            int stackSize = dragType == 1 ? stackInSlot.getMaxStackSize() : 1;
//                            size = (int)Math.min(stackSize, container.getCount());
//                            player.drop(ItemHandlerHelper.copyStackWithSize(stackInSlot, size), false);
//                            container.shrink(size);
//                            if (container.isEmpty()) {
//                                slot.set(ItemStack.EMPTY);
//                            }
//                        }
//                    }
//                } else if (clickTypeIn == ClickType.QUICK_MOVE) {
//                    if (dragType == 2) {
//                        grabbedStack = slot.getItem().copy();
//                        if (!grabbedStack.isEmpty()) {
//                            for(i = 0; i < this.storageWrapper.getSlots(); ++i) {
//                                copy = this.getSlot(i).getItem();
//                                if (ItemHandlerHelper.canItemStacksStack(grabbedStack, copy)) {
//                                    while(!this.quickMoveStack(player, i).isEmpty()) {
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        this.quickMoveStack(player, slotId);
//                    }
//                } else if (clickTypeIn == ClickType.CLONE) {
//                    Comparator<StorageItem> comparator = SortUtils.DEFAULT_1;
//                    if (dragType == 2) {
//                        comparator = SortUtils.DEFAULT_2;
//                    } else if (dragType == 3) {
//                        comparator = SortUtils.DEFAULT_3;
//                    }
//
//                    this.sort(comparator, 0, this.storageWrapper.getSlots());
//                }
//            } else {
//                this.swapIndex = -1;
//                if (clickTypeIn == ClickType.PICKUP) {
//                    if (dragType == 3) {
//                        grabbedStack = slot.getItem();
//                        if (!grabbedStack.isEmpty()) {
//                            stackInSlot = ItemHandlerHelper.copyStackWithSize(grabbedStack, 1);
//                            if (this.moveItemStackTo(stackInSlot, 0, this.storageWrapper.getSlots(), false)) {
//                                grabbedStack.shrink(1);
//                            }
//                        }
//                    }
//                } else if (clickTypeIn == ClickType.QUICK_MOVE && dragType == 2) {
//                    grabbedStack = slot.getItem().copy();
//                    if (!grabbedStack.isEmpty()) {
//                        for(i = this.storageWrapper.getSlots(); i < this.mainInventorySize; ++i) {
//                            copy = this.getSlot(i).getItem();
//                            if (ItemHandlerHelper.canItemStacksStack(grabbedStack, copy)) {
//                                this.quickMoveStack(player, i);
//                            }
//                        }
//                    }
//                }
//                super.clicked(slotId, dragType, clickTypeIn, player);
//            }
//        }
//    }


}
