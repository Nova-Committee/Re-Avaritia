package committee.nova.mods.avaritia.common.menu;

import java.util.Optional;

import javax.annotation.Nullable;

import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.common.container.slot.InfinitySlot;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InfinityChestMenu extends AbstractContainerMenu {

    private final InfinityChestTile tile;

    public InfinityChestMenu(int id, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        this(id, inventory, (InfinityChestTile) inventory.player.level().getBlockEntity(friendlyByteBuf.readBlockPos()));
    }

    public InfinityChestMenu(int id, Inventory inventory, InfinityChestTile tile) {
        super(ModMenus.infinity_chest.get(), id);
        this.tile = tile;
        this.tile.chest.startOpen(inventory.player);

        int y;
        for (y = 0; y < 9; y++) {
            for (int i = 0; i < 27; i++) {
                this.addSlot(new InfinitySlot(tile.chest, i + y * 27, 8 + i * 18, 17 + y * 18));
            }
        }

        for (y = 0; y < 3; y++) {
            for (int i = 0; i < 9; i++) {
                this.addSlot(new Slot(inventory, i + y * 9 + 9, 170 + i * 18, 193 + y * 18));
            }
        }
        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(inventory, x, 170 + x * 18, 251));
        }
    }

    public InfinityChestTile getTile() {
        return this.tile;
    }

    public static boolean canItemQuickReplace(Container inventory, @Nullable Slot slot, ItemStack stack, boolean stackSizeMatters, int maxStackSize) {
        boolean flag = slot == null || !slot.hasItem();
        return !flag && ItemStack.isSameItemSameComponents(stack, slot.getItem()) ? slot.getItem().getCount() + (stackSizeMatters ? 0 : stack.getCount()) < (slot instanceof InfinitySlot ? maxStackSize : stack.getMaxStackSize()) : flag;
    }

    public int getSlotMaxStack(Slot slot, ItemStack stack) {
        return slot instanceof InfinitySlot ? slot.getMaxStackSize() : slot.getMaxStackSize(stack);
    }

    public int getSlotMaxStack(Slot slot) {
        ItemStack itemStack = slot.getItem();
        return this.getSlotMaxStack(slot, itemStack);
    }

    public ItemStack safeInsert(Slot slot, ItemStack stack, int increment) {
        if (!stack.isEmpty() && slot.mayPlace(stack)) {
            ItemStack itemStack = slot.getItem();
            int i = Math.min(Math.min(increment, stack.getCount()), this.getSlotMaxStack(slot, stack) - itemStack.getCount());

            if (itemStack.isEmpty()) {
                slot.setByPlayer(stack.split(i));
            } else if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                stack.shrink(i);
                itemStack.grow(i);
                slot.setByPlayer(itemStack);
            }
            return stack;
        } else {
            return stack;
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        Inventory inventory = player.getInventory();

        if (clickType == ClickType.QUICK_CRAFT) {
            int i = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(button);
            if ((i != 1 || this.quickcraftStatus != 2) && i != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(button);
                if (isValidQuickcraftType(this.quickcraftType, player)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot slot = this.slots.get(slotId);
                ItemStack itemStack = this.getCarried();
                if (canItemQuickReplace(this.tile.chest, slot, itemStack, true, this.getSlotMaxStack(slot))
                        && slot.mayPlace(itemStack)
                        && (this.quickcraftType == 2 || itemStack.getCount() > this.quickcraftSlots.size())
                        && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int i1 = this.quickcraftSlots.iterator().next().index;
                        this.resetQuickCraft();
                        this.clicked(i1, this.quickcraftType, ClickType.PICKUP, player);
                        return;
                    }

                    ItemStack itemStack3 = this.getCarried().copy();
                    if (itemStack3.isEmpty()) {
                        this.resetQuickCraft();
                        return;
                    }

                    int k1 = this.getCarried().getCount();

                    for (Slot slot1 : this.quickcraftSlots) {
                        ItemStack itemStack1 = this.getCarried();
                        if (slot1 != null
                                && canItemQuickReplace(this.tile.chest, slot1, itemStack1, true, this.getSlotMaxStack(slot1))
                                && slot1.mayPlace(itemStack1)
                                && (this.quickcraftType == 2 || itemStack1.getCount() >= this.quickcraftSlots.size())
                                && this.canDragTo(slot1)) {
                            int j = slot1.hasItem() ? slot1.getItem().getCount() : 0;
                            int k = Math.min(this.tile.chest.getMaxStackSize(itemStack3), this.getSlotMaxStack(slot1, itemStack3));
                            int l = Math.min(getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemStack3) + j, k);
                            k1 -= l - j;
                            slot1.setByPlayer(itemStack3.copyWithCount(l));
                        }
                    }

                    itemStack3.setCount(k1);
                    this.setCarried(itemStack3);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
            ClickAction clickaction = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (slotId == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (clickaction == ClickAction.PRIMARY) {
                        player.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    } else {
                        player.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return;
                }

                Slot slot6 = this.slots.get(slotId);
                if (!slot6.mayPickup(player)) {
                    return;
                }

                ItemStack itemStack8 = this.quickMoveStack(player, slotId);

                while (!itemStack8.isEmpty() && ItemStack.isSameItem(slot6.getItem(), itemStack8)) {
                    itemStack8 = this.quickMoveStack(player, slotId);
                }
            } else {
                if (slotId < 0) {
                    return;
                }

                Slot slot7 = this.slots.get(slotId);
                ItemStack itemStack9 = slot7.getItem();
                ItemStack itemStack10 = this.getCarried();
                player.updateTutorialInventoryAction(itemStack10, slot7.getItem(), clickaction);
                if (!this.tryItemClickBehaviourOverride(player, clickaction, slot7, itemStack9, itemStack10)) {
                    if (itemStack9.isEmpty()) {
                        if (!itemStack10.isEmpty()) {
                            int i3 = clickaction == ClickAction.PRIMARY ? itemStack10.getCount() : 1;
                            this.setCarried(this.safeInsert(slot7, itemStack10, i3));
                        }
                    } else if (slot7.mayPickup(player)) {
                        if (itemStack10.isEmpty()) {
                            int j3 = clickaction == ClickAction.PRIMARY ? Math.min(64, itemStack9.getCount()) : (Math.min(64, itemStack9.getCount()) + 1) / 2;
                            Optional<ItemStack> optional1 = slot7.tryRemove(j3, Integer.MAX_VALUE, player);
                            optional1.ifPresent(stack -> {
                                this.setCarried(stack);
                                slot7.onTake(player, stack);
                            });
                        } else if (slot7.mayPlace(itemStack10)) {
                            if (ItemStack.isSameItemSameComponents(itemStack9, itemStack10)) {
                                int k3 = clickaction == ClickAction.PRIMARY ? itemStack10.getCount() : 1;
                                this.setCarried(this.safeInsert(slot7, itemStack10, k3));
                            } else if (itemStack10.getCount() <= this.getSlotMaxStack(slot7, itemStack10)) {
                                this.setCarried(itemStack9);
                                slot7.setByPlayer(itemStack10);
                            }
                        } else if (ItemStack.isSameItemSameComponents(itemStack9, itemStack10)) {
                            Optional<ItemStack> optional = slot7.tryRemove(itemStack9.getCount(), itemStack10.getMaxStackSize() - itemStack10.getCount(), player);
                            optional.ifPresent(stack -> {
                                itemStack10.grow(stack.getCount());
                                slot7.onTake(player, stack);
                            });
                        }
                    }
                }

                slot7.setChanged();
            }
        } else if (clickType == ClickType.SWAP && (button >= 0 && button < 9 || button == 40)) {
            ItemStack itemStack2 = inventory.getItem(button);
            Slot slot5 = this.slots.get(slotId);
            ItemStack itemStack7 = slot5.getItem();
            if (!itemStack2.isEmpty() || !itemStack7.isEmpty()) {
                if (itemStack2.isEmpty()) {
                    if (slot5.mayPickup(player)) {
                        ItemStack itemStack3 = itemStack7.split(Math.min(itemStack7.getCount(), Math.min(64, itemStack7.getMaxStackSize())));
                        inventory.setItem(button, itemStack3);
                        // slot5.onSwapCraft(itemStack7.getCount());
                        slot5.onTake(player, itemStack7);
                    }
                } else if (itemStack7.isEmpty()) {
                    if (slot5.mayPlace(itemStack2)) {
                        int j2 = this.getSlotMaxStack(slot5, itemStack2);
                        if (itemStack2.getCount() > j2) {
                            slot5.setByPlayer(itemStack2.split(j2));
                        } else {
                            inventory.setItem(button, ItemStack.EMPTY);
                            slot5.setByPlayer(itemStack2);
                        }
                    }
                } else if (slot5.mayPickup(player) && slot5.mayPlace(itemStack2) && itemStack7.getCount() <= Math.min(itemStack7.getMaxStackSize(), Math.min(64, slot5.getMaxStackSize()))) {
                    inventory.setItem(button, itemStack7);
                    slot5.setByPlayer(itemStack2);
                    slot5.onTake(player, itemStack7);
                }
            }
        } else if (clickType == ClickType.CLONE && player.hasInfiniteMaterials() && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot4 = this.slots.get(slotId);
            if (slot4.hasItem()) {
                ItemStack itemStack5 = slot4.getItem();
                ItemStack itemStack6 = itemStack5.copyWithCount(Math.min(64, itemStack5.getMaxStackSize()));
                this.setCarried(itemStack6);
            }
        } else if (clickType == ClickType.THROW && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot3 = this.slots.get(slotId);
            int j1 = button == 0 ? 1 : Math.min(64, slot3.getItem().getCount());
            ItemStack itemStack7 = slot3.safeTake(j1, Integer.MAX_VALUE, player);
            player.drop(itemStack7, true);
        } else if (clickType == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot2 = this.slots.get(slotId);
            ItemStack itemStack4 = this.getCarried();
            if (!itemStack4.isEmpty() && (!slot2.hasItem() || !slot2.mayPickup(player))) {
                int l1 = button == 0 ? 0 : this.slots.size() - 1;
                int i2 = button == 0 ? 1 : -1;

                for (int l2 = 0; l2 < 2; l2++) {
                    for (int l3 = l1; l3 >= 0 && l3 < this.slots.size() && itemStack4.getCount() < itemStack4.getMaxStackSize(); l3 += i2) {
                        Slot slot8 = this.slots.get(l3);
                        if (slot8.hasItem()
                                && canItemQuickReplace(this.tile.chest, slot8, itemStack4, true, this.getSlotMaxStack(slot8))
                                && slot8.mayPickup(player)
                                && this.canTakeItemForPickAll(itemStack4, slot8)) {
                            ItemStack itemStack11 = slot8.getItem();
                            if (l2 != 0 || itemStack11.getCount() != itemStack11.getMaxStackSize()) {
                                ItemStack itemStack12 = slot8.safeTake(itemStack11.getCount(), itemStack4.getMaxStackSize() - itemStack4.getCount(), player);
                                itemStack4.grow(itemStack12.getCount());
                            }
                        }
                    }
                }
            }
        } else {
            super.clicked(slotId, button, clickType, player);
        }
    }

    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, boolean inventory) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (!stack.isEmpty()) {
            while (reverseDirection ? i >= startIndex : i < endIndex) {
                Slot slot = this.slots.get(i);
                ItemStack itemStack = slot.getItem();
                if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    int maxSize = inventory ? Math.min(Math.min(64, slot.getMaxStackSize()), stack.getMaxStackSize()) : this.getSlotMaxStack(slot);
                    if (j <= maxSize && j > 0) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemStack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemStack.getCount());
                        itemStack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (reverseDirection ? i >= startIndex : i < endIndex) {
                Slot slot1 = this.slots.get(i);
                ItemStack itemStack1 = slot1.getItem();
                if (itemStack1.isEmpty() && slot1.mayPlace(stack)) {
                    slot1.setByPlayer(stack.split(Math.min(stack.getCount(), Math.min(64, slot1.getMaxStackSize()))));
                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        return flag;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();

            if (index < 243) {
                if (!this.moveItemStackTo(slotStack, 243, 279, true, true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 243 && index < 279) {

                if (!this.moveItemStackTo(slotStack, 0, 243, false, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == resultStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return resultStack;
    }


    @Override
    public void removed(Player player) {
        super.removed(player);
        this.tile.chest.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = this.tile.getBlockPos();
        return this.tile.getLevel().getBlockState(pos).is(ModBlocks.infinity_chest.get()) && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }}