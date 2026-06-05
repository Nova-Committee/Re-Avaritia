package com.avaritia.init.registry;

import com.avaritia.Const;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 注册模组中的所有伤害类型。
 */
public class ModDamageTypes {
    public static final ResourceKey<DamageType> INFINITY = ResourceKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(Const.MOD_ID, "infinity"));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(INFINITY, new DamageType("infinity", DamageScaling.ALWAYS, 0.1f));
    }

    public static DamageSource causeRandomDamage(Entity attacker) {
        return new DamageSourceRandomMessages(attacker.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(INFINITY), attacker);
    }

    public static class DamageSourceRandomMessages extends DamageSource {
        public DamageSourceRandomMessages(Holder<DamageType> damageTypeHolder, @Nullable Entity entity) {
            super(damageTypeHolder, entity);
        }

        @Override
        public @NotNull Component getLocalizedDeathMessage(LivingEntity attacked) {
            int type = attacked.getRandom().nextInt(3);
            LivingEntity livingentity = attacked.getKillCredit();
            String s = "death.attack." + this.getMsgId() + "." + type;
            String s1 = "death.attack." + this.getMsgId() + ".player." + type;
            return livingentity != null
                    ? Component.translatable(s1, attacked.getDisplayName(), livingentity.getDisplayName())
                    : Component.translatable(s, attacked.getDisplayName());
        }
    }
}
