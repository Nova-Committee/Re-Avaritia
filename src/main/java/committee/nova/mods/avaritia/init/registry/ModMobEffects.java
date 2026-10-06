package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.effects.BurningEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;

/**
 * @author cnlimiter
 */
public class ModMobEffects {
    public static final RegistryEntries<MobEffect> MOB_EFFECTS = RegistryEntries.create(BuiltInRegistries.MOB_EFFECT, Const.MOD_ID);
    public static final RegistryEntry<MobEffect> BURNING = MOB_EFFECTS.register("burning", () -> new BurningEffect(MobEffectCategory.HARMFUL, 0xd0f9ff));

}
