package committee.nova.mods.avaritia.api.init.registry;

import net.minecraft.resources.Identifier;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** A named registration whose factory is evaluated once, on first use, with the registration id. */
public final class RegistryEntry<T> implements Supplier<T> {
    private final Identifier id;
    private Function<Identifier, ? extends T> factory;
    private T value;
    private boolean creating;

    RegistryEntry(Identifier id, Function<Identifier, ? extends T> factory) {
        this.id = Objects.requireNonNull(id);
        this.factory = Objects.requireNonNull(factory);
    }

    public Identifier getId() { return id; }

    @Override
    public T get() {
        if (value == null) {
            if (creating) throw new IllegalStateException("Circular registration: " + id);
            creating = true;
            try {
                value = Objects.requireNonNull(factory.apply(id), "Null registration: " + id);
                factory = null;
            } finally {
                creating = false;
            }
        }
        return value;
    }
}
