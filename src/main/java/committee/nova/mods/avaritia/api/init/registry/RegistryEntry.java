package committee.nova.mods.avaritia.api.init.registry;

import net.minecraft.resources.ResourceLocation;
import java.util.Objects;
import java.util.function.Supplier;

/** A named registration whose factory is evaluated once, on first use. */
public final class RegistryEntry<T> implements Supplier<T> {
    private final ResourceLocation id;
    private Supplier<? extends T> factory;
    private T value;
    private boolean creating;

    RegistryEntry(ResourceLocation id, Supplier<? extends T> factory) {
        this.id = Objects.requireNonNull(id);
        this.factory = Objects.requireNonNull(factory);
    }

    public ResourceLocation getId() { return id; }

    @Override
    public T get() {
        if (value == null) {
            if (creating) throw new IllegalStateException("Circular registration: " + id);
            creating = true;
            try {
                value = Objects.requireNonNull(factory.get(), "Null registration: " + id);
                factory = null;
            } finally {
                creating = false;
            }
        }
        return value;
    }
}
