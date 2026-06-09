package committee.nova.mods.avaritia.api.utils.java;


import com.google.common.collect.*;
import committee.nova.mods.avaritia.api.utils.SneakyUtils;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static committee.nova.mods.avaritia.api.utils.SneakyUtils.unsafeCast;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * StreamableIterable
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/7 上午2:40
 */
public interface StreamableIterable<T> extends Iterable<T> {

    /**
     * Static empty instance.
     * You want {@link #empty()}.
     */
    StreamableIterable<?> EMPTY = of(Collections.emptyList());

    /**
     * Returns an empty {@link StreamableIterable}.
     *
     * @return The empty {@link StreamableIterable}
     */
    static <T> StreamableIterable<T> empty() {
        return unsafeCast(EMPTY);
    }

    /**
     * Constructs a {@link StreamableIterable} wrapper from the
     * given {@link Iterable}.
     *
     * @param itr The {@link Iterable} to wrap.
     * @return The {@link StreamableIterable}
     */
    @SuppressWarnings("unchecked")
    static <T> StreamableIterable<T> of(Iterable<? extends T> itr) {
        if (itr instanceof StreamableIterable) return (StreamableIterable<T>) itr;

        return ((Iterable<T>) itr)::iterator;
    }

    /**
     * Returns an empty {@link StreamableIterable}.
     *
     * @return The empty {@link StreamableIterable}
     */
    static <T> StreamableIterable<T> of() {
        return unsafeCast(EMPTY);
    }

    /**
     * Creates a {@link StreamableIterable} for single input.
     *
     * @param thing The thing.
     * @return the {@link StreamableIterable}.
     */
    static <T> StreamableIterable<T> of(T thing) {
        return of(singletonList(thing));
    }

    /**
     * Creates an {@link StreamableIterable} for a Nullable value.
     * <p>
     * If the value provided is <code>null</code>, this method will
     * return {@link #empty()}, otherwise it will return a new {@link StreamableIterable}
     * containing only the value provided.
     *
     * @param thing The thing.
     * @return The {@link StreamableIterable}.
     */
    static <T> StreamableIterable<T> ofNullable(@Nullable T thing) {
        if (thing == null) return empty();
        return of(thing);
    }

    /**
     * Creates an {@link StreamableIterable} for an {@link Optional} value.
     * <p>
     * If the value provided is not present, this method will
     * return {@link #empty()}, otherwise it will return a new {@link StreamableIterable}
     * containing only the value provided.
     *
     * @param optional The thing.
     * @return The {@link StreamableIterable}.
     */
    static <T> StreamableIterable<T> of(Optional<? extends T> optional) {
        return optional.map(StreamableIterable::<T>of).orElse(empty());
    }

    /**
     * Creates an {@link StreamableIterable} from a variable list of inputs.
     *
     * @param things The things.
     * @return the {@link StreamableIterable}.
     */
    @SafeVarargs
    static <T> StreamableIterable<T> of(T... things) {
        return of(asList(things));
    }

    /**
     * Returns an {@link StreamableIterable} containing the elements
     * from all the provided iterables concatenated together.
     *
     * @param iterables The {@link Iterable}s to concatenate.
     * @return The concatenated {@link StreamableIterable}.
     */
    @SafeVarargs
    static <T> StreamableIterable<T> concat(Iterable<? extends T>... iterables) {
        return of(Iterables.concat(iterables));
    }

    /**
     * Returns an {@link StreamableIterable} containing the elements
     * from all the provided iterables concatenated together.
     *
     * @param iterables The {@link Iterable}s to concatenate.
     * @return The concatenated {@link StreamableIterable}.
     */
    static <T> StreamableIterable<T> concatMany(Iterable<? extends Iterable<? extends T>> iterables) {
        return of(Iterables.concat(iterables));
    }

    /**
     * Concatenates another {@link StreamableIterable} onto this one.
     *
     * @param other The other {@link Iterable} to append.
     * @return The new concatenated {@link StreamableIterable}.
     */
    default StreamableIterable<T> concat(Iterable<? extends T> other) {
        return concat(this, other);
    }

    /**
     * Filter the elements in this {@link StreamableIterable} by matching a {@link Predicate}.
     * <p>
     * All elements which match this Predicate will be in the resulting {@link StreamableIterable}.
     *
     * @param pred The predicate.
     * @return A wrapped {@link StreamableIterable} with the filter applied.
     */
    default StreamableIterable<T> filter(Predicate<? super T> pred) {
        return () -> new AbstractIterator<T>() {
            private final Iterator<T> itr = iterator();

            @Override
            protected T computeNext() {
                while (itr.hasNext()) {
                    T e = itr.next();
                    if (pred.test(e)) {
                        return e;
                    }
                }
                return endOfData();
            }
        };
    }

