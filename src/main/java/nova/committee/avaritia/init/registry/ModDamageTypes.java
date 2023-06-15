package nova.committee.avaritia.init.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Author cnlimiter
 * CreateTime 2023/6/15 0:39
 * Name ModDamageTypes
 * Description
 */

public class ModDamageTypes {
    static ResourceKey<DamageType> INFINITY = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("infinity"));


    public static DamageSource source(Level level, ResourceKey<DamageType> id) {
        final Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        final Holder.Reference<DamageType> damage = registry.getHolderOrThrow(id);
        return new DamageSource(damage);
    }

    public static DamageSource causeRandomDamage(LivingEntity attacker) {
        return new DamageSourceRandomMessages(attacker.getCommandSenderWorld().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(INFINITY), attacker);
    }

    public static DamageSource causeDamage(LivingEntity attacker) {
        return new DamageSource(attacker.getCommandSenderWorld().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(INFINITY), attacker);
    }

    public static class DamageSourceRandomMessages extends DamageSource {
        public DamageSourceRandomMessages(Holder<DamageType> damageTypeHolder, @Nullable Entity entity1, @Nullable Entity entity2, @Nullable Vec3 from) {
            super(damageTypeHolder, entity1, entity2, from);
        }

        public DamageSourceRandomMessages(Holder<DamageType> damageTypeHolder, @Nullable Entity entity1, @Nullable Entity entity2) {
            super(damageTypeHolder, entity1, entity2);
        }

        public DamageSourceRandomMessages(Holder<DamageType> damageTypeHolder, Vec3 from) {
            super(damageTypeHolder, from);
        }

        public DamageSourceRandomMessages(Holder<DamageType> damageTypeHolder, @Nullable Entity entity) {
            super(damageTypeHolder, entity);
        }

        public DamageSourceRandomMessages(Holder<DamageType> p_270475_) {
            super(p_270475_);
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity attacked) {
            int type = attacked.getRandom().nextInt(3);
            LivingEntity livingentity = attacked.getKillCredit();
            String s = "death.attack." + this.getMsgId() + "." + type;
            String s1 = s + ".player";
            return livingentity != null ? Component.translatable(s1, attacked.getDisplayName(), livingentity.getDisplayName()) : Component.translatable(s, attacked.getDisplayName());
        }
    }
}
