package committee.nova.mods.avaritia.api.utils.java;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static committee.nova.mods.avaritia.api.utils.SneakyUtils.unsafeCast;

/**
 * ColUtils
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/7 上午3:11
 */
public class ColUtils {
    @SuppressWarnings("unchecked")
    public static <T> T[] slice(T[] arr, int from, int until) {
        int low = Math.max(from, 0);
        int high = Math.min(Math.max(until, 0), arr.length);
        int size = Math.max(high - low, 0);
        T[] result = (T[]) Array.newInstance(arr.getClass().getComponentType(), size);
        if (size > 0) {
            System.arraycopy(arr, low, result, 0, size);
        }
        return result;
    }

    /**
     * Returns the element in the array with the highest value returned by the supplied function.
     *
     * @param col  The array.
     * @param func The function.
     * @return The max element or null.
     */
    @Nullable
    public static <T> T maxBy(T[] col, ToIntFunction<T> func) {
        return maxBy(Arrays.asList(col), func);
    }

    /**
     * Returns the element in the Iterable with the highest value returned by the supplied function.
     *
     * @param col  The array.
     * @param func The function.
     * @return The max element or null.
     */
    @Nullable
    @Deprecated
    @ScheduledForRemoval(inVersion = "0.5.0")
    public static <T> T maxBy(Iterable<T> col, ToIntFunction<T> func) {
        return maxByOrDefault(col, func, null);
    }

    /**
     * Returns the element in the Iterable with the highest value returned by the supplied function.
     *
     * @param col  The collection.
     * @param func The function.
     * @return The max element.
     */
    public static <T> T requireMaxBy(Iterable<T> col, ToIntFunction<T> func) {
        T ret = maxByOrDefault(col, func, null);
        if (ret == null) {
            throw new IllegalArgumentException("Not found.");
        }
        return ret;
    }

    /**
     * Returns the element in the Iterable with the highest value returned by the supplied function.
     *
     * @param col      The collection.
     * @param func     The function.
     * @param default_ The default value to return if the collection is empty.
     * @return The max element or the default value.
     */
    @Nullable
    @Contract("_,_,!null -> !null")
    public static <T> T maxByOrDefault(Iterable<T> col, ToIntFunction<T> func, @Nullable T default_) {
        int max = Integer.MIN_VALUE;
        T maxT = null;
        for (T t : col) {
            int x = func.applyAsInt(t);
            if (x > max) {
                maxT = t;
                max = x;
            }
        }
        return maxT == null ? default_ : maxT;
    }

    /**
     * Returns true if all elements in the provided array match the
     * given predicate.
     *
     * @param col  The collection to match.
     * @param func The function to apply.
     * @return If all elements match.
     */
    public static <T> boolean allMatch(T[] col, Predicate<T> func) {
        return allMatch(Arrays.asList(col), func);
    }

