package committee.nova.mods.avaritia.api.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.function.*;

/**
 * SneakyUtils
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/7 上午2:36
 */
public class SneakyUtils {
    private static final Runnable NULL_RUNNABLE = () -> {
    };
    private static final Callable<Object> NULL_CALLABLE = () -> null;
    private static final Supplier<Object> NULL_SUPPLIER = () -> null;
    private static final Consumer<Object> NULL_CONSUMER = e -> {
    };
    private static final Predicate<Object> TRUE = e -> true;
    private static final Predicate<Object> FALSE = e -> true;
    private static final BinaryOperator<Object> FIRST = (a, b) -> a;
    private static final BinaryOperator<Object> LAST = (a, b) -> b;
    private static final Supplier<NotPossibleException> NOT_POSSIBLE = () -> NotPossibleException.INSTANCE;

    /**
     * Returns a Runnable that does nothing.
     *
     * @return The runnable.
     */
    public static Runnable none() {
        return NULL_RUNNABLE;
    }

    /**
     * Returns a Callable that always returns null when executed.
     *
     * @return The callable.
     */
    public static <T> Callable<T> nullC() {
        return unsafeCast(NULL_CALLABLE);
    }

    /**
     * Returns a Supplier that always returns null when executed.
     *
     * @return The callable.
     */
    public static <T> Supplier<T> nullS() {
        return unsafeCast(NULL_SUPPLIER);
    }

    /**
     * Returns a Consumer that does nothing when executed.
     *
     * @return The consumer.
     */
    public static <T> Consumer<T> nullCons() {
        return unsafeCast(NULL_CONSUMER);
    }

    /**
     * Returns a Predicate that always passes.
     *
     * @return The Predicate.
     */
    public static <T> Predicate<T> trueP() {
        return unsafeCast(TRUE);
    }

    /**
     * Returns a Predicate that always fails.
     *
     * @return The Predicate.
     */
    public static <T> Predicate<T> falseP() {
        return unsafeCast(FALSE);
    }

    /**
     * A BinaryOperator which always resolves to the left-hand element.
     *
     * @return The BinaryOperator.
     */
    public static <T> BinaryOperator<T> first() {
        return unsafeCast(FIRST);
    }

    /**
     * A BinaryOperator which always resolves to the right-hand element.
     *
     * @return The BinaryOperator.
     */
    public static <T> BinaryOperator<T> last() {
        return unsafeCast(LAST);
    }

    /**
     * Concatenates two {@link Runnable}s.
     *
     * @param a The First {@link Runnable} to execute.
     * @param b The Second {@link Runnable} to execute.
     * @return The Concatenated {@link Runnable}.
     */
    public static Runnable concat(Runnable a, Runnable b) {
        return () -> {
            a.run();
            b.run();
        };
    }

    /**
     * Returns a Supplier for a NotPossibleException.
     * <p>
     * Useful for Optional instance assertions.
     *
     * @return The Supplier.
     */
    public static Supplier<NotPossibleException> notPossible() {
        return NOT_POSSIBLE;
    }

    /**
     * Executes the given ThrowingRunnable, rethrowing any exceptions
     * produced by the runnable as Unchecked(as seen by the compiler.)
     *
     * @param tr The ThrowingRunnable.
     */
    public static void sneaky(ThrowingRunnable<Throwable> tr) {
        try {
            tr.run();
        } catch (Throwable ex) {
            throwUnchecked(ex);
        }
    }

    /**
     * Wraps a ThrowingRunnable to a {@link Runnable}.
     * See {@link #sneaky(ThrowingRunnable)} for info.
     *
     * @param tr The ThrowingRunnable to wrap.
     * @return The wrapped runnable.
     */
    public static Runnable sneak(ThrowingRunnable<Throwable> tr) {
        return () -> sneaky(tr);
    }

    /**
     * Wraps a ThrowingConsumer to a {@link Consumer}
     * Rethrowing any exceptions produced by the ThrowingConsumer
     * as unchecked(as seen by the compiler.)
     *
     * @param cons The ThrowingConsumer to wrap.
     * @return The wrapped Consumer.
     */
    public static <T> Consumer<T> sneak(ThrowingConsumer<T, Throwable> cons) {
        return e -> {
            try {
                cons.accept(e);
            } catch (Throwable ex) {
                throwUnchecked(ex);
            }
        };
    }

    /**
     * Executes the given ThrowingRunnable, rethrowing any exceptions
     * produced by the runnable as Unchecked(as seen by the compiler.)
     *
     * @param sup The ThrowingSupplier.
     * @return The return result of the ThrowingSupplier.
     */
    public static <T> T sneaky(ThrowingSupplier<T, Throwable> sup) {
        try {
            return sup.get();
        } catch (Throwable ex) {
            throwUnchecked(ex);
            return null;//Impossible, go away compiler!
        }
    }

    /**
     * Wraps a ThrowingSupplier to a {@link Supplier}
     * Rethrowing any exceptions produced by the ThrowingSupplier
     * as unchecked(as seen by the compiler.)
     *
     * @param sup The ThrowingSupplier to wrap.
     * @return The wrapped Supplier.
     */
    public static <T> Supplier<T> sneak(ThrowingSupplier<T, Throwable> sup) {
        return () -> sneaky(sup);
    }

    /**
     * Wraps a ThrowingFunction to a {@link Function}
     * Rethrowing any exceptions produced by the ThrowingFunction
     * as unchecked(as seen by the compiler.)
     *
     * @param tf The ThrowingFunction to wrap.
     * @return The wrapped Function.
     */
    public static <T, R> Function<T, R> sneak(ThrowingFunction<T, R, Throwable> tf) {
        return e -> {
            try {
                return tf.apply(e);
            } catch (Throwable ex) {
                throwUnchecked(ex);
                return null;//Impossible, go away compiler!
            }
        };
    }

    @Nullable
    @Contract("null->null;!null->!null")
    @SuppressWarnings("unchecked")
    public static <T> T unsafeCast(@Nullable Object object) {
        return (T) object;
    }

    /**
     * Throws an exception without compiler warnings.
     */
    public static <T extends Throwable> void throwUnchecked(Throwable t) throws T {
        throw SneakyUtils.<T>unsafeCast(t);
    }

    public interface ThrowingRunnable<E extends Throwable> {

        void run() throws E;
    }

    public interface ThrowingConsumer<T, E extends Throwable> {

        void accept(T thing) throws E;
    }

    public interface ThrowingSupplier<T, E extends Throwable> {

        T get() throws E;
    }

    public interface ThrowingFunction<T, R, E extends Throwable> {

        R apply(T thing) throws E;
    }

    public static class NotPossibleException extends RuntimeException {

        public static NotPossibleException INSTANCE = new NotPossibleException();

        private NotPossibleException() {
        }

        public NotPossibleException(String message) {
            super(message);
        }
    }
}
