package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.container.slot.InfinitySlot;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Menu for the local 9x27 infinity chest inventory.
 */
public class InfinityChestMenu extends AbstractContainerMenu {
    public static final int CHEST_ROWS = 9;
    public static final int CHEST_COLUMNS = 27;
    public static final int CHEST_SLOT_COUNT = CHEST_ROWS * CHEST_COLUMNS;
    public static final int PLAYER_SLOT_COUNT = 36;
    public static final int PLAYER_SLOT_START = CHEST_SLOT_COUNT;
    public static final int PLAYER_SLOT_END = PLAYER_SLOT_START + PLAYER_SLOT_COUNT;

    private final InfinityChestTile tile;
    private final Container container;

    /**
     * Kept only so the legacy infinity chest filter packet can still compile.
     */
    public String filter = "";

    public InfinityChestMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getTile(inventory, buffer.readBlockPos()));
    }

    public InfinityChestMenu(int id, Inventory inventory, InfinityChestTile tile) {
        super(ModMenus.infinity_chest.get(), id);
        if (tile == null) {
            throw new IllegalArgumentException("Infinity chest menu requires a valid tile");
        }
        this.tile = tile;
        this.container = tile.chest;
        this.container.startOpen(inventory.player);

        for (int row = 0; row < CHEST_ROWS; row++) {
            for (int column = 0; column < CHEST_COLUMNS; column++) {
                this.addSlot(new InfinitySlot(this.container, column + row * CHEST_COLUMNS, 8 + column * 18, 17 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 170 + column * 18, 193 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 170 + column * 18, 251));
        }
    }

    public InfinityChestTile getTile() {
        return this.tile;
    }

    public int getSlotMaxStack(Slot slot, ItemStack stack) {
        return slot instanceof InfinitySlot ? slot.getMaxStackSize() : slot.getMaxStackSize(stack);
    }

    public int getSlotMaxStack(Slot slot) {
        return this.getSlotMaxStack(slot, slot.getItem());
    }

    public void action(int actionId, String id) {
        Const.LOGGER.warn("Ignored legacy infinity chest action packet: actionId={}, id={}", actionId, id);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            if (index < CHEST_SLOT_COUNT) {
                if (!this.moveItemStackTo(slotStack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_SLOT_END) {
                if (!this.moveItemStackTo(slotStack, 0, CHEST_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
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
    protected boolean moveItemStackTo(@NotNull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean moved = false;
        int index = reverseDirection ? endIndex - 1 : startIndex;

        if (stack.isStackable()) {
            while (!stack.isEmpty() && (reverseDirection ? index >= startIndex : index < endIndex)) {
                Slot slot = this.slots.get(index);
                ItemStack slotStack = slot.getItem();
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(stack, slotStack)) {
                    int maxSize = this.getSlotMaxStack(slot, stack);
                    int newCount = slotStack.getCount() + stack.getCount();
                    if (newCount <= maxSize) {
                        stack.setCount(0);
                        slotStack.setCount(newCount);
                        slot.setChanged();
                        moved = true;
                    } else if (slotStack.getCount() < maxSize) {
                        stack.shrink(maxSize - slotStack.getCount());
                        slotStack.setCount(maxSize);
                        slot.setChanged();
                        moved = true;
                    }
                }
                index += reverseDirection ? -1 : 1;
            }
        }

        index = reverseDirection ? endIndex - 1 : startIndex;
        while (!stack.isEmpty() && (reverseDirection ? index >= startIndex : index < endIndex)) {
            Slot slot = this.slots.get(index);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                int moveCount = Math.min(stack.getCount(), this.getSlotMaxStack(slot, stack));
                slot.setByPlayer(stack.split(moveCount));
                slot.setChanged();
                moved = true;
                break;
            }
            index += reverseDirection ? -1 : 1;
        }

        return moved;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        BlockPos pos = this.tile.getBlockPos();
        return this.tile.getLevel() != null
                && this.tile.getLevel().getBlockState(pos).is(ModBlocks.infinity_chest.get())
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    private static InfinityChestTile getTile(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof InfinityChestTile tile) {
            return tile;
        }
        throw new IllegalStateException("Missing infinity chest tile at " + pos);
    }
}