    /**
     * Filter the elements in this {@link StreamableIterable} by matching a {@link Predicate}.
     * <p>
     * All elements which do not match this Predicate will be in the resulting {@link StreamableIterable}.
     *
     * @param pred The predicate.
     * @return A wrapped {@link StreamableIterable} with the filter applied.
     */
    default StreamableIterable<T> filterNot(Predicate<? super T> pred) {
        return filter(pred.negate());
    }

    /**
     * Transform the elements in this {@link StreamableIterable}.
     *
     * @param func The {@link Function} to transform with.
     * @return A wrapped {@link StreamableIterable} with the mapped elements.
     */
    default <R> StreamableIterable<R> map(Function<? super T, ? extends R> func) {
        return () -> new Iterator<R>() {
            private final Iterator<T> itr = iterator();

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public R next() {
                return func.apply(itr.next());
            }

            @Override
            public void remove() {
                itr.remove();
            }
        };
    }

    /**
     * Flat map the elements in this {@link StreamableIterable}.
     *
     * @param func The function to get/create the iterable for a given element.
     * @return A wrapped {@link StreamableIterable} with the flat mapped elements.
     */
    default <R> StreamableIterable<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> func) {
        return () -> new AbstractIterator<R>() {
            private final Iterator<T> itr = iterator();
            @Nullable
            Iterator<? extends R> working = null;

            @Override
            protected R computeNext() {
                while (true) {
                    if (working == null) {
                        if (itr.hasNext()) {
                            working = func.apply(itr.next()).iterator();
                        } else {
                            break;
                        }
                    }
                    if (working.hasNext()) {
                        return working.next();
                    }
                    working = null;
                }
                return endOfData();
            }
        };
    }

    /**
     * Filter the elements in this {@link StreamableIterable} by their hashcode/equals.
     *
     * @return A wrapped {@link StreamableIterable} with the filtered elements.
     */
    default StreamableIterable<T> distinct() {
        Set<T> set = new HashSet<>();
        return filter(set::add);
    }

    /**
     * Peek the elements of this {@link StreamableIterable} as they are consumed.
     *
     * @param action The consumer.
     * @return A wrapped {@link StreamableIterable} with the peek function applied.
     */
    default StreamableIterable<T> peek(Consumer<? super T> action) {
        return () -> new Iterator<T>() {
            private final Iterator<T> itr = iterator();

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public T next() {
                T n = itr.next();
                action.accept(n);
                return n;
            }
        };
    }

    /**
     * Limit the number of elements returned by this {@link StreamableIterable} to the given value.
     *
     * @param max The limit. -1 for infinite.
     * @return A wrapped {@link StreamableIterable} with the max filter applied.
     * In the event <code>-1<</code> is supplied, the same {@link StreamableIterable} will be provided.
     * In the event <code>0</code> is supplied, an empty {@link StreamableIterable} will be provided.
     */
    default StreamableIterable<T> limit(@Range(from = -1, to = Integer.MAX_VALUE) int max) {
        // No point doing filtering.
        if (max == -1) return this;
        if (max == 0) return Collections::emptyIterator;

        return () -> new AbstractIterator<T>() {
            private final Iterator<T> itr = iterator();
            private int count = 0;

            @Override
            protected T computeNext() {
                if (!itr.hasNext()) return endOfData();
                if (count++ >= max) return endOfData();
                return itr.next();
            }
        };
    }

    /**
     * Skip n number of elements in this {@link StreamableIterable}.
     *
     * @param n The number of elements to skip.
     * @return A wrapped {@link StreamableIterable} with the max filter applied.
     * In the event <code>0</code> is supplied, the same {@link StreamableIterable} will be provided.
     */
    default StreamableIterable<T> skip(@Range(from = 0, to = Integer.MAX_VALUE) int n) {
        if (n == 0) return this;

        return () -> new AbstractIterator<T>() {
            private final Iterator<T> itr = iterator();
            private int count = 0;

            @Override
            protected T computeNext() {
                while (itr.hasNext()) {
                    T next = itr.next();
                    if (count++ < n) {
                        continue;
                    }
                    return next;
                }
                return endOfData();
            }
        };
    }

    /**
     * Collect this {@link StreamableIterable} to an array.
     *
     * @return The array.
     */
    default Object[] toArray() {
        return toList().toArray();
    }

    /**
     * Collect this {@link StreamableIterable} to an array.
     *
     * @param arr The array to add the elements to.
     *            If all elements to not fit in this array a new array
     *            will be returned of appropriate size.
     * @return Either the array passed in or one of appropriate size
     * with all elements of the Iterable.
     */
    default T[] toArray(T[] arr) {
        return toList().toArray(arr);
    }

    /**
     * Folds each element of this {@link StreamableIterable} into the previous.
     *
     * @param identity    The starting element.
     * @param accumulator The function to fold elements with.
     * @return The result.
     * @deprecated Remains for ABI compat.
     */
    @Nullable
    @Contract("!null,_ -> !null")
    @Deprecated
    @ScheduledForRemoval(inVersion = "0.5.0")
    default T fold(@Nullable T identity, BinaryOperator<@Nullable T> accumulator) {
        return fold(identity, (BiFunction<@Nullable T, T, T>) accumulator);
    }

    /**
     * Folds each element of this {@link StreamableIterable} into the previous.
     *
     * @param identity    The starting element.
     * @param accumulator The function to fold elements with.
     * @return The result.
     */
    @Nullable
    @Contract("!null,_ -> !null")
    default <U> U fold(@Nullable U identity, BiFunction<? super @Nullable U, ? super T, ? extends U> accumulator) {
        U ret = identity;
        for (T t : this) {
            ret = accumulator.apply(ret, t);
        }
        return ret;
    }

    /**
     * Folds each element of this {@link StreamableIterable} into the previous.
     *
     * @param accumulator The function to fold elements with.
     * @return The result. Empty if no elements exist.
     */
    default Optional<T> fold(BinaryOperator<T> accumulator) {
        boolean found = false;
        T ret = null;
        for (T t : this) {
            if (found) {
                ret = accumulator.apply(ret, t);
            } else {
                ret = t;
            }
            found = true;
        }
        return found ? Optional.ofNullable(ret) : Optional.empty();
    }

    /**
     * Count the number of elements in this {@link StreamableIterable}.
     *
     * @return The number of elements.
     */
    default int count() {
        int i = 0;
        for (T ignored : this) {
            i++;
        }
        return i;
    }

    /**
     * Map all elements of this {@link StreamableIterable} to an int,
     * then sum the result.
     *
     * @param func The mapping function.
     * @return The sum result.
     */
    default int intSum(ToIntFunction<? super T> func) {
        int i = 0;
        for (T thing : this) {
            i += func.applyAsInt(thing);
        }
        return i;
    }

    /**
     * Map all elements of this {@link StreamableIterable} to a long,
     * then sum the result.
     *
     * @param func The mapping function.
     * @return The sum result.
     */
    default long longSum(ToLongFunction<? super T> func) {
        long l = 0;
        for (T thing : this) {
            l += func.applyAsLong(thing);
        }
        return l;
    }

    /**
     * Map all elements of this {@link StreamableIterable} to a double,
     * then sum the result.
     *
     * @param func The mapping function.
     * @return The sum result.
     */
    default double doubleSum(ToDoubleFunction<? super T> func) {
        double d = 0;
        for (T thing : this) {
            d += func.applyAsDouble(thing);
        }
        return d;
    }

    /**
     * @return If this {@link StreamableIterable} is empty.
     */
    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    /**
     * Check if any elements in this {@link StreamableIterable} match the given predicate.
     *
     * @param test The predicate.
     * @return The result.
     */
    default boolean anyMatch(Predicate<? super T> test) {
        for (T t : this) {
            if (test.test(t)) return true;
        }
        return false;
    }

    /**
     * Check if all elements in this {@link StreamableIterable} match the given predicate.
     *
     * @param test The predicate.
     * @return The result.
     */
    default boolean allMatch(Predicate<? super T> test) {
        for (T t : this) {
            if (!test.test(t)) return false;
        }
        return true;
    }

    /**
     * Check if no elements in this {@link StreamableIterable} match the given predicate.
     *
     * @param test The predicate.
     * @return The result.
     */
    default boolean noneMatch(Predicate<? super T> test) {
        for (T t : this) {
            if (test.test(t)) return false;
        }
        return true;
    }

