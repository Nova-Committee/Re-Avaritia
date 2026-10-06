package committee.nova.mods.avaritia.api.init.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Ordered project registrations backed directly by a vanilla registry. */
public final class RegistryEntries<T> {
    private final Registry<T> registry;
    private final String namespace;
    private final Map<ResourceLocation, RegistryEntry<? extends T>> entries = new LinkedHashMap<>();
    private boolean registered;

    private RegistryEntries(Registry<T> registry, String namespace) {
        this.registry = registry;
        this.namespace = namespace;
    }

    public static <T> RegistryEntries<T> create(Registry<T> registry, String namespace) {
        return new RegistryEntries<>(registry, namespace);
    }

    public <V extends T> RegistryEntry<V> register(String path, Supplier<? extends V> factory) {
        if (registered) throw new IllegalStateException("Registry already initialized: " + registry.key());
        ResourceLocation id = new ResourceLocation(namespace, path);
        RegistryEntry<V> entry = new RegistryEntry<>(id, factory);
        if (entries.putIfAbsent(id, entry) != null) throw new IllegalArgumentException("Duplicate registration: " + id);
        return entry;
    }

    public Collection<RegistryEntry<? extends T>> getEntries() {
        return Collections.unmodifiableCollection(entries.values());
    }

    public void registerAll() {
        if (registered) return;
        for (RegistryEntry<? extends T> entry : entries.values()) {
            Registry.register(registry, entry.getId(), entry.get());
        }
        registered = true;
    }
}
