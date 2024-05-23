package committee.nova.mods.avaritia.util.registry;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * RegistryHolder
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/20 下午9:09
 */
public interface RegistryHolder<T> {
    <I extends T> Supplier<I> register(String name, Supplier<I> supplier);

    Collection<Supplier<T>> listAll();
}
