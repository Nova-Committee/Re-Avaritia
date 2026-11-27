package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/10/20 23:45
 * @Description:
 */
public class ModEnchants {
    public static final DeferredRegister<Enchantment> ENCHANTMENT = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Const.MOD_ID);

    //public static RegistryObject<Enchantment> smelt = enchant("smelt", SmeltEnchant::new);

    public static RegistryObject<Enchantment> enchant(String name, Supplier<Enchantment> enchantment) {
        return ENCHANTMENT.register(name, enchantment);
    }
}
