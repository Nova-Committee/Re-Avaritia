package committee.nova.mods.avaritia.api.common.item;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * InvWrapper
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/23 下午9:16
 */
public class InvWrapper extends ItemStackHandler {
    private final Container inv;

    public InvWrapper(Container inv) {
        this.inv = inv;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            InvWrapper that = (InvWrapper)o;
            return this.getInv().equals(that.getInv());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getInv().hashCode();
    }

    @Override
    public int getSlotCount() {
        return this.getInv().getContainerSize();
    }

    public @NotNull ItemStack getStackInSlot(int slot) {
        return this.getInv().getItem(slot);
    }

    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack stackInSlot = this.getInv().getItem(slot);
            int m;
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), this.getSlotLimit(slot))) {
                    return stack;
                } else if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
                    return stack;
                } else if (!this.getInv().canPlaceItem(slot, stack)) {
                    return stack;
                } else {
                    m = Math.min(stack.getMaxStackSize(), this.getSlotLimit(slot)) - stackInSlot.getCount();
                    ItemStack copy;
                    if (stack.getCount() <= m) {
                        if (!simulate) {
                            copy = stack.copy();
                            copy.grow(stackInSlot.getCount());
                            this.getInv().setItem(slot, copy);
                            this.getInv().setChanged();
                        }

                        return ItemStack.EMPTY;
                    } else {
                        stack = stack.copy();
                        if (!simulate) {
                            copy = stack.split(m);
                            copy.grow(stackInSlot.getCount());
                            this.getInv().setItem(slot, copy);
                            this.getInv().setChanged();
                            return stack;
                        } else {
                            stack.shrink(m);
                            return stack;
                        }
                    }
                }
            } else if (!this.getInv().canPlaceItem(slot, stack)) {
                return stack;
            } else {
                m = Math.min(stack.getMaxStackSize(), this.getSlotLimit(slot));
                if (m < stack.getCount()) {
                    stack = stack.copy();
                    if (!simulate) {
                        this.getInv().setItem(slot, stack.split(m));
                        this.getInv().setChanged();
                        return stack;
                    } else {
                        stack.shrink(m);
                        return stack;
                    }
                } else {
                    if (!simulate) {
                        this.getInv().setItem(slot, stack);
                        this.getInv().setChanged();
                    }

                    return ItemStack.EMPTY;
                }
            }
        }
    }

    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        } else {
            ItemStack stackInSlot = this.getInv().getItem(slot);
            if (stackInSlot.isEmpty()) {
                return ItemStack.EMPTY;
            } else if (simulate) {
                if (stackInSlot.getCount() < amount) {
                    return stackInSlot.copy();
                } else {
                    ItemStack copy = stackInSlot.copy();
                    copy.setCount(amount);
                    return copy;
                }
            } else {
                int m = Math.min(stackInSlot.getCount(), amount);
                ItemStack decrStackSize = this.getInv().removeItem(slot, m);
                this.getInv().setChanged();
                return decrStackSize;
            }
        }
    }

    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        this.getInv().setItem(slot, stack);
    }

    public int getSlotLimit(int slot) {
        return this.getInv().getMaxStackSize();
    }

    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return this.getInv().canPlaceItem(slot, stack);
    }

    public Container getInv() {
        return this.inv;
    }
}
