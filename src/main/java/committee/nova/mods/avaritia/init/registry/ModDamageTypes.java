package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Author cnlimiter
 * CreateTime 2023/6/15 0:39
 * Name ModDamageTypes
 * Description
 */

public class ModDamageTypes {

    public static ResourceKey<DamageType> INFINITY = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Const.MOD_ID, "infinity"));

    // 注册
    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(INFINITY, new DamageType("infinity", DamageScaling.ALWAYS, 0.1F));
    }

    private static Holder<DamageType> holder(Level level) {
        return level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(INFINITY);
    }

    public static DamageSource causeRandomDamage(Level level, @Nullable Entity itemEntity, @Nullable Entity owner) {
        return new DamageSourceRandomMessages(holder(level), itemEntity, owner);
    }

    public static DamageSource causeRandomDamage(Level level, @Nullable Entity owner) {
        return new DamageSourceRandomMessages(holder(level), owner, owner);
    }

    public static class DamageSourceRandomMessages extends DamageSource {
        public DamageSourceRandomMessages(Holder<DamageType> damageTypeHolder, @Nullable Entity itemEntity, @Nullable Entity owner) {
            super(damageTypeHolder, itemEntity, owner);
        }

        @Override
        public @NotNull Component getLocalizedDeathMessage(LivingEntity attacked) {
            int type = attacked.getRandom().nextInt(3);
            LivingEntity livingentity = attacked.getKillCredit();
            String s = "death.attack." + this.getMsgId() + "." + type;
            String s1 = "death.attack." + this.getMsgId() + ".player." + type;
            return livingentity != null ? Component.translatable(s1, attacked.getDisplayName(), livingentity.getDisplayName()) : Component.translatable(s, attacked.getDisplayName());
        }
    }
}
