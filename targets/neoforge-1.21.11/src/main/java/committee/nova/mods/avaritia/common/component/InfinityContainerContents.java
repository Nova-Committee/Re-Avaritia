package committee.nova.mods.avaritia.common.component;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 23:44
 * @Description:
 */
public class InfinityContainerContents {
    private static final int MAX_SIZE = Integer.MAX_VALUE;
    public static final InfinityContainerContents EMPTY = new InfinityContainerContents(NonNullList.create());
    public static final Codec<InfinityContainerContents> CODEC = InfinityContainerContents.Slot.CODEC
            .sizeLimitedListOf(MAX_SIZE)
            .xmap(InfinityContainerContents::fromSlots, InfinityContainerContents::asSlots);
    public static final StreamCodec<RegistryFriendlyByteBuf, InfinityContainerContents> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC
            .apply(ByteBufCodecs.list(MAX_SIZE))
            .map(InfinityContainerContents::new, p_331691_ -> p_331691_.items);
    private final NonNullList<ItemStack> items;
    private final int hashCode;

    private InfinityContainerContents(NonNullList<ItemStack> items) {
        if (items.size() > MAX_SIZE) {
            throw new IllegalArgumentException("Got " + items.size() + " items, but maximum is " + MAX_SIZE);
        } else {
            this.items = items;
            this.hashCode = ItemStack.hashStackList(items);
        }
    }

    private InfinityContainerContents(int size) {
        this(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    private InfinityContainerContents(List<ItemStack> items) {
        this(items.size());

        for (int i = 0; i < items.size(); i++) {
            this.items.set(i, items.get(i));
        }
    }

    private static InfinityContainerContents fromSlots(List<InfinityContainerContents.Slot> slots) {
        OptionalInt optionalint = slots.stream().mapToInt(InfinityContainerContents.Slot::index).max();
        if (optionalint.isEmpty()) {
            return EMPTY;
        } else {
            InfinityContainerContents InfinityContainerContents = new InfinityContainerContents(optionalint.getAsInt() + 1);

            for (InfinityContainerContents.Slot MatterClusterContents$slot : slots) {
                InfinityContainerContents.items.set(MatterClusterContents$slot.index(), MatterClusterContents$slot.item());
            }

            return InfinityContainerContents;
        }
    }

    public static InfinityContainerContents fromItems(List<ItemStack> items) {
        int i = findLastNonEmptySlot(items);
        if (i == -1) {
            return EMPTY;
        } else {
            InfinityContainerContents InfinityContainerContents = new InfinityContainerContents(i + 1);

            for (int j = 0; j <= i; j++) {
                InfinityContainerContents.items.set(j, items.get(j).copy());
            }

            return InfinityContainerContents;
        }
    }

    private static int findLastNonEmptySlot(List<ItemStack> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private List<InfinityContainerContents.Slot> asSlots() {
        List<InfinityContainerContents.Slot> list = new ArrayList<>();

        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemstack = this.items.get(i);
            if (!itemstack.isEmpty()) {
                list.add(new InfinityContainerContents.Slot(i, itemstack));
            }
        }

        return list;
    }

    public void copyInto(NonNullList<ItemStack> list) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack itemstack = i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
            list.set(i, itemstack.copy());
        }
    }

    public ItemStack copyOne() {
        return this.items.isEmpty() ? ItemStack.EMPTY : this.items.get(0).copy();
    }

    public Stream<ItemStack> stream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Stream<ItemStack> nonEmptyStream() {
        return this.items.stream().filter(p_331322_ -> !p_331322_.isEmpty()).map(ItemStack::copy);
    }

    public Iterable<ItemStack> nonEmptyItems() {
        return Iterables.filter(this.items, p_331420_ -> !p_331420_.isEmpty());
    }

    public Iterable<ItemStack> nonEmptyItemsCopy() {
        return Iterables.transform(this.nonEmptyItems(), ItemStack::copy);
    }

    public NonNullList<ItemStack> getItems(){
        return this.items;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            if (other instanceof InfinityContainerContents InfinityContainerContents &&
                    ItemStack.listMatches(this.items, InfinityContainerContents.items)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * Neo:
     * {@return the number of slots in this container}
     */
    public int getSlots() {
        return this.items.size();
    }

    /**
     * Neo: Gets a copy of the stack at a particular slot.
     *
     * @param slot The slot to check. Must be within [0, {@link #getSlots()}]
     * @return A copy of the stack in that slot
     * @throws UnsupportedOperationException if the provided slot index is out-of-bounds.
     */
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.items.get(slot).copy();
    }

    /**
     * Neo: Throws {@link UnsupportedOperationException} if the provided slot index is invalid.
     */
    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            throw new UnsupportedOperationException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
        }
    }

    static record Slot(int index, ItemStack item) {
        public static final Codec<InfinityContainerContents.Slot> CODEC = RecordCodecBuilder.create(
                p_331695_ -> p_331695_.group(
                                Codec.intRange(0, 255).fieldOf("slot").forGetter(InfinityContainerContents.Slot::index),
                                ItemStack.CODEC.fieldOf("item").forGetter(InfinityContainerContents.Slot::item)
                        )
                        .apply(p_331695_, InfinityContainerContents.Slot::new)
        );
    }
}
