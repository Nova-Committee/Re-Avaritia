package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Author cnlimiter
 * CreateTime 2023/6/15 0:39
 * Name ModDamageTypes
 * Description
 */

public class ModDamageTypes {

    public static ResourceKey<DamageType> INFINITY = ResourceKey.create(Registries.DAMAGE_TYPE, Static.rl("infinity"));

    public static final RegistrySetBuilder DAMAGE_BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);

    public static HolderLookup.Provider append(HolderLookup.Provider original) {
        return DAMAGE_BUILDER.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), original);
    }

    // 注册
    public static void bootstrap(RegistrySetBuilder.RegistryBootstrap<DamageType> context) {
        context.register(INFINITY, new DamageType("infinity", DamageScaling.ALWAYS, 0.1F));
    }

    public static DamageSource causeRandomDamage(Entity attacker) {
        return new DamageSourceRandomMessages(attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(INFINITY), attacker);
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
            return livingentity != null ? Component.translatable(s1, attacked.getDisplayName(), livingentity.getDisplayName()) : Component.translatable(s, attacked.getDisplayName());
        }
    }
}
