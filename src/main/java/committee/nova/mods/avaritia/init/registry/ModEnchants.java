package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.world.item.enchantment.Enchantment;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/10/20 23:45
 * @Description:
 */
public class ModEnchants {
    public static final RegistryEntries<Enchantment> ENCHANTMENT = RegistryEntries.create(BuiltInRegistries.ENCHANTMENT, Const.MOD_ID);


    public static RegistryEntry<Enchantment> enchant(String name, Supplier<Enchantment> enchantment) {
        return ENCHANTMENT.register(name, enchantment);
    }
}
