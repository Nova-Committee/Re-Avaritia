package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.common.container.slot.InfinitySlot;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InfinityChestMenu extends AbstractContainerMenu {
    private static final int CHEST_SLOT_COUNT = 9 * 27;
    private static final int PLAYER_SLOT_COUNT = 36;
    private static final int PLAYER_SLOT_START = CHEST_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + PLAYER_SLOT_COUNT;
    private static final int VANILLA_STACK_LIMIT = 64;

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

    public int getSlotMaxStack(Slot slot, ItemStack stack) {
        return slot instanceof InfinitySlot ? slot.getMaxStackSize() : slot.getMaxStackSize(stack);
    }

    public int getSlotMaxStack(Slot slot) {
        ItemStack itemStack = slot.getItem();
        return this.getSlotMaxStack(slot, itemStack);
    }

    static int availableTransferSpace(int currentCount, int maxStackSize) {
        return currentCount >= maxStackSize ? 0 : maxStackSize - currentCount;
    }

    static int transferAmount(int sourceCount, int currentCount, int maxStackSize) {
        if (sourceCount <= 0 || maxStackSize <= 0) {
            return 0;
        }
        return Math.min(sourceCount, availableTransferSpace(currentCount, maxStackSize));
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
                    int maxSize = this.getTransferLimit(slot, stack, inventory);
                    int moved = transferAmount(stack.getCount(), itemStack.getCount(), maxSize);
                    if (moved > 0) {
                        stack.shrink(moved);
                        itemStack.grow(moved);
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
                    int moved = transferAmount(stack.getCount(), 0, this.getTransferLimit(slot1, stack, inventory));
                    if (moved > 0) {
                        // 无尽箱子槽位自身是 Integer.MAX_VALUE；普通玩家背包仍按原版堆叠上限放入。
                        slot1.setByPlayer(stack.split(moved));
                        slot1.setChanged();
                        flag = true;
                        break;
                    }
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

    private int getTransferLimit(Slot slot, ItemStack stack, boolean inventory) {
        if (inventory) {
            return Math.min(Math.min(VANILLA_STACK_LIMIT, slot.getMaxStackSize(stack)), stack.getMaxStackSize());
        }
        return this.getSlotMaxStack(slot, stack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            if (index < CHEST_SLOT_COUNT) {
                if (!this.moveItemStackTo(slotStack, PLAYER_SLOT_START, PLAYER_SLOT_END, true, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_SLOT_START && index < PLAYER_SLOT_END) {
                if (!this.moveItemStackTo(slotStack, 0, CHEST_SLOT_COUNT, false, false)) {
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
    }
}
