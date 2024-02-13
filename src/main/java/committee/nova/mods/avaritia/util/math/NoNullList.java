package committee.nova.mods.avaritia.util.math;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/2/13 18:24
 * @Description:
 */

public class NoNullList<E> extends AbstractList<E> {

    /**
     * Neo: utility method to construct a Codec for a NoNullList
     * @param entryCodec the codec to use for the elements
     * @param <E> the element type
     * @return a codec that encodes as a list, and decodes into NoNullList
     */
    public static <E> Codec<NoNullList<E>> codecOf(Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(NoNullList::copyOf, Function.identity());
    }

    /**
     * Neo: utility method to construct an immutable NoNullList from a given collection
     * @param entries the collection to make a copy of
     * @param <E> the type of the elements in the list
     * @return a new immutable NoNullList
     * @throws NullPointerException if entries is null, or if it contains any nulls
     */
    public static <E> NoNullList<E> copyOf(java.util.Collection<? extends E> entries) {
        return new NoNullList<>(List.copyOf(entries), null);
    }

    public final List<E> list;
    @Nullable
    private final E defaultValue;

    public static <E> NoNullList<E> create() {
        return new NoNullList<>(Lists.newArrayList(), (E)null);
    }

    public static <E> NoNullList<E> createWithCapacity(int pInitialCapacity) {
        return new NoNullList<>(Lists.newArrayListWithCapacity(pInitialCapacity), (E)null);
    }

    /**
     * Creates a new NoNullList with <i>fixed</i> size and default value. The list will be filled with the default value.
     */
    public static <E> NoNullList<E> withSize(int pSize, E pDefaultValue) {
        Validate.notNull(pDefaultValue);
        Object[] aobject = new Object[pSize];
        Arrays.fill(aobject, pDefaultValue);
        return new NoNullList<>(Arrays.asList((E[])aobject), pDefaultValue);
    }

    @SafeVarargs
    public static <E> NoNullList<E> of(E pDefaultValue, E... pElements) {
        return new NoNullList<>(Arrays.asList(pElements), pDefaultValue);
    }

    public NoNullList(List<E> pList, @Nullable E pDefaultValue) {
        this.list = pList;
        this.defaultValue = pDefaultValue;
    }

    @Nonnull
    @Override
    public E get(int pIndex) {
        return this.list.get(pIndex);
    }

    @Override
    public E set(int pIndex, E pValue) {
        Validate.notNull(pValue);
        return this.list.set(pIndex, pValue);
    }

    @Override
    public void add(int pIndex, E pValue) {
        Validate.notNull(pValue);
        this.list.add(pIndex, pValue);
    }

    @Override
    public boolean add(E e) {
        Validate.notNull(e);
        return this.list.add(e);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        Validate.notNull(c);
        return this.list.addAll(c);
    }

    @Override
    public E remove(int pIndex) {
        return this.list.remove(pIndex);
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public void clear() {
        if (this.defaultValue == null) {
            super.clear();
        } else {
            for(int i = 0; i < this.size(); ++i) {
                this.set(i, this.defaultValue);
            }
        }
    }
}
