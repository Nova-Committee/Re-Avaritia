package com.avaritia.common.menu;

import java.util.Optional;

import javax.annotation.Nullable;

import com.avaritia.common.tile.InfinityChestTile;
import com.avaritia.common.container.slot.InfinitySlot;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

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
    public void clicked(int slotId, int button, @NonNull ContainerInput containerInput, Player player) {
        if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        }
        ClickAction clickAction = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;

        if (slotId == -999) {
            if (!this.getCarried().isEmpty()) {
                if (clickAction == ClickAction.PRIMARY) {
                    player.drop(this.getCarried(), true);
                    this.setCarried(ItemStack.EMPTY);
                } else {
                    player.drop(this.getCarried().split(1), true);
                }
            }
        } else if (slotId < 0) {
            super.clicked(slotId, button, containerInput, player);
        } else {
            Slot slot = this.slots.get(slotId);
            ItemStack slotStack = slot.getItem();
            ItemStack carriedStack = this.getCarried();
            player.updateTutorialInventoryAction(carriedStack, slot.getItem(), clickAction);
            if (!this.tryItemClickBehaviourOverride(player, clickAction, slot, slotStack, carriedStack)) {
                if (slotStack.isEmpty()) {
                    if (!carriedStack.isEmpty()) {
                        int i = clickAction == ClickAction.PRIMARY ? carriedStack.getCount() : 1;
                        this.setCarried(this.safeInsert(slot, carriedStack, i));
                    }
                } else if (slot.mayPickup(player)) {
                    if (carriedStack.isEmpty()) {
                        int j = clickAction == ClickAction.PRIMARY ? Math.min(64, slotStack.getCount()) : (Math.min(64, slotStack.getCount()) + 1) / 2;
                        Optional<ItemStack> optional = slot.tryRemove(j, Integer.MAX_VALUE, player);
                        optional.ifPresent(stack -> {
                            this.setCarried(stack);
                            slot.onTake(player, stack);
                        });
                    } else if (slot.mayPlace(carriedStack)) {
                        if (ItemStack.isSameItemSameComponents(slotStack, carriedStack)) {
                            int k = clickAction == ClickAction.PRIMARY ? carriedStack.getCount() : 1;
                            this.setCarried(this.safeInsert(slot, carriedStack, k));
                        } else if (carriedStack.getCount() <= this.getSlotMaxStack(slot, carriedStack)) {
                            this.setCarried(slotStack);
                            slot.setByPlayer(carriedStack);
                        }
                    } else if (ItemStack.isSameItemSameComponents(slotStack, carriedStack)) {
                        Optional<ItemStack> optional = slot.tryRemove(slotStack.getCount(), carriedStack.getMaxStackSize() - carriedStack.getCount(), player);
                        optional.ifPresent(stack -> {
                            carriedStack.grow(stack.getCount());
                            slot.onTake(player, stack);
                        });
                    }
                }
            }
            slot.setChanged();
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