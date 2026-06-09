package committee.nova.mods.avaritia.api.utils.java;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import committee.nova.mods.avaritia.api.utils.SneakyUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

import static committee.nova.mods.avaritia.api.utils.SneakyUtils.unsafeCast;

/**
 * FastStream
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/7 上午3:10
 */
public interface FastStream<T> extends Iterable<T> {

    // region Creation, Constants and Composing.
    /**
     * Static empty instance.
     * You want {@link #empty()} for return type inference.
     */
    FastStream<?> EMPTY = new Internal.Empty<>();

    /**
     * Returns an empty {@link FastStream} singleton.
     *
     * @return An empty {@link FastStream}.
     */
    static <T> FastStream<T> empty() {
        return unsafeCast(EMPTY);
    }

    /**
     * Overload of {@link #empty()} for convenience.
     *
     * @return An empty {@link FastStream}.
     */
    static <T> FastStream<T> of() {
        return empty();
    }

    /**
     * Wraps the provided {@link Iterable} to a {@link FastStream}
     * <p>
     * This method may return the same object if it's already a {@link FastStream},
     * the empty singleton if it's a {@link Collection} and provably empty,
     * or a new Wrapped instance.
     * <p>
     * The Wrapped instance is careful to expose the underlying iterator of the provided
     * {@link Iterable}, as well as forward calls to {@link Iterable#forEach}
     *
     * @param itr The {@link Iterable} to wrap.
     * @return The wrapped Iterable.
     */
    @SuppressWarnings("unchecked")
    static <T> FastStream<T> of(Iterable<? extends T> itr) {
        if (itr instanceof FastStream) return (FastStream<T>) itr;

        int knownLength = Internal.knownLength(itr, false);
        if (knownLength == 0) return empty();

        return new Wrapped<>((Iterable<T>) itr, knownLength);
    }

    /**
     * Wraps the provided {@link Spliterator} to a {@link FastStream}.
     *
     * @param itr The {@link Spliterator}.
     * @return The {@link FastStream}
     */
    @SuppressWarnings("unchecked")
    static <T> FastStream<T> of(Spliterator<? extends T> itr) {
        long exactSize = itr.getExactSizeIfKnown();
        if (exactSize == 0) return empty();
        // We can't express streams with more elements than Int.MAX here.
        // If this is ever actually used, I would be shocked.
        if (exactSize > Integer.MAX_VALUE) {
            exactSize = -1;
        }

        return new WrappedSpl<>((Spliterator<T>) itr, (int) exactSize);
    }

    /**
     * Wrap the provided Java {@link Stream} to a {@link FastStream}.
     * <p>
     * This method is provided to support wrapping API's which only provide {@link Stream}
     * outputs, into {@link FastStream}. This implicitly uses the {@link #of(Spliterator)}.
     * <p>
     * Where possible, raw {@link Spliterator} or {@link Iterator} inputs should be used.
     * <p>
     * NOTE: Using Java {@link Stream} operations combined with {@link FastStream}
     * operations may result in poor performance.
     *
     * @param stream The stream.
     * @return The {@link FastStream}
     */
    static <T> FastStream<T> of(Stream<? extends T> stream) {
        return of(stream.spliterator());
    }

    /**
     * Returns a {@link FastStream} for a single object.
     *
     * @param thing The thing.
     * @return The {@link FastStream} for the single object.
     */
    static <T> FastStream<T> of(T thing) {
        return new OfSingle<>(thing);
    }

    /**
     * Returns a {@link FastStream} for a single object.
     * <p>
     * If the provided object is {@code null},
     * an empty {@link FastStream} is returned.
     *
     * @param thing The thing.
     * @return The {@link FastStream} for the single object or empty.
     */
    static <T> FastStream<T> ofNullable(@Nullable T thing) {
        return thing != null ? of(thing) : empty();
    }

    /**
     * Returns a {@link FastStream} for an {@link Optional}.
     * <p>
     * If the provided {@link Optional} is not {@link Optional#isPresent()},
     * an empty {@link FastStream} is returned.
     *
     * @param opt The {@link Optional}.
     * @return The {@link FastStream} for the {@link Optional} or empty.
     */
    static <T> FastStream<T> of(Optional<? extends T> opt) {
        return ofNullable(opt.orElse(null));
    }

    /**
     * Returns a {@link FastStream} for an array of objects.
     * <p>
     * This method is preferred over a wrapper such as {@link Arrays#asList}
     * due to fewer allocations and virtual/interface calls.
     *
     * @param things The thing.
     * @return The {@link FastStream} for the objects.
     */
    @SafeVarargs
    static <T> FastStream<T> of(T... things) {
        if (things.length == 0) return empty();

        return new OfN<>(things);
    }

    /**
     * Returns a concatenated {@link FastStream} containing the elements from
     * the provided {@link Iterable} array.
     *
     * @param iterables The {@link Iterable}s to concatenate.
     * @return The concatenated {@link FastStream}.
     */
    @SafeVarargs
    static <T> FastStream<T> concat(Iterable<? extends T>... iterables) {
        if (iterables.length == 0) return empty();
        if (iterables.length == 1) return of(iterables[0]);

        int nonEmptyCount = 0;
        Iterable<? extends T> lastNonEmpty = null;
        for (Iterable<? extends T> iterable : iterables) {
            if (Internal.knownLength(iterable, false) != 0) {
                nonEmptyCount++;
                lastNonEmpty = iterable;
            }
        }
        if (nonEmptyCount == 0) return empty();
        if (nonEmptyCount == 1) return of(lastNonEmpty);

        return new ConcatenatedN<>(iterables);
    }

    /**
     * Returns a concatenated {@link FastStream} containing the elements from
     * the provided {@link Iterable}s {@link Iterable}.
     *
     * @param iterables The {@link Iterable}s to concatenate.
     * @return The concatenated {@link FastStream}.
     */
    static <T> FastStream<T> concatMany(Iterable<? extends Iterable<? extends T>> iterables) {
        return new Concatenated<>(iterables);
    }

    /**
     * Used to nudge Javac to perform looser inference on return types of some
     * collecting functions provided in here.
     */
    static <T> TypeCheck<T, T> infer() {
        return null;
    }
    // endregion

    // region Stream operations

