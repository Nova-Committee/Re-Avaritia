package committee.nova.mods.avaritia.util.registry;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * FabricRegistryHolder
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/23 上午11:53
 */
public class FabricRegistryHolder<T> implements RegistryHolder<T> {
    private final Registry<T> wrapped;
    private final String modid;

    public FabricRegistryHolder(Registry<T> registry, String modid) {
        this.wrapped = registry;
        this.modid = modid;
    }

    @Override
    public <I extends T> Supplier<I> register(String name, Supplier<I> supplier) {
        return Suppliers.ofInstance(Registry.register(wrapped, new ResourceLocation(modid, name), supplier.get()));
    }

    @Override
    public Collection<Supplier<T>> listAll() {
        return this.wrapped.stream().map(Suppliers::ofInstance).collect(Collectors.toSet());
    }

}
