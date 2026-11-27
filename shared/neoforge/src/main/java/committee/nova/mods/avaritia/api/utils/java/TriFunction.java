package committee.nova.mods.avaritia.api.utils.java;

import java.util.Objects;
import java.util.function.Function;

/**
 * TriFunction
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/1 19:58
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    R apply(T var1, U var2, V var3);

    default <W> org.apache.commons.lang3.function.TriFunction<T, U, V, W> andThen(Function<? super R, ? extends W> after) {
        Objects.requireNonNull(after);
        return (t, u, v) -> {
            return after.apply(this.apply(t, u, v));
        };
    }
}
