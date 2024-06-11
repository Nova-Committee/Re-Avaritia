package committee.nova.mods.avaritia.api.common.container;

import committee.nova.mods.avaritia.util.ContainerUtils;
import committee.nova.mods.avaritia.util.java.ArrayUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

/**
 * SimpleContainer
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/11 下午11:46
 */
public class SimpleContainer implements Container {

    public ItemStack[] items;
    public int limit;
    public String name;

    public SimpleContainer(ItemStack[] items, int limit, String name) {
        this.items = items;
        ArrayUtils.fillArray(items, ItemStack.EMPTY, (Objects::isNull));
        this.limit = limit;
        this.name = name;
    }

    public SimpleContainer(ItemStack[] items, String name) {
        this(items, 64, name);
    }

    public SimpleContainer(ItemStack[] items, int limit) {
        this(items, limit, "inv");
    }

    public SimpleContainer(ItemStack[] items) {
        this(items, 64, "inv");
    }

    public SimpleContainer(int size, int limit, String name) {
        this(new ItemStack[size], limit, name);
    }

    public SimpleContainer(int size, int limit) {
        this(size, limit, "inv");
    }

    public SimpleContainer(int size, String name) {
        this(size, 64, name);
    }

    public SimpleContainer(int size) {
        this(size, 64, "inv");
    }

    @Override
    public int getContainerSize() {
        return items.length;
    }

    @Override
    public boolean isEmpty() {
        return ArrayUtils.count(items, (stack -> !stack.isEmpty())) <= 0;
    }

    @Override
    @Nonnull
    public ItemStack getItem(int slot) {
        return items[slot];
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int slot, int amount) {
        return ContainerUtils.decrStackSize(this, slot, amount);
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerUtils.removeStackFromSlot(this, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items[slot] = stack;
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return limit;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemstack) {
        return true;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
    }

    @Override
    public void clearContent() {
        Arrays.fill(items, ItemStack.EMPTY);
    }
}