//    /**
//     * Optionally get the first element within this {@link StreamableIterable}.
//     *
//     * @return The last element.
//     */
//    default Optional<T> findFirst() {
//        return ColUtils.headOption(this);
//    }
//
//    /**
//     * Asserts the {@link StreamableIterable} is not empty and returns the first element.
//     *
//     * @return The first element.
//     * @throws IllegalArgumentException If the {@link StreamableIterable} is empty.
//     */
//    default T first() {
//        return ColUtils.head(this);
//    }
//
//    /**
//     * Returns the first element in the {@link StreamableIterable} otherwise
//     * <code>null</code>.
//     *
//     * @return The first element, or <code>null</code>.
//     */
//    @Nullable
//    default T firstOrDefault() {
//        return ColUtils.headOrDefault(this);
//    }
//
//    /**
//     * Returns the first element in the {@link StreamableIterable} otherwise
//     * the supplied default.
//     *
//     * @param _default The default if the {@link StreamableIterable} is empty.
//     * @return The first element, or the default.
//     */
//    @Nullable
//    @Contract ("!null -> !null")
//    default T firstOrDefault(@Nullable T _default) {
//        return ColUtils.headOrDefault(this, _default);
//    }
//
//    /**
//     * Optionally get the last element within this {@link StreamableIterable}.
//     *
//     * @return The last element.
//     */
//    default Optional<T> findLast() {
//        return ColUtils.tailOption(this);
//    }
//
//    /**
//     * Asserts the {@link StreamableIterable} is not empty and returns the last element.
//     *
//     * @return The last element.
//     * @throws IllegalArgumentException If the {@link StreamableIterable} is empty.
//     */
//    default T last() {
//        return ColUtils.tail(this);
//    }
//
//    /**
//     * Returns the last element in the {@link StreamableIterable} otherwise
//     * <code>null</code>.
//     *
//     * @return The last element, or <code>null</code>.
//     */
//    @Nullable
//    default T lastOrDefault() {
//        return ColUtils.tailOrDefault(this);
//    }
//
//    /**
//     * Returns the last element in the {@link StreamableIterable} otherwise
//     * the supplied default.
//     *
//     * @param _default The default if the {@link StreamableIterable} is empty.
//     * @return The last element, or the default.
//     */
//    @Nullable
//    @Contract ("!null -> !null")
//    default T lastOrDefault(@Nullable T _default) {
//        return ColUtils.tailOrDefault(this, _default);
//    }
//
//    /**
//     * Returns the only element in this {@link StreamableIterable}.
//     *
//     * @return The single element.
//     * @throws IllegalArgumentException If there are none, or more than one element.
//     */
//    default T only() {
//        return ColUtils.only(this);
//    }
//
//    /**
//     * Returns the first element found in the {@link StreamableIterable} if it is the only element,
//     * otherwise <code>null</code> is returned.
//     *
//     * @return The first element or <code>null</code>.
//     */
//    @Nullable
//    default T onlyOrDefault() {
//        return ColUtils.onlyOrDefault(this, null);
//    }
//
//    /**
//     * Returns the first element found in the {@link StreamableIterable} if it is the only element,
//     * otherwise the default value is returned.
//     *
//     * @param _default The default value, in the event the {@link StreamableIterable} is empty, or has more than one element.
//     * @return The first element or the default.
//     */
//    @Nullable
//    @Contract ("!null -> !null")
//    default T onlyOrDefault(@Nullable T _default) {
//        return ColUtils.onlyOrDefault(this, _default);
//    }
//
//    /**
//     * Returns the element with the highest value returned by the supplied function.
//     *
//     * @param func The function.
//     * @return The element with the highest value.
//     */
//    default T maxBy(ToIntFunction<T> func) {
//        return ColUtils.requireMaxBy(this, func);
//    }
//
//    /**
//     * Returns the element with the highest value returned by the supplied function.
//     *
//     * @param func The function.
//     * @return The element with the highest value or null.
//     */
//    @Nullable
//    default T maxByOrDefault(ToIntFunction<T> func) {
//        return maxByOrDefault(func, null);
//    }
//
//    /**
//     * Returns the element with the highest value returned by the supplied function.
//     *
//     * @param func     The function.
//     * @param _default The default value of the collection is empty.
//     * @return The element with the highest value or the default value.
//     */
//    @Nullable
//    @Contract ("_,!null -> !null")
//    default T maxByOrDefault(ToIntFunction<T> func, @Nullable T _default) {
//        return ColUtils.maxByOrDefault(this, func, _default);
//    }

    /**
     * Collect this {@link StreamableIterable} to an {@link ArrayList}.
     *
     * @return The {@link ArrayList}.
     */
    default ArrayList<T> toList() {
        return Lists.newArrayList(this);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link LinkedList}.
     *
     * @return The {@link LinkedList}.
     */
    default LinkedList<T> toLinkedList() {
        return Lists.newLinkedList(this);
    }

    /**
     * Collect this {@link StreamableIterable} to an {@link ImmutableList}.
     *
     * @return The {@link ImmutableList}.
     */
    default ImmutableList<T> toImmutableList() {
        return ImmutableList.copyOf(this);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link HashSet}.
     *
     * @return The {@link HashSet}.
     */
    default HashSet<T> toSet() {
        return Sets.newHashSet(this);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link LinkedHashSet}.
     *
     * @return The {@link LinkedHashSet}.
     */
    default LinkedHashSet<T> toLinkedHashSet() {
        return Sets.newLinkedHashSet(this);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link LinkedHashSet}.
     *
     * @return The {@link LinkedHashSet}.
     */
    default ImmutableSet<T> toImmutableSet() {
        return ImmutableSet.copyOf(this);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link HashMap}.
     * This method will always resolve the existing element on collision.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @return The {@link Map}.
     */
    default <K, V> HashMap<K, V> toMap(Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return toMap(new HashMap<>(), keyFunc, valueFunc);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link HashMap}.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @param mergeFunc The function for merging 2 values on collision. (Left existing, Right toAdd)
     * @return The {@link Map}.
     */
    default <K, V> HashMap<K, V> toMap(Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunc) {
        return toMap(new HashMap<>(), keyFunc, valueFunc, mergeFunc);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link LinkedHashMap}.
     * This method will always resolve the existing element on collision.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @return The {@link Map}.
     */
    default <K, V> HashMap<K, V> toLinkedHashMap(Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return toMap(new LinkedHashMap<>(), keyFunc, valueFunc);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link LinkedHashMap}.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @param mergeFunc The function for merging 2 values on collision. (Left existing, Right toAdd)
     * @return The {@link Map}.
     */
    default <K, V> HashMap<K, V> toLinkedHashMap(Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunc) {
        return toMap(new LinkedHashMap<>(), keyFunc, valueFunc, mergeFunc);
    }

    /**
     * Collect this {@link StreamableIterable} to am {@link ImmutableMap}.
     * <p>
     * This function collects to an intermediate {@link LinkedHashMap} internally,
     * iteration order will be preserved.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @return The {@link ImmutableMap}.
     */
    default <K, V> ImmutableMap<K, V> toImmutableMap(Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return ImmutableMap.copyOf(toLinkedHashMap(keyFunc, valueFunc));
    }

    /**
     * Collect this {@link StreamableIterable} to am {@link ImmutableMap}.
     * <p>
     * This function collects to an intermediate {@link LinkedHashMap} internally,
     * iteration order will be preserved.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @param mergeFunc The function for merging 2 values on collision. (Left existing, Right toAdd)
     * @return The {@link ImmutableMap}.
     */
    default <K, V> ImmutableMap<K, V> toImmutableMap(Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunc) {
        return ImmutableMap.copyOf(toLinkedHashMap(keyFunc, valueFunc, mergeFunc));
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link Map}.
     * This method will always resolve the existing element on collision.
     *
     * @param map       The map to add the elements to.
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @return The same map that was passed in.
     */
    default <K, V, M extends Map<K, V>> M toMap(M map, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return toMap(map, keyFunc, valueFunc, SneakyUtils.first());
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link Map}.
     *
     * @param map       The map to add the elements to.
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @param mergeFunc The function for merging 2 values on collision. (Left existing, Right toAdd)
     * @return The same map that was passed in.
     */
    default <K, V, M extends Map<K, V>> M toMap(M map, Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunc) {
        for (T t : this) {
            K key = keyFunc.apply(t);
            V value = valueFunc.apply(t);
            V existing = map.get(key);
            if (existing == null) {
                map.put(key, valueFunc.apply(t));
            } else {
                map.put(key, mergeFunc.apply(existing, value));
            }
        }
        return map;
    }

    /**
     * Collects this {@link StreamableIterable} into a {@link HashMap} grouped by a key.
     * <p>
     * This function uses {@link ArrayList} values.
     *
     * @param keyFunc The function to extract the {@link Map} key.
     * @return The {@link HashMap} representing the grouped values.
     */
    default <K> HashMap<K, List<T>> groupBy(Function<T, K> keyFunc) {
        return groupBy(keyFunc, Function.identity());
    }

    /**
     * Collects this {@link StreamableIterable} into a {@link HashMap} grouped by a key.
     * <p>
     * This function uses {@link ArrayList} values.
     *
     * @param keyFunc   The function to extract the {@link Map} key.
     * @param valueFunc The function to extract the {@link List} value.
     * @return The {@link HashMap} representing the grouped values.
     */
    default <K, V> HashMap<K, List<V>> groupBy(Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return groupBy(new HashMap<>(), ArrayList::new, keyFunc, valueFunc);
    }

    /**
     * Collects this {@link StreamableIterable} into a {@link LinkedHashMap} grouped by a key.
     * <p>
     * This function uses {@link LinkedList} values.
     *
     * @param keyFunc The function to extract the {@link LinkedHashMap} key.
     * @return The {@link LinkedHashMap} representing the grouped values.
     */
    default <K> LinkedHashMap<K, List<T>> groupByLinked(Function<T, K> keyFunc) {
        return groupByLinked(keyFunc, Function.identity());
    }

    /**
     * Collects this {@link StreamableIterable} into a {@link LinkedHashMap} grouped by a key.
     * <p>
     * This function uses {@link LinkedList} values.
     * <p>
     * This function will preserve iteration order.
     *
     * @param keyFunc   The function to extract the {@link LinkedHashMap} key.
     * @param valueFunc The function to extract the {@link LinkedList} value.
     * @return The {@link LinkedHashMap} representing the grouped values.
     */
    default <K, V> LinkedHashMap<K, List<V>> groupByLinked(Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return groupBy(new LinkedHashMap<>(), LinkedList::new, keyFunc, valueFunc);
    }

    /**
     * Collects this {@link StreamableIterable} into a {@link ImmutableMap} grouped by a key.
     * <p>
     * This function uses {@link ImmutableList} values.
     * <p>
     * This function will preserve iteration order.
     *
     * @param keyFunc The function to extract the {@link ImmutableMap} key.
     * @return The {@link ImmutableMap} representing the grouped values.
     */
    default <K> ImmutableMap<K, List<T>> groupByImmutable(Function<T, K> keyFunc) {
        return groupByImmutable(keyFunc, Function.identity());
    }

    /**
     * Collects this {@link StreamableIterable} into a {@link ImmutableMap} grouped by a key.
     * <p>
     * This function uses {@link ImmutableList} values.
     * <p>
     * This function will preserve iteration order.
     *
     * @param keyFunc   The function to extract the {@link ImmutableMap} key.
     * @param valueFunc The function to extract the {@link ImmutableList} value.
     * @return The {@link ImmutableMap} representing the grouped values.
     */
    default <K, V> ImmutableMap<K, List<V>> groupByImmutable(Function<T, K> keyFunc, Function<T, V> valueFunc) {
        ImmutableMap.Builder<K, List<V>> builder = ImmutableMap.builder();
        for (Map.Entry<K, List<V>> entry : groupByLinked(keyFunc, valueFunc).entrySet()) {
            builder.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
        }
        return builder.build();
    }

    /**
     * Collects this {@link StreamableIterable} into a {@link Map} grouped by a key.
     *
     * @param map         The map to collect into.
     * @param listFactory The function to construct {@link List} instances.
     * @param keyFunc     The function to extract the {@link Map} key.
     * @param valueFunc   The function to extract the {@link List} value.
     * @return The same map that was passed in.
     */
    default <K, V, L extends List<V>, M extends Map<K, L>> M groupBy(M map, Supplier<L> listFactory, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        for (T t : this) {
            K key = keyFunc.apply(t);
            L list = map.computeIfAbsent(key, k -> listFactory.get());
            list.add(valueFunc.apply(t));
        }
        return map;
    }

    /**
     * Join all elements of the {@link StreamableIterable} together into a {@link String}, seperated by {@code sep}.
     *
     * @param sep The separator.
     * @return The joined string.
     */
    default String join(String sep) {
        StringBuilder str = new StringBuilder();
        boolean first = true;
        for (T t : this) {
            if (!first) {
                str.append(sep);
            }
            first = false;
            str.append(t);
        }

        return str.toString();
    }

    /**
     * Convert this {@link StreamableIterable} to a {@link Stream}.
     *
     * @return The {@link Stream}
     */
    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Convert this {@link StreamableIterable} to a parallel {@link Stream}.
     *
     * @return The {@link Stream}
     */
    default Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
}