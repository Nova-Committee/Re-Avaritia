package committee.nova.mods.avaritia.init.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/2/12 14:28
 * @Description:
 */

public class ModDamageSources {
    private final Registry<DamageType> damageTypes;
    private final DamageSource infinity;

    public ModDamageSources(RegistryAccess access) {
        this.damageTypes = access.registryOrThrow(Registries.DAMAGE_TYPE);
        this.infinity = init(ModDamageTypes.INFINITY);
    }

    private DamageSource init(ResourceKey<DamageType> key) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(key));
    }

    public static DamageSource infinity(Entity entity) {
        Registry<DamageType> damageTypes = entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);

        return new DamageSource(damageTypes.getHolderOrThrow(ModDamageTypes.INFINITY)){
            @Override
            public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity attacked) {
                int type = attacked.getRandom().nextInt(3);
                LivingEntity livingentity = attacked.getKillCredit();
                String s = "death.attack." + this.getMsgId() + "." + type;
                String s1 = s + ".player";
                return livingentity != null ? Component.translatable(s1, attacked.getDisplayName(), livingentity.getDisplayName()) : Component.translatable(s, attacked.getDisplayName());

            }
        };
    }

    public DamageSource infinity() {
        return this.infinity;
    }


}
