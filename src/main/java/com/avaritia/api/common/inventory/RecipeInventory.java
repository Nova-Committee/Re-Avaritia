package com.avaritia.api.common.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/14 19:14
 * @Description:
 */
public class RecipeInventory implements Container {
    private final ResourceHandler<ItemResource> inventory;
    private final IndexModifier<ItemResource> modifier;
    private final int start;
    private final int size;

    public RecipeInventory(ResourceHandler<ItemResource> inventory, IndexModifier<ItemResource> modifier) {
        this(inventory, modifier, 0, inventory.size());
    }

    public RecipeInventory(ResourceHandler<ItemResource> inventory, IndexModifier<ItemResource> modifier, int start, int size) {
        this.inventory = inventory;
        this.modifier = modifier;
        this.start = start;
        this.size = size;
    }

    public int getContainerSize() {
        return this.size;
    }

    public @NotNull ItemStack getItem(int slot) {
        return ItemUtil.getStack(this.inventory, slot + this.start);
    }

    public @NotNull ItemStack removeItem(int slot, int count) {
        int index = slot + this.start;
        ItemResource resource = this.inventory.getResource(index);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }

        try (var tx = Transaction.openRoot()) {
            int extracted = this.inventory.extract(index, resource, count, tx);
            tx.commit();
            return extracted == 0 ? ItemStack.EMPTY : resource.toStack(extracted);
        }
    }

    public void setItem(int slot, @NotNull ItemStack stack) {
        this.modifier.set(slot + this.start, ItemResource.of(stack), stack.getCount());
    }

    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.getItem(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.setItem(slot, ItemStack.EMPTY);
            return stack;
        }
    }

    public boolean isEmpty() {
        for (int i = this.start; i < this.start + this.size; ++i) {
            if (this.inventory.getAmountAsLong(i) > 0) {
                return false;
            }
        }

        return true;
    }

    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return !stack.isEmpty() && this.inventory.isValid(slot + this.start, ItemResource.of(stack));
    }

    public void clearContent() {
        for (int i = this.start; i < this.start + this.size; ++i) {
            this.modifier.set(i, ItemResource.EMPTY, 0);
        }

    }

    public int getMaxStackSize() {
        return 64;
    }

    public void setChanged() {
    }

    public boolean stillValid(@NotNull Player player) {
        return false;
    }

    public void startOpen(@NotNull Player player) {
    }

    public void stopOpen(@NotNull Player player) {
    }
}