    /**
     * Returns true if all elements in the provided Iterable match the
     * given predicate.
     *
     * @param col  The collection to match.
     * @param func The function to apply.
     * @return If all elements match.
     */
    public static <T> boolean allMatch(Iterable<T> col, Predicate<T> func) {
        for (T t : col) {
            if (!func.test(t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if any element in the provided Iterable matches the
     * given predicate.
     *
     * @param col  The collection to match.
     * @param func The function to apply.
     * @return If any elements match.
     */
    public static <T> boolean anyMatch(Iterable<T> col, Predicate<T> func) {
        for (T t : col) {
            if (func.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the first element in the Iterable that matches the given predicate.
     *
     * @param col  The collection.
     * @param func The predicate.
     * @return The Optional result.
     */
    public static <T> Optional<T> findFirst(Iterable<T> col, Predicate<T> func) {
        for (T t : col) {
            if (func.test(t)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    /**
     * Attempts to get the first element of the Iterable.
     *
     * @param col The collection.
     * @return The optional result.
     */
    public static <T> Optional<T> headOption(Iterable<T> col) {
        return Optional.ofNullable(headOrDefault(col));
    }

    /**
     * Asserts the {@link Iterable} is not empty and returns the first element.
     *
     * @param col The {@link Iterable}.
     * @return The first element.
     * @throws IllegalArgumentException If the {@link Iterable} is empty.
     */
    public static <T> T head(Iterable<T> col) {
        T head = headOrDefault(col);
        if (head == null) {
            throw new IllegalArgumentException("Not found.");
        }
        return head;
    }

    /**
     * Returns the first element in the {@link Iterable} otherwise
     * <code>null</code>.
     *
     * @param col The {@link Iterable}.
     * @return The first element, or <code>null</code>.
     */
    @Nullable
    public static <T> T headOrDefault(Iterable<T> col) {
        return headOrDefault(col, null);
    }

    /**
     * Returns the first element in the {@link Iterable} otherwise
     * the supplied default.
     *
     * @param col      The {@link Iterable}.
     * @param _default The default if the {@link Iterable} is empty.
     * @return The first element, or the default.
     */
    @Nullable
    @Contract("_,!null -> !null")
    public static <T> T headOrDefault(Iterable<T> col, @Nullable T _default) {
        class Cons implements Consumer<T> {

            @Nullable
            T t = _default;

            @Override
            public void accept(T t) {
                if (this.t != null) {
                    throw new ForEachAbort();
                }
                this.t = t;
            }
        }
        Cons cons = new Cons();
        try {
            col.forEach(cons);
        } catch (ForEachAbort ignored) {
        }
        return cons.t;
    }

    /**
     * Attempts to get the last element of the Iterable.
     *
     * @param col The collection.
     * @return The optional result.
     */
    public static <T> Optional<T> tailOption(Iterable<T> col) {
        return Optional.ofNullable(tailOrDefault(col));
    }

    /**
     * Asserts the {@link Iterable} is not empty and returns the last element.
     *
     * @param col The {@link Iterable}.
     * @return The last element.
     * @throws IllegalArgumentException If the {@link Iterable} is empty.
     */
    public static <T> T tail(Iterable<T> col) {
        T tail = tailOrDefault(col);
        if (tail == null) {
            throw new IllegalArgumentException("Not found.");
        }
        return tail;
    }

    /**
     * Returns the last element in the {@link Iterable} otherwise
     * <code>null</code>.
     *
     * @param col The {@link Iterable}.
     * @return The last element, or <code>null</code>.
     */
    @Nullable
    public static <T> T tailOrDefault(Iterable<T> col) {
        return tailOrDefault(col, null);
    }

    /**
     * Returns the last element in the {@link Iterable} otherwise
     * the supplied default.
     *
     * @param col      The {@link Iterable}.
     * @param _default The default if the {@link Iterable} is empty.
     * @return The last element, or the default.
     */
    @Nullable
    @Contract("_,!null -> !null")
    public static <T> T tailOrDefault(Iterable<T> col, @Nullable T _default) {
        class Cons implements Consumer<T> {

            @Nullable
            T t = _default;

            @Override
            public void accept(T t) {
                this.t = t;
            }
        }
        Cons cons = new Cons();
        col.forEach(cons);
        return cons.t;
    }

    /**
     * Checks if a map contains all keys passed in.
     *
     * @param map  Map to check.
     * @param keys Keys that must exist.
     * @param <T>  The type of data in the map key.
     * @return False if fail.
     */
    @SafeVarargs
    public static <T> boolean containsKeys(Map<T, ?> map, T... keys) {
        for (T object : keys) {
            if (!map.containsKey(object)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds the value at the first null index in the array.
     *
     * @param array Array to add to.
     * @param value Value to add.
     * @param <T>   Type of value.
     * @return Returns a new array in the event the input was expanded.
     */
    public static <T> T[] addToArrayFirstNull(T[] array, T value) {
        int nullIndex = -1;
        for (int i = 0; i < array.length; i++) {
            T v = array[i];
            if (v == null) {
                nullIndex = i;
                break;
            }
        }
        if (nullIndex == -1) {
            T[] copy = createNewArray(array, array.length + 1);
            System.arraycopy(array, 0, copy, 0, array.length);
            nullIndex = array.length;
            array = copy;
        }
        array[nullIndex] = value;
        return array;
    }

    /**
     * Adds all elements from the array that are not null to the list.
     *
     * @param array Array to grab from.
     * @param list  List to add to.
     * @param <T>   What we are dealing with.
     * @return The modified list.
     */
    public static <T> List<T> addAllNoNull(T[] array, List<T> list) {
        for (T value : array) {
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * Checks if the array is all null.
     *
     * @param array The array to check.
     * @param <T>   What we are dealing with.
     * @return True if the array only contains nulls.
     */
    public static <T> boolean isEmpty(T[] array) {
        for (T value : array) {
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts the elements in the array that are not null.
     *
     * @param array The array to check.
     * @param <T>   What we are dealing with.
     * @return The count of non-null objects in the array.
     */
    public static <T> int countNonNull(T[] array) {
        return count(array, Objects::nonNull);
    }

    /**
     * Counts elements in the array that conform to the Function check.
     *
     * @param array The array to check.
     * @param check The Function to apply to each element.
     * @param <T>   What we are dealing with.
     * @return The count.
     */
    public static <T> int count(T[] array, Function<T, Boolean> check) {
        int counter = 0;
        for (T value : array) {
            if (check.apply(value)) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Fills the array with the specified value.
     * If the value is an instance of Copyable it will call copy.
     *
     * @param array Array to fill.
     * @param value Value to fill with.
     * @param <T>   What we are dealing with.
     */
    public static <T> T[] fill(T[] array, T value) {
        for (int i = 0; i < array.length; i++) {
            T newValue = value;
            if (value instanceof Copyable) {
                Copyable<T> copyable = unsafeCast(value);
                newValue = copyable.copy();
            }
            array[i] = newValue;
        }
        return array;
    }

    /**
     * Fills the array with the specified value.
     * A Function is used to check if the value should be replaced.
     * If the value is an instance of Copyable it will call copy.
     *
     * @param array Array to fill.
     * @param value Value to fill with.
     * @param check Called to decide if the value should be replaced.
     * @param <T>   What we are dealing with.
     */
    public static <T> void fillArray(T[] array, T value, Function<T, Boolean> check) {
        for (int i = 0; i < array.length; i++) {
            if (check.apply(array[i])) {
                T newValue = value;
                if (value instanceof Copyable) {
                    Copyable<T> copyable = unsafeCast(value);
                    newValue = copyable.copy();
                }
                array[i] = newValue;
            }
        }
    }

    /**
     * Basically a wrapper for System.arraycopy with support for Copyable's
     *
     * @param src     The source array.
     * @param srcPos  Starting position in the source array.
     * @param dst     The destination array.
     * @param destPos Starting position in the destination array.
     * @param length  The number of elements to copy.
     */
    public static void arrayCopy(Object src, int srcPos, Object dst, int destPos, int length) {
        System.arraycopy(src, srcPos, dst, destPos, length);
        if (dst instanceof Copyable[]) {
            Object[] oa = (Object[]) dst;
            Copyable<Object>[] c = unsafeCast(dst);
            for (int i = destPos; i < destPos + length; i++) {
                if (c[i] != null) {
                    oa[i] = c[i].copy();
                }
            }
        }
    }

    /**
     * Returns the index of the first occurrence of the specified element in the array.
     * Will return -1 if the element is non existent in the array.
     *
     * @param array  The array to search.
     * @param object Element to find.
     * @param <T>    What we are dealing with.
     * @return The index in the array of the object.
     */
    public static <T> int indexOf(T[] array, T object) {
        if (object == null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                if (object.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the first occurrence of the specified element in the array
     * with the same identity. (Ref compare).
     * Will return -1 if the element is non existent in the array.
     *
     * @param array  The array to search.
     * @param object Element to find.
     * @return The index in the array of the object.
     */
    public static <T> int indexOfRef(T[] array, T object) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == object) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Create a new array using the provided array as a template for both type and length.
     *
     * @param array The template.
     * @param <T>   The type.
     * @return The new array.
     */
    public static <T> T[] createNewArray(T[] array) {
        return createNewArray(array, array.length);
    }

    /**
     * Create a new array using the provided array as a template for the type and with the provided length.
     *
     * @param array  The type template.
     * @param length The new array's length.
     * @param <T>    The type.
     * @return The new array.
     */
    public static <T> T[] createNewArray(T[] array, int length) {
        Class<? extends T[]> newType = unsafeCast(array.getClass());
        T[] copy;
        if (newType.equals(Object[].class)) {
            copy = unsafeCast(new Object[length]);
        } else {
            copy = unsafeCast(newArray(newType.getComponentType(), length));
        }
        return copy;
    }

    /**
     * Creates a new array form its component class.
     *
     * @param arrayClass The component class.
     * @param length     The length.
     * @param <T>        The thing.
     * @return The new array.
     */
    public static <T> T[] newArray(Class<T> arrayClass, int length) {
        return unsafeCast(Array.newInstance(arrayClass, length));
    }

    /**
     * Rolls the array based on the shift.
     * Positive shift means the array will roll to the right.
     * Negative shift means the array will roll to the left.
     *
     * @param input The input array.
     * @param shift The shift amount.
     * @param <T>   The thing.
     * @return The new array.
     */
    public static <T> T[] rollArray(T[] input, int shift) {
        T[] newArray = createNewArray(input);

        for (int i = 0; i < input.length; i++) {
            int newPos = (i + shift) % input.length;

            if (newPos < 0) {
                newPos += input.length;
            }
            newArray[newPos] = input[i];
        }
        return newArray;
    }

    /**
     * Checks if an array contains any of the specified element.
     *
     * @param input   The input
     * @param element The thing to test against.
     * @param <T>     The thing.
     * @return If the element exists at all.
     */
    public static <T> boolean contains(T[] input, T element) {
        for (T test : input) {
            if (Objects.equals(test, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the inverse of an array.
     * If the input array does not contain an element from the allElements array,
     * then it is added to the output.
     *
     * @param input       The input.
     * @param allElements All possible values.
     * @param <T>         The thing.
     * @return The inverse array.
     */
    public static <T> T[] inverse(T[] input, T[] allElements) {
        List<T> list = new LinkedList<>();
        for (T e : allElements) {
            if (!contains(input, e)) {
                list.add(e);
            }
        }

        return list.toArray(createNewArray(input, list.size()));
    }

    /**
     * Checks if the specified array is empty or contains a null entry.
     *
     * @param input The input.
     * @param <T>   The thing.
     * @return If the array is null or contains null.
     */
    public static <T> boolean isNullOrContainsNull(T @Nullable [] input) {
        if (input == null) return true;
        for (T t : input) {
            if (t != null) continue;
            return true;
        }
        return false;
    }

    /**
     * Convert an int array to a list of Integers.
     *
     * @param arr in.
     * @return out.
     */
    public static List<Integer> toList(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int i : arr) {
            list.add(i);
        }
        return list;
    }

    /**
     * Represents this Enumeration as an Iterable.
     *
     * @param enumeration The Enumeration.
     * @return The Iterable.
     */
    public static <E> Iterable<E> iterable(Enumeration<E> enumeration) {
        return () -> new Iterator<E>() {
            //@formatter:off
            @Override public boolean hasNext() { return enumeration.hasMoreElements(); }
            @Override public E next() { return enumeration.nextElement(); }
            //@formatter:on
        };
    }

    /**
     * Shorthand for converting a stream to an Iterable.
     * <p>
     * It is important to note, that a Stream can only be consumed once,
     * and thus, the returned Iterable can only be consumed once.
     *
     * @param stream The Stream.
     * @return The Iterable.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "0.5.0")
    public static <E> Iterable<E> iterable(Stream<E> stream) {
        return stream::iterator;
    }

    /**
     * Shorthand util for streaming an Iterable.
     *
     * @param iter The iterable.
     * @return The Stream.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "0.5.0")
    public static <E> Stream<E> stream(Iterable<E> iter) {
        return StreamSupport.stream(iter.spliterator(), false);
    }

    /**
     * Shorthand util for streaming an Iterable in parallel.
     *
     * @param iter The iterable.
     * @return The Parallel Stream.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "0.5.0")
    public static <E> Stream<E> parallelStream(Iterable<E> iter) {
        return StreamSupport.stream(iter.spliterator(), true);
    }

    @Deprecated
    @ScheduledForRemoval(inVersion = "0.5.0")
    @Nullable
    public static <T> T onlyOrDefault(Stream<T> stream) {
        return onlyOrDefault(stream, null);
    }

    /**
     * Returns the first element found in the Stream if it is the only element in the stream,
     * otherwise the default value is returned.
     *
     * @param stream   The stream.
     * @param _default The default value, in the event the stream is empty, or has more than one element.
     * @return The first element or the default.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "0.5.0")
    @Nullable
    @Contract("_,!null -> !null")
    public static <T> T onlyOrDefault(Stream<T> stream, @Nullable T _default) {
        return onlyOrDefault(iterable(stream), _default);
    }

    /**
     * Returns the first element found in the {@link Iterable} if it's the first and only element in the iterable,
     * otherwise {@code null} is returned.
     *
     * @param iterable The {@link Iterable}.
     * @return The first and only element, or {@code null}
     */
    @Nullable
    public static <T> T onlyOrDefault(Iterable<T> iterable) {
        return onlyOrDefault(iterable, null);
    }

    /**
     * Returns the first element found in the {@link Iterable} if it's the first and only element in the iterable,
     * otherwise the default value is returned.
     *
     * @param iterable The {@link Iterable}.
     * @param _default The default value, in the event the iterable is empty, or has more than one element.
     * @return The first element or the default.
     */
    @Nullable
    @Contract("_,!null -> !null")
    public static <T> T onlyOrDefault(Iterable<T> iterable, @Nullable T _default) {
        final class Cons implements Consumer<T> {

            @Nullable
            T thing = _default;
            boolean found = false;

            @Override
            public void accept(T t) {
                if (found) {
                    thing = _default;
                    throw new ForEachAbort();
                }
                found = true;
                thing = t;
            }
        }
        Cons cons = new Cons();
        try {
            iterable.forEach(cons);
        } catch (ForEachAbort ignored) {
        }
        return cons.thing;
    }

    /**
     * Assert the stream contains a single element, and return it.
     *
     * @param stream The stream.
     * @return The element.
     * @throws IllegalArgumentException If the {@link Stream} contains no or more than one element.
     */
    @Deprecated
    @ScheduledForRemoval(inVersion = "0.5.0")
    public static <T> T only(Stream<T> stream) {
        return only(iterable(stream));
    }

    /**
     * Assert the {@link Iterable} contains a single element, and return it.
     *
     * @param col The iterable.
     * @return The element.
     * @throws IllegalArgumentException If the {@link Iterable} contains no or more than one element.
     */
    public static <T> T only(Iterable<T> col) {
        final class Cons implements Consumer<T> {

            @Nullable
            T thing = null;
            boolean found = false;

            @Override
            public void accept(T t) {
                if (found) {
                    throw new IllegalArgumentException("More than one element.");
                }
                found = true;
                thing = t;
            }
        }
        Cons cons = new Cons();
        col.forEach(cons);
        if (!cons.found) {
            throw new IllegalArgumentException("Not found.");
        }
        return cons.thing;
    }

    /**
     * Return an iterator for an array.
     *
     * @param arr The array.
     * @return The {@link Iterator}.
     */
    public static <T> Iterator<T> iterator(T[] arr) {
        return iterator(arr, 0, arr.length);
    }

    /**
     * Return an iterator for an array slice.
     *
     * @param arr   The array to iterate.
     * @param from  The offset in the array.
     * @param until The end offset in the array.
     * @return The {@link Iterator}.
     */
    public static <T> Iterator<T> iterator(T[] arr, int from, int until) {
        if (until > arr.length) throw new IndexOutOfBoundsException();
        return new Iterator<T>() {
            int i = from;

            @Override
            public boolean hasNext() {
                return i < until;
            }

            @Override
            public T next() {
                return arr[i++];
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                for (; i < until; i++) {
                    action.accept(arr[i]);
                }
            }
        };
    }

    /**
     * Reverse the provided array.
     *
     * @param array The array to reverse.
     */
    public static <T> void reverse(T[] array) {
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}