    /**
     * Returns a {@link FastStream} with the provided {@link Iterable} concatenated
     * after.
     *
     * @param other The other
     * @return THe concatenated {@link FastStream}.
     */
    default FastStream<T> concat(Iterable<? extends T> other) {
        if (other == EMPTY) return this;

        return concat(this, other);
    }

    /**
     * Returns a {@link FastStream} containing all elements that pass
     * the provided {@link Predicate} filter.
     *
     * @param pred The {@link Predicate} to apply.
     * @return The filtered {@link FastStream}.
     */
    default FastStream<T> filter(Predicate<? super T> pred) {
        return new Filtered<>(this, pred);
    }

    /**
     * Returns a {@link FastStream} containing all elements that fail
     * the provided {@link Predicate} filter.
     *
     * @param pred The {@link Predicate} to apply.
     * @return The filtered {@link FastStream}.
     */
    default FastStream<T> filterNot(Predicate<? super T> pred) {
        return new Filtered<>(this, pred.negate());
    }

    /**
     * Returns a {@link FastStream} with each element transformed by
     * the provided {@link Function}.
     *
     * @param func The {@link Function} to apply.
     * @return The transformed {@link FastStream}.
     */
    default <R> FastStream<R> map(Function<? super T, ? extends R> func) {
        return new Mapped<>(this, func);
    }

