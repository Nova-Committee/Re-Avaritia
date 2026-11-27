package committee.nova.mods.avaritia.api.common.container;

import committee.nova.mods.avaritia.api.utils.ContainerUtils;
import committee.nova.mods.avaritia.api.utils.java.ArrayUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/6/11 下午11:46
 * @Description:
 */
public class NoMenuContainer implements Container {

    public ItemStack[] items;
    public int limit;
    public String name;

    public NoMenuContainer(ItemStack[] items, int limit, String name) {
        this.items = items;
        ArrayUtils.fillArray(items, ItemStack.EMPTY, (Objects::isNull));
        this.limit = limit;
        this.name = name;
    }

    public NoMenuContainer(ItemStack[] items, String name) {
        this(items, 64, name);
    }

    public NoMenuContainer(ItemStack[] items, int limit) {
        this(items, limit, "inv");
    }

    public NoMenuContainer(ItemStack[] items) {
        this(items, 64, "inv");
    }

    public NoMenuContainer(int size, int limit, String name) {
        this(new ItemStack[size], limit, name);
    }

    public NoMenuContainer(int size, int limit) {
        this(size, limit, "inv");
    }

    public NoMenuContainer(int size, String name) {
        this(size, 64, name);
    }

    public NoMenuContainer(int size) {
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
    public void setItem(int slot, @NotNull ItemStack stack) {
        items[slot] = stack;
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return limit;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int i, @NotNull ItemStack itemstack) {
        return true;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public void startOpen(@NotNull Player player) {
    }

    @Override
    public void stopOpen(@NotNull Player player) {
    }

    @Override
    public void clearContent() {
        Arrays.fill(items, ItemStack.EMPTY);
    }
}
