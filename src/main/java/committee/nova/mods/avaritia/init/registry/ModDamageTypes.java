package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Author cnlimiter
 * CreateTime 2023/6/15 0:39
 * Name ModDamageTypes
 * Description
 */

public class ModDamageTypes {

    public static final ResourceKey<DamageType> INFINITY = createKey("infinity_damage");

    public static void createDamageTypes(BootstapContext<DamageType> context) {
        context.register(INFINITY, new DamageType("infinity", DamageScaling.ALWAYS, 0.1F, DamageEffects.HURT, RANDOM_MSG));
    }

    private static ResourceKey<DamageType> createKey(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Static.MOD_ID, name));
    }

    public static final DeathMessageType RANDOM_MSG = DeathMessageType.create("RANDOM_MSG", Static.rl("random_infinity").toString(),
            (entity, lastEntry, sigFall) -> {
                DamageSource dmgSrc = lastEntry.source();
                int type = entity.getRandom().nextInt(3);
                LivingEntity livingentity = entity.getKillCredit();
                String s = "death.attack." + dmgSrc.getMsgId() + "." + type;
                String s1 = "death.attack." + dmgSrc.getMsgId() + ".player." + type;
                return livingentity != null ? Component.translatable(s1, entity.getDisplayName(), livingentity.getDisplayName()) : Component.translatable(s, entity.getDisplayName());
            });

}
