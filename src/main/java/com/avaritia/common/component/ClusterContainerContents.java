package com.avaritia.common.component;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public final class ClusterContainerContents {
    private static final int NO_SLOT = -1;
    private static final int MAX_SIZE = Integer.MAX_VALUE;
    public static final ClusterContainerContents EMPTY = new ClusterContainerContents(NonNullList.create());
    public static final Codec<ClusterContainerContents> CODEC = ClusterContainerContents.Slot.CODEC
            .sizeLimitedListOf(MAX_SIZE).xmap(ClusterContainerContents::fromSlots, ClusterContainerContents::asSlots);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClusterContainerContents> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC
            .apply(ByteBufCodecs.list(MAX_SIZE)).map(ClusterContainerContents::new, p_331691_ -> p_331691_.items);
    private final NonNullList<ItemStack> items;
    private final int hashCode;

    @SuppressWarnings("deprecation")
    private ClusterContainerContents(NonNullList<ItemStack> items) {
        if (items.size() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "Got " + items.size() + " items, but maximum is " + String.valueOf(MAX_SIZE));
        } else {
            this.items = items;
            this.hashCode = ItemStack.hashStackList(items);
        }
    }

    private ClusterContainerContents(int size) {
        this(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    private ClusterContainerContents(List<ItemStack> items) {
        this(items.size());

        for (int i = 0; i < items.size(); i++) {
            this.items.set(i, items.get(i));
        }
    }

    private static ClusterContainerContents fromSlots(List<ClusterContainerContents.Slot> slots) {
        OptionalInt optionalint = slots.stream().mapToInt(ClusterContainerContents.Slot::index).max();
        if (optionalint.isEmpty()) {
            return EMPTY;
        } else {
            ClusterContainerContents itemcontainercontents = new ClusterContainerContents(optionalint.getAsInt() + 1);

            for (ClusterContainerContents.Slot itemcontainercontents$slot : slots) {
                itemcontainercontents.items.set(itemcontainercontents$slot.index(), itemcontainercontents$slot.item());
            }
            return itemcontainercontents;
        }
    }

    public static ClusterContainerContents fromItems(List<ItemStack> items) {
        int i = findLastNonEmptySlot(items);
        if (i == -1) {
            return EMPTY;
        } else {
            ClusterContainerContents itemcontainercontents = new ClusterContainerContents(i + 1);

            for (int j = 0; j <= i; j++) {
                itemcontainercontents.items.set(j, items.get(j).copy());
            }
            return itemcontainercontents;
        }
    }

    private static int findLastNonEmptySlot(List<ItemStack> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                return i;
            }
        }
        return NO_SLOT;
    }

    private List<ClusterContainerContents.Slot> asSlots() {
        List<ClusterContainerContents.Slot> list = new ArrayList<>();

        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemStack = this.items.get(i);
            if (!itemStack.isEmpty()) {
                list.add(new ClusterContainerContents.Slot(i, itemStack));
            }
        }
        return list;
    }

    public void copyInto(NonNullList<ItemStack> list) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack itemStack = i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
            list.set(i, itemStack.copy());
        }
    }

    public ItemStack copyOne() {
        return this.items.isEmpty() ? ItemStack.EMPTY : this.items.get(0).copy();
    }

    public Stream<ItemStack> stream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Stream<ItemStack> nonEmptyStream() {
        return this.items.stream().filter(itemStack -> !itemStack.isEmpty()).map(ItemStack::copy);
    }

    public Iterable<ItemStack> nonEmptyItems() {
        return Iterables.filter(this.items, itemStack -> !itemStack.isEmpty());
    }

    public Iterable<ItemStack> nonEmptyItemsCopy() {
        return Iterables.transform(this.nonEmptyItems(), ItemStack::copy);
    }

    public NonNullList<ItemStack> getItems(){
        return this.items;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            if (pOther instanceof ClusterContainerContents itemcontainercontents
                    && ItemStack.listMatches(this.items, itemcontainercontents.items)) {
                return true;
            }
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    public int getSlots() {
        return this.items.size();
    }

    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.items.get(slot).copy();
    }

    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            throw new UnsupportedOperationException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
        }
    }

    static record Slot(int index, ItemStack item) {
        public static final Codec<ClusterContainerContents.Slot> CODEC = RecordCodecBuilder
                .create(p_331695_ -> p_331695_
                        .group(Codec.intRange(0, MAX_SIZE - 1).fieldOf("slot")
                                        .forGetter(ClusterContainerContents.Slot::index),
                                ItemStack.CODEC.fieldOf("item").forGetter(ClusterContainerContents.Slot::item))
                        .apply(p_331695_, ClusterContainerContents.Slot::new));
    }
}
