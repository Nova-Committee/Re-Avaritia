package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;

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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 注册模组中的所有伤害类型。
 */
public final class ModDamageTypes {
    private static final int DEATH_MESSAGE_VARIANTS = 5;

    public static final ResourceKey<DamageType> INFINITY = ResourceKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(Const.MOD_ID, "infinity"));

    private ModDamageTypes() {
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(INFINITY, new DamageType("infinity", DamageScaling.ALWAYS, 0.1f));
    }

    public static DamageSource source(Entity attacker) {
        return source(attacker.level(), attacker, attacker);
    }

    public static DamageSource source(Level level, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSourceRandomMessages(holder(level), directEntity, causingEntity);
    }

    /**
     * 旧名称保留给既有调用；新代码请使用语义更明确的 {@link #source(Entity)}。
     */
    @Deprecated
    public static DamageSource causeRandomDamage(Entity attacker) {
        return source(attacker);
    }

    private static Holder<DamageType> holder(Level level) {
        return level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(INFINITY);
    }

    public static class DamageSourceRandomMessages extends DamageSource {
        public DamageSourceRandomMessages(Holder<DamageType> damageTypeHolder, @Nullable Entity entity) {
            this(damageTypeHolder, entity, entity);
        }

        public DamageSourceRandomMessages(Holder<DamageType> damageTypeHolder, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
            super(damageTypeHolder, directEntity, causingEntity);
        }

        @Override
        public @NotNull Component getLocalizedDeathMessage(LivingEntity attacked) {
            int type = attacked.getRandom().nextInt(DEATH_MESSAGE_VARIANTS);
            LivingEntity livingentity = attacked.getKillCredit();
            String s = "death.attack." + this.getMsgId() + "." + type;
            String s1 = "death.attack." + this.getMsgId() + ".player." + type;
            return livingentity != null
                    ? Component.translatable(s1, attacked.getDisplayName(), livingentity.getDisplayName())
                    : Component.translatable(s, attacked.getDisplayName());
        }
    }
}
