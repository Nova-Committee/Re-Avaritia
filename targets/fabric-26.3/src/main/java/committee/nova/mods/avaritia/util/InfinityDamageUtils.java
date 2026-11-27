package committee.nova.mods.avaritia.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the terminal damage contract shared by Infinity Sword attacks.
 */
public final class InfinityDamageUtils {
    private InfinityDamageUtils() {
    }

    /**
     * Resolves multipart hit boxes, such as an Ender Dragon wing, to their living parent.
     */
    public static @Nullable LivingEntity resolveLivingTarget(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (entity instanceof EnderDragonPart part && part.parentMob instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }

    /**
     * Applies infinity damage first so normal hooks, attribution and boss behavior run, then enforces
     * the terminal state when a target survives the regular death pipeline.
     */
    public static boolean forceKill(ServerLevel level, LivingEntity victim, DamageSource source) {
        if (victim.isRemoved() || victim.isDeadOrDying()) {
            return false;
        }

        victim.setInvulnerableTime(0);
        if (victim instanceof WitherBoss wither) {
            wither.setInvulnerableTicks(0);
        }

        if (victim instanceof EnderDragon dragon) {
            dragon.hurt(level, dragon.head, source, Float.MAX_VALUE);
        } else {
            victim.hurtServer(level, source, Float.MAX_VALUE);
        }

        if (!victim.isRemoved() && !victim.isDeadOrDying()) {
            if (source.getEntity() instanceof Player player) {
                victim.setLastHurtByPlayer(player, 100);
            }
            victim.setHealth(0.0F);
            victim.die(source);
        }
        return victim.isRemoved() || victim.isDeadOrDying();
    }
}