    /**
     * Returns a {@link FastStream} with each element transformed by
     * the provided {@link Function} concatenated together.
     *
     * @param func The {@link Function} to apply producing the {@link Iterable} for concatenation.
     * @return The flat mapped {@link FastStream}.
     */
    default <R> FastStream<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> func) {
        return new FlatMapped<>(this, func);
    }

    /**
     * Returns a {@link FastStream} containing all elements that are unique
     * according to their {@link Object#hashCode()}/{@link Object#equals} identity.
     *
     * @return The distinct filtered {@link FastStream}.
     */
    default FastStream<T> distinct() {
        return new Distinct<>(this);
    }

    /**
     * Returns a {@link FastStream} containing all elements grouped by
     * a key. The provided {@link Function} is used to extract the key from the element.
     *
     * @param keyFunc The key {@link Function}.
     * @return The Grouped {@link FastStream}
     */
    default <K> FastStream<Group<K, T>> groupBy(Function<? super T, ? extends K> keyFunc) {
        return new Grouped<>(this, keyFunc, Function.identity());
    }

    /**
     * Returns a {@link FastStream} containing all elements grouped by
     * a key. The provided {@link Function} is used to extract the key from the element.
     *
     * @param keyFunc   The key {@link Function}.
     * @param valueFunc The value {@link Function}.
     * @return The Grouped {@link FastStream}
     */
    default <K, V> FastStream<Group<K, V>> groupBy(Function<? super T, ? extends K> keyFunc, Function<? super T, ? extends V> valueFunc) {
        return new Grouped<>(this, keyFunc, valueFunc);
    }

    /**
     * Returns a {@link FastStream} sorted based on the elements natural sort order.
     * <p>
     * This requires that {@code T} implements {@link Comparable}.
     *
     * @return The sorted {@link FastStream}.
     */
    default FastStream<T> sorted() {
        // Will throw CCE when we try to sort if T is not Comparable
        return new Sorted<>(this, unsafeCast(Comparator.naturalOrder()));
    }

    /**
     * Returns a {@link FastStream} sorted based on the provided comparator.
     *
     * @param comparator The {@link Comparator} to apply.
     * @return The sorted {@link FastStream}.
     */
    default FastStream<T> sorted(Comparator<? super T> comparator) {
        return new Sorted<>(this, comparator);
    }

    /**
     * Returns a {@link FastStream} in reverse order.
     *
     * @return The reversed {@link FastStream}.
     */
    default FastStream<T> reversed() {
        return new Reversed<>(this);
    }

    /**
     * Returns a {@link FastStream} which listens to all the elements which pass to the next operation.
     *
     * @param cons The listener {@link Consumer}.
     * @return The {@link FastStream}.
     */
    default FastStream<T> peek(Consumer<? super T> cons) {
        return new Peeked<>(this, cons);
    }

    /**
     * Returns a {@link FastStream} which will let at most {@code max} elements pass.
     * <p>
     * A special case of {@code -1} is provided to indicate no max limit.
     *
     * @param max The maximum amount of elements to pass through, or {@code -1}.
     * @return The limited {@link FastStream}.
     */
    default FastStream<T> limit(@Range(from = -1, to = Integer.MAX_VALUE) int max) {
        if (max == -1) return this;
        if (max <= 0) return empty();

        // TODO, special implementation if underlying stream is 'sorted'
        return new Sliced<>(this, 0, max);
    }
    // endregion

    // region Queries.

    /**
     * Returns a {@link FastStream} which will skip {@code n} number of elements.
     *
     * @param n The number of elements to skip.
     * @return The skipping {@link FastStream}.
     */
    default FastStream<T> skip(@Range(from = 0, to = Integer.MAX_VALUE) int n) {
        if (n == 0) return this;

        return new Sliced<>(this, n, Integer.MAX_VALUE);
    }

    /**
     * Tests if any element in the {@link FastStream} matches the provided {@link Predicate}.
     *
     * @param pred The {@link Predicate} to apply.
     * @return If any element matches the {@link Predicate}.
     */
    default boolean anyMatch(Predicate<? super T> pred) {
        try {
            forEach(e -> {
                if (pred.test(e)) {
                    throw new ForEachAbort();
                }
            });
        } catch (ForEachAbort ignored) {
            return true;
        }
        return false;
    }

    /**
     * Tests if all elements in the {@link FastStream} match the provided {@link Predicate}.
     *
     * @param pred The {@link Predicate} to apply.
     * @return If all elements match the {@link Predicate}.
     */
    default boolean allMatch(Predicate<? super T> pred) {
        return !anyMatch(pred.negate());
    }

    /**
     * Tests if no elements in the {@link FastStream} match the provided {@link Predicate}.
     *
     * @param pred The {@link Predicate} to apply.
     * @return If no elements match the {@link Predicate}.
     */
    default boolean noneMatch(Predicate<? super T> pred) {
        return allMatch(pred.negate());
    }

    /**
     * Tests if the stream is empty.
     * <p>
     * This method is provided for convenience. Generally, using {@link #knownLength} or
     * a collecting operation would achieve greater performance.
     *
     * @return If the stream is empty.
     */
    default boolean isEmpty() {
        return knownLength() == 0 || !iterator().hasNext();
    }

    /**
     * Returns the known length for the stream.
     * <p>
     * This may return {@code -1} if the stream contains operations
     * that can't know their length before computing (filter operations).
     *
     * @return The known length, or {@code -1}
     */
    default int knownLength() {
        return knownLength(false);
    }

    /**
     * Returns the known length for the stream.
     * <p>
     * If {@code true} is specified to {@code consumeToCalculate} causes
     * operations which would not normally know their length prior to a
     * terminal operation being applied, to cache their result under the assumption
     * that the stream is about to be fully consumed.
     *
     * @param consumeToCalculate If the caller intends to consume the entire stream via {@link #forEach} after calling.
     * @return The known length for the stream. {code -1} if the length is not known.
     */
    default int knownLength(boolean consumeToCalculate) {
        return -1;
    }
    //endregion

    // region Terminal operations

    /**
     * Evaluates the stream, counting the number of elements contained within.
     *
     * @return The number of elements in the stream.
     */
    default int count() {
        int len = knownLength(true);
        if (len >= 0) return len;

        final class Cons implements Consumer<T> {

            private int count;

            @Override
            public void accept(T t) {
                count++;
            }
        }
        Cons cons = new Cons();
        forEach(cons);
        return cons.count;
    }

    /**
     * Returns the result of a folding operation applied to the {@link FastStream} contents.
     *
     * @param identity    The initial value.
     * @param accumulator The {@link Function} responsible for merging elements in the stream together.
     * @return The result of the fold operation. May be {@code null} if {@code identity} is null and the stream is empty.
     */
    @Nullable
    @Contract("!null,_ -> !null")
    default <U> U fold(@Nullable U identity, BiFunction<? super @Nullable U, ? super T, ? extends U> accumulator) {
        final class Cons implements Consumer<T> {

            @Nullable
            U ret = identity;

            @Override
            public void accept(T t) {
                ret = accumulator.apply(ret, t);
            }
        }
        Cons cons = new Cons();
        forEach(cons);
        return cons.ret;
    }

    /**
     * Returns the result of a folding operation applied to the {@link FastStream} contents.
     *
     * @param accumulator The {@link Function} responsible for merging elements in the stream together.
     * @return Optionally, the result of the fold operation.
     * Will be empty if the {@link FastStream} contained no elements.
     */
    default Optional<T> fold(BinaryOperator<T> accumulator) {
        final class Cons implements Consumer<T> {

            @Nullable
            T ret = null;
            boolean found = false;

            @Override
            public void accept(T t) {
                if (!found) {
                    ret = t;
                } else {
                    ret = accumulator.apply(ret, t);
                }
                found = true;
            }
        }
        Cons cons = new Cons();
        forEach(cons);
        return cons.found ? Optional.ofNullable(cons.ret) : Optional.empty();
    }

    /**
     * Sum all elements in the stream to an integer, using the
     * provided {@link ToIntFunction} to convert each element to
     * an integer.
     *
     * @param func The {@link ToIntFunction} to apply.
     * @return The sum of the elements.
     */
    default int intSum(ToIntFunction<? super T> func) {
        final class Cons implements Consumer<T> {

            private int sum;

            @Override
            public void accept(T t) {
                sum += func.applyAsInt(t);
            }
        }
        Cons cons = new Cons();
        forEach(cons);
        return cons.sum;
    }

    /**
     * Sum all elements in the stream to a long, using the
     * provided {@link ToLongFunction} to convert each element to
     * a long.
     *
     * @param func The {@link ToLongFunction} to apply.
     * @return The sum of the elements.
     */
    default long longSum(ToLongFunction<? super T> func) {
        final class Cons implements Consumer<T> {

            private long sum;

            @Override
            public void accept(T t) {
                sum += func.applyAsLong(t);
            }
        }
        Cons cons = new Cons();
        forEach(cons);
        return cons.sum;
    }

    /**
     * Sum all elements in the stream to a double, using the
     * provided {@link ToDoubleFunction} to convert each element to
     * a double.
     *
     * @param func The {@link ToDoubleFunction} to apply.
     * @return The sum of the elements.
     */
    default double doubleSum(ToDoubleFunction<? super T> func) {
        final class Cons implements Consumer<T> {

            private double sum;

            @Override
            public void accept(T t) {
                sum += func.applyAsDouble(t);
            }
        }
        Cons cons = new Cons();
        forEach(cons);
        return cons.sum;
    }

    /**
     * @return Optionally, the first element in the stream.
     */
    default Optional<T> findFirst() {
        return ColUtils.headOption(this);
    }

    /**
     * @return The first element in the stream.
     */
    default T first() {
        return ColUtils.head(this);
    }

    /**
     * @return The first element in the stream, or {@code null}.
     */
    @Nullable
    default T firstOrDefault() {
        return ColUtils.headOrDefault(this);
    }

    /**
     * @param _default The default value to return if the stream is empty.
     * @return The first element in the stream, or {@code _default}.
     */
    @Nullable
    @Contract("!null -> !null")
    default T firstOrDefault(@Nullable T _default) {
        return ColUtils.headOrDefault(this, _default);
    }

    /**
     * @return Optionally, the last element in the stream.
     */
    default Optional<T> findLast() {
        return ColUtils.tailOption(this);
    }

    /**
     * @return The last element in the stream.
     */
    default T last() {
        return ColUtils.tail(this);
    }

    /**
     * @return The last element in the stream, or {@code null}.
     */
    @Nullable
    default T lastOrDefault() {
        return ColUtils.tailOrDefault(this);
    }

    /**
     * @param _default The default value to return if the stream is empty.
     * @return The last element in the stream, or {@code _default}.
     */
    @Nullable
    @Contract("!null -> !null")
    default T lastOrDefault(@Nullable T _default) {
        return ColUtils.tailOrDefault(this, _default);
    }

    /**
     * @return Returns the only element in the stream.
     */
    default T only() {
        return ColUtils.only(this);
    }

    /**
     * @return Returns the only element in the steam.
     * {@code null} if the stream is empty or contains more than one element.
     */
    @Nullable
    default T onlyOrDefault() {
        return onlyOrDefault(null);
    }

    /**
     * @param _default The default value to return if the stream is empty or contains more than one element.
     * @return Returns the only element in the stream. {@code _default} if the stream is empty
     * or contains more than one element.
     */
    @Nullable
    @Contract("!null->!null")
    default T onlyOrDefault(@Nullable T _default) {
        return ColUtils.onlyOrDefault(this, _default);
    }

    /**
     * Returns the element in the stream with the highest value returned by
     * the provided {@link ToIntFunction}.
     *
     * @param func The {@link ToIntFunction}.
     * @return The maximum value.
     */
    default T maxBy(ToIntFunction<T> func) {
        T t = maxByOrDefault(func);
        if (t == null) {
            throw new IllegalArgumentException("Not found.");
        }
        return t;
    }

    /**
     * Returns the element in the stream with the highest value returned by
     * the provided {@link ToIntFunction}.
     *
     * @param func The {@link ToIntFunction}.
     * @return The maximum value or {@code null} if the stream is empty.
     */
    @Nullable
    default T maxByOrDefault(ToIntFunction<T> func) {
        return maxByOrDefault(func, null);
    }
    // endregion

    // region Collecting

    /**
     * Returns the element in the stream with the highest value returned by
     * the provided {@link ToIntFunction}.
     *
     * @param func     The {@link ToIntFunction}.
     * @param _default The default value to return if the stream is empty.
     * @return The maximum value or {@code _default} if the stream is empty.
     */
    @Nullable
    @Contract("_,!null->!null")
    default T maxByOrDefault(ToIntFunction<T> func, @Nullable T _default) {
        final class Cons implements Consumer<T> {

            int max = Integer.MIN_VALUE;
            @Nullable
            T maxT = _default;

            @Override
            public void accept(T t) {
                int x = func.applyAsInt(t);
                if (x > max) {
                    maxT = t;
                    max = x;
                }
            }
        }
        Cons cons = new Cons();
        forEach(cons);
        return cons.maxT;
    }

    /**
     * Collects this stream into an {@link ArrayList}.
     *
     * @return The {@link ArrayList}.
     */
    default ArrayList<T> toList() {
        int len = knownLength(true);
        ArrayList<T> list = len < 0 ? new ArrayList<>() : new ArrayList<>(len);
        forEach(list::add);
        return list;
    }

    /**
     * Collects this stream into an {@link ArrayList}.
     *
     * @param check Call {@link #infer()} in this argument for flexible return inference.
     * @return The {@link ArrayList}.
     */
    @SuppressWarnings("unchecked")
    default <R> List<R> toList(TypeCheck<R, ? super T> check) {
        return (List<R>) toList();
    }

    /**
     * Collects this stream into a {@link LinkedList}.
     *
     * @return The {@link LinkedList}.
     */
    default LinkedList<T> toLinkedList() {
        LinkedList<T> list = new LinkedList<>();
        forEach(list::add);
        return list;
    }

    /**
     * Collects this stream into a {@link LinkedList}.
     *
     * @param check Call {@link #infer()} in this argument for flexible return inference.
     * @return The {@link LinkedList}.
     */
    @SuppressWarnings("unchecked")
    default <R> LinkedList<R> toLinkedList(TypeCheck<R, ? super T> check) {
        return (LinkedList<R>) toLinkedList();
    }

    /**
     * Collects this stream into a {@link ImmutableList}.
     *
     * @return The {@link ImmutableList}.
     */

    default ImmutableList<T> toImmutableList() {
        int len = knownLength(true);
        ImmutableList.Builder<T> builder = len < 0 ? ImmutableList.builder() : ImmutableList.builderWithExpectedSize(len);
        forEach(builder::add);
        return builder.build();
    }

    /**
     * Collects this stream into a {@link ImmutableList}.
     *
     * @param check Call {@link #infer()} in this argument for flexible return inference.
     * @return The {@link ImmutableList}.
     */
    @SuppressWarnings("unchecked")

    default <R> ImmutableList<R> toImmutableList(TypeCheck<R, ? super T> check) {
        return (ImmutableList<R>) toImmutableList();
    }

    /**
     * Collects this stream into a {@link HashSet}.
     *
     * @return The {@link HashSet}.
     */
    default HashSet<T> toSet() {
        HashSet<T> list = new HashSet<>();
        forEach(list::add);
        return list;
    }

    /**
     * Collects this stream into a {@link HashSet}.
     *
     * @param check Call {@link #infer()} in this argument for flexible return inference.
     * @return The {@link HashSet}.
     */
    @SuppressWarnings("unchecked")
    default <R> HashSet<R> toSet(TypeCheck<R, ? super T> check) {
        return (HashSet<R>) toSet();
    }

    /**
     * Collects this stream into a {@link LinkedHashSet}.
     *
     * @return The {@link LinkedHashSet}.
     */
    default LinkedHashSet<T> toLinkedHashSet() {
        LinkedHashSet<T> list = new LinkedHashSet<>();
        forEach(list::add);
        return list;
    }

    /**
     * Collects this stream into a {@link LinkedHashSet}.
     *
     * @param check Call {@link #infer()} in this argument for flexible return inference.
     * @return The {@link LinkedHashSet}.
     */
    @SuppressWarnings("unchecked")
    default <R> LinkedHashSet<R> toLinkedHashSet(TypeCheck<R, ? super T> check) {
        return (LinkedHashSet<R>) toLinkedHashSet();
    }

    /**
     * Collects this stream into a {@link ImmutableSet}.
     *
     * @return The {@link ImmutableSet}.
     */
    default ImmutableSet<T> toImmutableSet() {
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        forEach(builder::add);
        return builder.build();
    }

    /**
     * Collects this stream into a {@link ImmutableSet}.
     *
     * @param check Call {@link #infer()} in this argument for flexible return inference.
     * @return The {@link ImmutableSet}.
     */
    @SuppressWarnings("unchecked")
    default <R> ImmutableSet<R> toImmutableSet(TypeCheck<R, ? super T> check) {
        return (ImmutableSet<R>) toImmutableSet();
    }

    /**
     * Collects this stream into an {@link Object}[].
     *
     * @return The {@link Object}[].
     */
    @SuppressWarnings("unchecked")
    default Object[] toArray() {
        return toArray((T[]) new Object[0]);
    }

    /**
     * Collects the stream into a {@code T}[].
     *
     * @param arr The template array to use. If the array is large enough
     *            to fit all elements in the stream, this will be used with
     *            a single {@code null} element after the last element.
     * @return The {@code T}[].
     */
    default T[] toArray(T[] arr) {
        int len = knownLength(true);
        if (len < 0) return toList().toArray(arr);

        T[] array = len > arr.length ? Arrays.copyOf(arr, len) : arr;
        forEach(new Consumer<T>() {
            int i = 0;

            @Override
            public void accept(T t) {
                array[i++] = t;
            }
        });
        if (len < arr.length) {
            array[len] = null;
        }
        return array;
    }

    /**
     * Collects this stream into a {@link HashMap}.
     * <p>
     * In the event of a collision, the first value will be used.
     *
     * @param kFunc The {@link Function} to extracting the key.
     * @param vFunc The {@link Function} to extracting the value.
     * @return The {@link HashMap}.
     */
    default <K, V> HashMap<K, V> toMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc) {
        return toMap(new HashMap<>(), kFunc, vFunc);
    }

    /**
     * Collects this stream into a {@link HashMap}.
     *
     * @param kFunc     The {@link Function} to extracting the key.
     * @param vFunc     The {@link Function} to extracting the value.
     * @param mergeFunc The {@link BinaryOperator} to resolve merge conflicts.
     * @return The {@link HashMap}.
     */
    default <K, V> HashMap<K, V> toMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc, BinaryOperator<V> mergeFunc) {
        return toMap(new HashMap<>(), kFunc, vFunc, mergeFunc);
    }

    /**
     * Collects this stream into a {@link LinkedHashMap}.
     * <p>
     * In the event of a collision, the first value will be used.
     *
     * @param kFunc The {@link Function} to extracting the key.
     * @param vFunc The {@link Function} to extracting the value.
     * @return The {@link LinkedHashMap}.
     */
    default <K, V> LinkedHashMap<K, V> toLinkedHashMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc) {
        return toMap(new LinkedHashMap<>(), kFunc, vFunc);
    }

    /**
     * Collects this stream into a {@link LinkedHashMap}.
     *
     * @param kFunc     The {@link Function} to extracting the key.
     * @param vFunc     The {@link Function} to extracting the value.
     * @param mergeFunc The {@link BinaryOperator} to resolve merge conflicts.
     * @return The {@link LinkedHashMap}.
     */
    default <K, V> LinkedHashMap<K, V> toLinkedHashMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc, BinaryOperator<V> mergeFunc) {
        return toMap(new LinkedHashMap<>(), kFunc, vFunc, mergeFunc);
    }

    /**
     * Collects this stream into an {@link ImmutableMap}.
     * <p>
     * In the event of a collision, the first value will be used.
     *
     * @param kFunc The {@link Function} to extracting the key.
     * @param vFunc The {@link Function} to extracting the value.
     * @return The {@link ImmutableMap}.
     */

    default <K, V> ImmutableMap<K, V> toImmutableMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc) {
        return ImmutableMap.copyOf(toLinkedHashMap(kFunc, vFunc));
    }

    /**
     * Collects this stream into an {@link ImmutableMap}.
     *
     * @param kFunc     The {@link Function} to extracting the key.
     * @param vFunc     The {@link Function} to extracting the value.
     * @param mergeFunc The {@link BinaryOperator} to resolve merge conflicts.
     * @return The {@link ImmutableMap}.
     */

    default <K, V> ImmutableMap<K, V> toImmutableMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc, BinaryOperator<V> mergeFunc) {
        return ImmutableMap.copyOf(toLinkedHashMap(kFunc, vFunc, mergeFunc));
    }

    /**
     * Collects this stream into the provided {@link Map}.
     * <p>
     * In the event of a collision, the first value will be used.
     *
     * @param kFunc The {@link Function} to extracting the key.
     * @param vFunc The {@link Function} to extracting the value.
     * @return The {@link Map}.
     */
    default <K, V, M extends Map<K, V>> M toMap(M map, Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc) {
        return toMap(map, kFunc, vFunc, SneakyUtils.first());
    }

    /**
     * Collects this stream into the provided {@link Map}.
     *
     * @param kFunc     The {@link Function} to extracting the key.
     * @param vFunc     The {@link Function} to extracting the value.
     * @param mergeFunc The {@link BinaryOperator} to resolve merge conflicts.
     * @return The {@link Map}.
     */
    default <K, V, M extends Map<K, V>> M toMap(M map, Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc, BinaryOperator<V> mergeFunc) {
        forEach(t -> {
            K key = kFunc.apply(t);
            V value = vFunc.apply(t);
            V existing = map.get(key);
            if (existing == null) {
                map.put(key, value);
            } else {
                map.put(key, mergeFunc.apply(existing, value));
            }
        });
        return map;
    }
    // endregion

    // region Creation and Composition implementations.

    /**
     * Join all elements of this stream together into a {@link String},
     * separated by {@code sep}.
     * <p>
     * This function will use the element's {@link Object#toString()}.
     *
     * @param sep The seperator.
     * @return The joined {@link String}.
     */
    default String join(String sep) {
        class Cons implements Consumer<T> {

            private final StringBuilder builder = new StringBuilder();
            private boolean first = true;

            @Override
            public void accept(T t) {
                if (!first) {
                    builder.append(sep);
                } else {
                    first = false;
                }
                builder.append(t);
            }
        }
        Cons cons = new Cons();
        forEach(cons);
        return cons.builder.toString();
    }

    /**
     * Wraps a regular {@link Iterable} into a {@link FastStream}.
     */
    final class Wrapped<T> implements FastStream<T> {

        private final Iterable<T> _itr;
        private final int knownLength;

        private Wrapped(Iterable<T> itr, int knownLength) {
            _itr = itr;
            this.knownLength = knownLength;
        }

        @Override
        public Iterator<T> iterator() {
            return _itr.iterator();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            _itr.forEach(action);
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return knownLength;
        }
    }

    /**
     * Wraps a {@link Spliterator} into a {@link FastStream}
     */
    final class WrappedSpl<T> implements FastStream<T> {

        private final Spliterator<T> _itr;
        private final int knownLength;

        private WrappedSpl(Spliterator<T> itr, int knownLength) {
            _itr = itr;
            this.knownLength = knownLength;
        }

        @Override
        public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
                private @Nullable T t;
                private final Consumer<T> cons = e -> t = e;

                @Override
                protected @Nullable T computeNext() {
                    if (_itr.tryAdvance(cons)) {
                        return t;
                    }
                    return endOfData();
                }
            };
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            _itr.forEachRemaining(action);
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return knownLength;
        }
    }

    /**
     * A {@link FastStream} for a single element.
     */
    final class OfSingle<T> implements FastStream<T> {

        private final T thing;

        private OfSingle(T thing) {
            this.thing = thing;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public T next() {
                    if (!hasNext) throw new NoSuchElementException();
                    hasNext = false;
                    return thing;
                }

                @Override
                public void forEachRemaining(Consumer<? super T> action) {
                    if (hasNext) {
                        action.accept(thing);
                    }
                }
            };
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            action.accept(thing);
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return 1;
        }
    }

    /**
     * A {@link FastStream} for an array of elements.
     */
    final class OfN<T> implements FastStream<T> {

        private final T[] things;

        private OfN(T[] things) {
            this.things = things;
        }

        @Override
        public Iterator<T> iterator() {
            return ColUtils.iterator(things);
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            for (T t : things) {
                action.accept(t);
            }
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return things.length;
        }
    }

    /**
     * A {@link FastStream} for an array of concatenated {@link Iterable}s.
     */
    final class ConcatenatedN<T> implements FastStream<T> {

        private final Iterable<? extends T>[] iterables;

        private ConcatenatedN(Iterable<? extends T>[] iterables) {
            this.iterables = iterables;
        }

        @Override
        public Iterator<T> iterator() {
            return new AbstractIterator<T>() {

                private int i = 0;

                @Nullable
                private Iterator<? extends T> working;

                @Override
                protected T computeNext() {
                    while (true) {
                        if (working == null) {
                            if (i >= iterables.length) break;
                            working = iterables[i++].iterator();
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

        @Override
        public void forEach(Consumer<? super T> action) {
            for (Iterable<? extends T> iterable : iterables) {
                iterable.forEach(action);
            }
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            int len = 0;
            for (Iterable<? extends T> iterable : iterables) {
                int ilen = Internal.knownLength(iterable, consumeToCalculate);
                if (ilen < 0) return -1;

                len += ilen;
            }

            return len;
        }
    }
    // endregion

    // region Stream operation implementations.

    /**
     * A {@link FastStream} for an {@link Iterable} of concatenated {@link Iterable}s.
     */
    final class Concatenated<T> implements FastStream<T> {

        private final Iterable<? extends Iterable<? extends T>> iterables;

        private Concatenated(Iterable<? extends Iterable<? extends T>> iterables) {
            this.iterables = iterables;
        }

        @Override
        public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
                private final Iterator<? extends Iterable<? extends T>> itr = iterables.iterator();

                @Nullable
                private Iterator<? extends T> working;

                @Override
                protected T computeNext() {
                    while (true) {
                        if (working == null) {
                            if (!itr.hasNext()) break;
                            working = itr.next().iterator();
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

        @Override
        public void forEach(Consumer<? super T> action) {
            iterables.forEach(e -> e.forEach(action));
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            int len = 0;
            for (Iterable<? extends T> iterable : iterables) {
                int ilen = Internal.knownLength(iterable, consumeToCalculate);
                if (ilen < 0) return -1;

                len += ilen;
            }

            return len;
        }
    }

    /**
     * A {@link FastStream} with a filtering function applied.
     */
    final class Filtered<T> implements FastStream<T> {

        private final FastStream<T> parent;
        private final Predicate<? super T> pred;

        private Filtered(FastStream<T> parent, Predicate<? super T> pred) {
            this.parent = parent;
            this.pred = pred;
        }

        @Override
        public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
                private final Iterator<T> itr = parent.iterator();

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

        @Override
        public void forEach(Consumer<? super T> action) {
            parent.forEach(e -> {
                if (pred.test(e)) {
                    action.accept(e);
                }
            });
        }
    }

    /**
     * A {@link FastStream} with a mapping function applied.
     */
    final class Mapped<T, R> implements FastStream<R> {

        private final FastStream<T> parent;
        private final Function<? super T, ? extends R> func;

        private Mapped(FastStream<T> parent, Function<? super T, ? extends R> func) {
            this.parent = parent;
            this.func = func;
        }

        @Override
        public Iterator<R> iterator() {
            return new Iterator<R>() {
                private final Iterator<T> itr = parent.iterator();

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public R next() {
                    return func.apply(itr.next());
                }
            };
        }

        @Override
        public void forEach(Consumer<? super R> action) {
            parent.forEach(e -> action.accept(func.apply(e)));
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return parent.knownLength(consumeToCalculate);
        }

    }

    /**
     * A {@link FastStream} with a flat mapping option applied
     * to each element flattened together.
     */
    final class FlatMapped<T, R> implements FastStream<R> {

        private final FastStream<T> parent;
        private final Function<? super T, ? extends Iterable<? extends R>> func;

        private FlatMapped(FastStream<T> parent, Function<? super T, ? extends Iterable<? extends R>> func) {
            this.parent = parent;
            this.func = func;
        }

        @Override
        public Iterator<R> iterator() {
            return new AbstractIterator<R>() {

                private final Iterator<T> itr = parent.iterator();

                @Nullable
                private Iterator<? extends R> working;

                @Override
                protected R computeNext() {
                    while (true) {
                        if (working == null) {
                            if (!itr.hasNext()) break;
                            working = func.apply(itr.next()).iterator();
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

        @Override
        public void forEach(Consumer<? super R> action) {
            parent.forEach(e -> func.apply(e).forEach(action));
        }
    }

    /**
     * A {@link FastStream} filtered using {@link Object#hashCode()}/{@link Object#equals} identity.
     */
    final class Distinct<T> implements FastStream<T> {

        private final FastStream<T> parent;
        private int knownLength = -1;

        private Distinct(FastStream<T> parent) {
            this.parent = parent;
        }

        @Override
        public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
                private final Set<T> set = new HashSet<>();
                private final Iterator<T> itr = parent.iterator();

                @Override
                protected T computeNext() {
                    while (itr.hasNext()) {
                        T e = itr.next();
                        if (set.add(e)) {
                            return e;
                        }
                    }
                    knownLength = set.size();
                    return endOfData();
                }
            };
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            Set<T> set = new HashSet<>();
            parent.forEach(e -> {
                if (set.add(e)) {
                    action.accept(e);
                }
            });
            knownLength = set.size();
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return knownLength;
        }

        @Override
        public HashSet<T> toSet() {
            return parent.toSet();
        }

        @Override
        public LinkedHashSet<T> toLinkedHashSet() {
            return parent.toLinkedHashSet();
        }

        @Override
        public ImmutableSet<T> toImmutableSet() {
            return parent.toImmutableSet();
        }
    }

    /**
     * Represents a Key to sequence of values in a {@link #groupBy} grouping.
     */
    final class Group<K, V> implements FastStream<V> {

        private final K key;
        private int size;
        private Object[] values = new Object[1];

        public Group(K key) {
            this.key = key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Iterator<V> iterator() {
            return ColUtils.iterator((V[]) values, 0, size);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEach(Consumer<? super V> action) {
            for (int i = 0; i < size; i++) {
                action.accept((V) values[i]);
            }
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return size;
        }

        public K getKey() {
            return key;
        }

        private void add(V value) {
            resize();
            values[size++] = value;
        }

        private void resize() {
            if (size < values.length) return; // No need for resize.
            values = Arrays.copyOf(values, size > 1024 ? size * 2 : size * 4);
        }

        @Override
        @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
        public V[] toArray(V[] arr) {
            if (arr.length >= size) {
                System.arraycopy(values, 0, arr, 0, size);
                if (arr.length >= size + 1) {
                    arr[size + 1] = null;
                }
                return arr;
            }

            return (V[]) Arrays.copyOf(values, size, arr.getClass());
        }
    }

    /**
     * A {@link FastStream} of elements grouped by a specific key.
     */
    final class Grouped<T, K, V> implements FastStream<Group<K, V>> {

        private final FastStream<T> parent;
        private final Function<? super T, ? extends K> keyFunc;
        private final Function<? super T, ? extends V> valueFunc;

        @Nullable
        private Map<K, Group<K, V>> groups = null;

        public Grouped(FastStream<T> parent, Function<? super T, ? extends K> keyFunc, Function<? super T, ? extends V> valueFunc) {
            this.parent = parent;
            this.keyFunc = keyFunc;
            this.valueFunc = valueFunc;
        }

        @NotNull
        @Override
        public Iterator<Group<K, V>> iterator() {
            return getGroups().values().iterator();
        }

        @Override
        public void forEach(Consumer<? super Group<K, V>> action) {
            getGroups().values().forEach(action);
        }

        private Map<K, Group<K, V>> getGroups() {
            if (groups == null) {
                groups = new HashMap<>();
                parent.forEach(e -> groups.computeIfAbsent(keyFunc.apply(e), Group::new)
                        .add(valueFunc.apply(e)));
            }
            return groups;
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return consumeToCalculate ? getGroups().size() : -1;
        }
    }

    /**
     * A {@link FastStream} sorted by a comparator.
     */
    final class Sorted<T> implements FastStream<T> {

        private final FastStream<T> parent;
        private final Comparator<? super T> comparator;

        T @Nullable [] sorted = null;

        private Sorted(FastStream<T> parent, Comparator<? super T> comparator) {
            this.parent = parent;
            this.comparator = comparator;
        }

        @Override
        public Iterator<T> iterator() {
            return ColUtils.iterator(getSorted());
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            for (T t : getSorted()) {
                action.accept(t);
            }
        }

        private T[] getSorted() {
            if (sorted == null) {
                sorted = unsafeCast(parent.toArray());
                Arrays.sort(sorted, comparator);
            }
            return sorted;
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return consumeToCalculate ? getSorted().length : parent.knownLength(false);
        }

        @Override
        public T[] toArray(T[] arr) {
            T[] sorted = getSorted();
            if (arr.getClass() == sorted.getClass()) {
                this.sorted = null;
                return sorted;
            }
            //noinspection unchecked
            return (T[]) Arrays.copyOf(sorted, sorted.length, arr.getClass());
        }

        @Override
        public ArrayList<T> toList() {
            return new ArrayList<>(Arrays.asList(getSorted()));
        }

        @Override
        public ImmutableList<T> toImmutableList() {
            return ImmutableList.copyOf(getSorted());
        }
    }

    /**
     * A {@link FastStream} in reverse order.
     */
    final class Reversed<T> implements FastStream<T> {

        private final FastStream<T> parent;

        T @Nullable [] reversed = null;

        private Reversed(FastStream<T> parent) {
            this.parent = parent;
        }

        @Override
        public Iterator<T> iterator() {
            return ColUtils.iterator(getReversed());
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            for (T t : getReversed()) {
                action.accept(t);
            }
        }

        private T[] getReversed() {
            if (reversed == null) {
                reversed = unsafeCast(parent.toArray());
                ColUtils.reverse(reversed);
            }
            return reversed;
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return consumeToCalculate ? getReversed().length : parent.knownLength(false);
        }

        @Override
        public T[] toArray(T[] arr) {
            T[] reversed = getReversed();
            if (arr.getClass() == reversed.getClass()) {
                this.reversed = null;
                return reversed;
            }
            //noinspection unchecked
            return (T[]) Arrays.copyOf(reversed, reversed.length, arr.getClass());
        }
    }

    /**
     * A {@link FastStream} with a listener attached.
     */
    final class Peeked<T> implements FastStream<T> {

        private final FastStream<T> parent;
        private final Consumer<? super T> cons;

        private Peeked(FastStream<T> parent, Consumer<? super T> cons) {
            this.parent = parent;
            this.cons = cons;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                private final Iterator<T> itr = parent.iterator();

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public T next() {
                    T n = itr.next();
                    cons.accept(n);
                    return n;
                }
            };
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            parent.forEach(e -> {
                cons.accept(e);
                action.accept(e);
            });
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            return parent.knownLength(consumeToCalculate);
        }
    }
    // endregion

    /**
     * A {@link FastStream} with a min/max filter applied.
     */
    final class Sliced<T> implements FastStream<T> {

        private final FastStream<T> parent;
        private final int min;
        private final int max;

        private Sliced(FastStream<T> parent, int min, int max) {
            this.parent = parent;
            this.min = min;
            this.max = max;
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
                private final Iterator<T> itr = parent.iterator();
                private int i;

                @Nullable
                @Override
                protected T computeNext() {
                    while (i < min && itr.hasNext()) {
                        itr.next();
                        i++;
                    }
                    if (i++ >= max || !itr.hasNext()) return endOfData();
                    return itr.next();
                }
            };
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            ForEachAbort abort = new ForEachAbort();
            try {
                parent.forEach(new Consumer<T>() {
                    int i = 0;

                    @Override
                    public void accept(T t) {
                        int n = i++;
                        if (n < min) return;
                        if (n >= max) throw abort;
                        action.accept(t);
                    }
                });
            } catch (ForEachAbort ex) {
                if (ex != abort) {
                    throw ex;
                }
            }
        }

        @Override
        public int knownLength(boolean consumeToCalculate) {
            int pLen = parent.knownLength(consumeToCalculate);
            if (pLen == -1) return -1;

            return Math.min(Math.max(pLen - min, 0), max);
        }
    }

    // region Type Hax
    final class TypeCheck<T, S> {

        private TypeCheck() {
        }
    }
    // endregion

    // region Internal.
    class Internal {

        private Internal() {
        }

        private static <T> int knownLength(Iterable<? extends T> itr, boolean consumeToCalculate) {
            if (itr instanceof Collection) return ((Collection<?>) itr).size();
            if (itr instanceof FastStream) return ((FastStream<?>) itr).knownLength(consumeToCalculate);
            return -1;
        }

        // @formatter:off
        private static class Empty<T> implements FastStream<T> {
            @Override public Iterator<T> iterator() { return Collections.emptyIterator(); }
            @Override public void forEach(Consumer<? super T> action) { }
            @Override public FastStream<T> concat(Iterable<? extends T> other) { return of(other); }
            @Override public FastStream<T> filter(Predicate<? super T> pred) { return this; }
            @Override public FastStream<T> filterNot(Predicate<? super T> pred) { return this; }
            @Override public <R> FastStream<R> map(Function<? super T, ? extends R> func) { return empty(); }
            @Override public <R> FastStream<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> func) { return empty(); }
            @Override public FastStream<T> distinct() { return this; }
            @Override public <K> FastStream<Group<K, T>> groupBy(Function<? super T, ? extends K> keyFunc) { return empty(); }
            @Override public <K, V> FastStream<Group<K, V>> groupBy(Function<? super T, ? extends K> keyFunc, Function<? super T, ? extends V> valueFunc) { return empty(); }
            @Override public FastStream<T> sorted() { return this; }
            @Override public FastStream<T> sorted(Comparator<? super T> comparator) { return this; }
            @Override public FastStream<T> peek(Consumer<? super T> cons) { return this; }
            @Override public FastStream<T> limit(int max) { return this; }
            @Override public FastStream<T> skip(int n) { return this; }
            @Override @Nullable public <U> U fold(@Nullable U identity, BiFunction<? super @Nullable U, ? super T, ? extends U> accumulator) { return identity; }
            @Override public Optional<T> fold(BinaryOperator<T> accumulator) { return Optional.empty(); }
            @Override public boolean anyMatch(Predicate<? super T> pred) { return false; }
            @Override public boolean allMatch(Predicate<? super T> pred) { return true; }
            @Override public boolean noneMatch(Predicate<? super T> pred) { return true; }
            @Override public boolean isEmpty() { return true; }
            @Override public int knownLength() { return 0; }
            @Override public int knownLength(boolean consumeToCalculate) { return 0; }
            @Override public int count() { return 0; }
            @Override public int intSum(ToIntFunction<? super T> func) { return 0; }
            @Override public long longSum(ToLongFunction<? super T> func) { return 0; }
            @Override public double doubleSum(ToDoubleFunction<? super T> func) { return 0; }
            @Override public Optional<T> findFirst() { return Optional.empty(); }
            @Override public T first() { throw new IllegalArgumentException("Not found."); }
            @Nullable @Override public T firstOrDefault() { return null; }
            @Nullable @Override public T firstOrDefault(@Nullable T _default) { return _default; }
            @Override public Optional<T> findLast() { return Optional.empty(); }
            @Override public T last() { throw new IllegalArgumentException("Not found."); }
            @Nullable @Override public T lastOrDefault() { return null; }
            @Nullable @Override public T lastOrDefault(@Nullable T _default) { return _default; }
            @Override public T only() { throw new IllegalArgumentException("Not found."); }
            @Nullable @Override public T onlyOrDefault() { return null; }
            @Nullable @Override public T onlyOrDefault(@Nullable T _default) { return _default; }
            @Override public T maxBy(ToIntFunction<T> func) { throw new IllegalArgumentException("Not found."); }
            @Nullable @Override public T maxByOrDefault(ToIntFunction<T> func) { return null; }
            @Nullable @Override public T maxByOrDefault(ToIntFunction<T> func, @Nullable T _default) { return _default; }
            @Override public ArrayList<T> toList() { return new ArrayList<>(); }
            @Override public LinkedList<T> toLinkedList() { return new LinkedList<>(); }
            @Override public ImmutableList<T> toImmutableList() { return ImmutableList.of(); }
            @Override public HashSet<T> toSet() { return new HashSet<>(); }
            @Override public LinkedHashSet<T> toLinkedHashSet() { return new LinkedHashSet<>(); }
            @Override public ImmutableSet<T> toImmutableSet() { return ImmutableSet.of(); }
            @Override public Object[] toArray() { return new Object[0]; }
            @Override public T[] toArray(T[] arr) { return ColUtils.fill(arr, null); }
            @Override public <K, V> HashMap<K, V> toMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc) { return new HashMap<>(); }
            @Override public <K, V> HashMap<K, V> toMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc, BinaryOperator<V> mergeFunc) { return new HashMap<>(); }
            @Override public <K, V> LinkedHashMap<K, V> toLinkedHashMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc) { return new LinkedHashMap<>(); }
            @Override public <K, V> LinkedHashMap<K, V> toLinkedHashMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc, BinaryOperator<V> mergeFunc) { return new LinkedHashMap<>(); }
            @Override public <K, V> ImmutableMap<K, V> toImmutableMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc) { return ImmutableMap.of(); }
            @Override public <K, V> ImmutableMap<K, V> toImmutableMap(Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc, BinaryOperator<V> mergeFunc) { return ImmutableMap.of(); }
            @Override public <K, V, M extends Map<K, V>> M toMap(M map, Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc) { return map; }
            @Override public <K, V, M extends Map<K, V>> M toMap(M map, Function<? super T, ? extends K> kFunc, Function<? super T, ? extends V> vFunc, BinaryOperator<V> mergeFunc) { return map; }
            @Override public String join(String sep) { return ""; }
            // @formatter:on
        }
    }
    //endregion
}
