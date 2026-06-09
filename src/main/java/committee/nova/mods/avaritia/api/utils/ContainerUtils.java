package committee.nova.mods.avaritia.api.utils;

import committee.nova.mods.avaritia.api.common.container.FaceContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nonnull;

/**
 * ContainerUtils
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/11 下午11:47
 */
public class ContainerUtils {
    /**
     * Static default implementation for IInventory method
     */
    @Nonnull
    public static ItemStack decrStackSize(Container inv, int slot, int size) {
        ItemStack item = inv.getItem(slot);

        if (!item.isEmpty()) {
            if (item.getCount() <= size) {
                inv.setItem(slot, ItemStack.EMPTY);
                inv.setChanged();
                return item;
            }
            ItemStack itemstack1 = item.split(size);
            if (item.getCount() == 0) {
                inv.setItem(slot, ItemStack.EMPTY);
            } else {
                inv.setItem(slot, item);
            }

            inv.setChanged();
            return itemstack1;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Static default implementation for IInventory method
     */
    public static ItemStack removeStackFromSlot(Container inv, int slot) {
        ItemStack stack = inv.getItem(slot);
        inv.setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    /**
     * @return The quantity of items from additions that can be added to base
     */
    public static int incrStackSize(@Nonnull ItemStack base, @Nonnull ItemStack addition) {
        if (canStack(base, addition)) {
            return incrStackSize(base, addition.getCount());
        }

        return 0;
    }

    /**
     * @return The quantity of items from additions that can be added to base
     */
    public static int incrStackSize(@Nonnull ItemStack base, int addition) {
        int totalSize = base.getCount() + addition;

        if (totalSize <= base.getMaxStackSize()) {
            return addition;
        } else if (base.getCount() < base.getMaxStackSize()) {
            return base.getMaxStackSize() - base.getCount();
        }

        return 0;
    }

    public static boolean areStacksIdentical(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return stack1 == stack2;
        }

        return stack1.getItem() == stack2.getItem() && stack1.getDamageValue() == stack2.getDamageValue() && stack1.getCount() == stack2.getCount() && ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    public static boolean canStack(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        return stack1.isEmpty() || stack2.isEmpty() || (stack1.getItem() == stack2.getItem() && (stack2.getDamageValue() == stack1.getDamageValue()) && ItemStack.isSameItemSameComponents(stack2, stack1)) && stack1.isStackable();
    }

    /**
     * Consumes one item from slot in inv with support for containers.
     */
    public static void consumeItem(Container inv, int slot) {
        ItemStack stack = inv.getItem(slot);
        var remainder = stack.getCraftingRemainder();
        if (remainder != null) {
            inv.setItem(slot, remainder.create());
        } else {
            inv.removeItem(slot, 1);
        }
    }

    /**
     * Gets the size of the stack in a slot. Returns 0 on empty stacks
     */
    public static int stackSize(Container inv, int slot) {
        ItemStack stack = inv.getItem(slot);
        return stack.isEmpty() ? 0 : stack.getCount();
    }

    /**
     * Drops all items from inv using removeStackFromSlot
     */
    public static void dropOnClose(Player player, Container inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
            }
        }
    }

    public static boolean canInsertStack(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        try (var tx = Transaction.openRoot()) {
            return handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx) > 0;
        }
    }

    public static boolean canExtractStack(ResourceHandler<ItemResource> handler, int slot) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty()) {
            return false;
        }
        try (var tx = Transaction.openRoot()) {
            return handler.extract(slot, resource, resource.getMaxStackSize(), tx) > 0;
        }
    }

    public static ItemStack insertItem(ResourceHandler<ItemResource> handler, ItemStack insert, boolean simulate) {
        if (insert.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.openRoot()) {
            int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(insert), insert.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int remaining = insert.getCount() - inserted;
            return remaining == 0 ? ItemStack.EMPTY : insert.copyWithCount(remaining);
        }
    }

    public static int fitStackInSlot(FaceContainer inv, int slot, ItemStack stack) {
        ItemStack base = inv.inv.getItem(slot);
        if (!canStack(base, stack) || !inv.canInsertItem(slot, stack)) {
            return 0;
        }

        int fit = !base.isEmpty() ? incrStackSize(base, inv.inv.getMaxStackSize() - base.getCount()) : inv.inv.getMaxStackSize();
        return Math.min(fit, stack.getCount());
    }

    public static int fitStackInSlot(Container inv, int slot, @Nonnull ItemStack stack) {
        return fitStackInSlot(new FaceContainer(inv), slot, stack);
    }

    /**
     * @param simulate If set to true, no items will actually be inserted
     * @return The number of items unable to be inserted
     */
    public static int insertItem(FaceContainer inv, @Nonnull ItemStack stack, boolean simulate) {
        stack = stack.copy();
        for (int pass = 0; pass < 2; pass++) {
            for (int slot : inv.slots) {
                ItemStack base = inv.inv.getItem(slot);
                if ((pass == 0) == (base.isEmpty())) {
                    continue;
                }
                int fit = fitStackInSlot(inv, slot, stack);
                if (fit == 0) {
                    continue;
                }

                if (!base.isEmpty()) {
                    stack.shrink(fit);
                    if (!simulate) {
                        base.grow(fit);
                        inv.inv.setItem(slot, base);
                    }
                } else {
                    if (!simulate) {
                        inv.inv.setItem(slot, ItemUtils.copyStack(stack, fit));
                    }
                    stack.shrink(fit);
                }
                if (stack.getCount() == 0) {
                    return 0;
                }
            }
        }
        return stack.getCount();
    }

    public static int insertItem(Container inv, @Nonnull ItemStack stack, boolean simulate) {
        return insertItem(new FaceContainer(inv), stack, simulate);
    }

    /**
     * Counts the matching stacks.
     * Checks for insertion or extraction.
     *
     * @param handler The inventory.
     * @param filter  What we are checking for.
     * @param insert  If we are checking for insertion or extraction.
     * @return The total number of items of the specified filter type.
     */
    public static int countMatchingStacks(ResourceHandler<ItemResource> handler, ItemStack filter, boolean insert) {

        int c = 0;
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemStack stack = ItemUtil.getStack(handler, slot);
            if (!stack.isEmpty() && ItemUtils.areStacksSameType(filter, stack) && (insert ? canInsertStack(handler, slot, stack) : canExtractStack(handler, slot))) {
                c += stack.getCount();
            }
        }
        return c;
    }

    public static int getInsertableQuantity(ResourceHandler<ItemResource> handler, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemResource resource = ItemResource.of(stack);
        int quantity = 0;
        for (int slot = 0; slot < handler.size(); slot++) {
            try (var tx = Transaction.openRoot()) {
                quantity += handler.insert(slot, resource, Integer.MAX_VALUE, tx);
            }
        }
        return quantity;
    }
}
