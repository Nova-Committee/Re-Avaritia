package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.common.level.LevelDamage;
import committee.nova.mods.avaritia.init.registry.ModDamageSources;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/2/12 14:32
 * @Description:
 */

public class DamageUtil {
    public static double scaleDamageWithLevel(int level, int maxLevel, double minDamage, double maxDamage) {
        return minDamage + level / (double) maxLevel * (maxDamage - minDamage);
    }

    public static @NotNull Optional<DamageSource> getDamageSource(@NotNull Level world, @NotNull Function<ModDamageSources, DamageSource> sourceFunc) {
        return LevelDamage.getOpt(world).map(sourceFunc);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean hurtModded(@NotNull Entity entity, @NotNull Function<ModDamageSources, DamageSource> sourceFunc, float amount) {
        return getDamageSource(entity.level(), sourceFunc).map(source -> entity.hurt(source, amount)).orElse(false);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean infinity(@NotNull Entity entity, float amount) {
        return entity.hurt(ModDamageSources.infinity(entity), amount);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean hurtVanilla(@NotNull Entity entity, @NotNull Function<DamageSources, DamageSource> sourceFunc, float amount) {
        DamageSource source = sourceFunc.apply(entity.level().damageSources());
        return entity.hurt(source, amount);
    }

    public static boolean kill(@NotNull Entity entity, int damage) {
        return hurtVanilla(entity, DamageSources::generic, damage);
    }
}
