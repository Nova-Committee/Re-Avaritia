package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.Const;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the terminal damage contract shared by Infinity Sword attacks.
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class InfinityDamageUtils {
    private static final ThreadLocal<DeathAttempt> DEATH_ATTEMPT = new ThreadLocal<>();

    private static final class DeathAttempt {
        private final LivingEntity victim;
        private final DeathAttempt parent;
        private boolean notified;

        private DeathAttempt(LivingEntity victim, DeathAttempt parent) {
            this.victim = victim;
            this.parent = parent;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void observeDeath(LivingDeathEvent event) {
        for (DeathAttempt attempt = DEATH_ATTEMPT.get(); attempt != null; attempt = attempt.parent) {
            if (attempt.victim == event.getEntity()) {
                attempt.notified = true;
            }
        }
    }

    private InfinityDamageUtils() {
    }

    /**
     * Resolves multipart hit boxes, such as an Ender Dragon wing, to their living parent.
     */
    public static @Nullable LivingEntity resolveLivingTarget(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (entity instanceof PartEntity<?> part && part.getParent() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }

    /**
     * Applies infinity damage first so normal hooks, attribution and boss behavior run, then enforces
     * the terminal state when a target survives or cancels the regular death pipeline.
     */
    public static boolean forceKill(ServerLevel level, LivingEntity victim, DamageSource source) {
        if (victim.isRemoved() || victim.dead) {
            return false;
        }

        victim.invulnerableTime = 0;
        if (victim instanceof WitherBoss wither) {
            wither.setInvulnerableTicks(0);
        }

        DeathAttempt previous = DEATH_ATTEMPT.get();
        DeathAttempt attempt = new DeathAttempt(victim, previous);
        DEATH_ATTEMPT.set(attempt);
        try {
            if (victim instanceof EnderDragon dragon) {
                dragon.hurt(level, dragon.head, source, Float.MAX_VALUE);
            } else {
                victim.hurtServer(level, source, Float.MAX_VALUE);
            }

            if (!victim.isRemoved() && !victim.dead) {
                if (source.getEntity() instanceof Player player) {
                    victim.setLastHurtByPlayer(player, 100);
                }
                victim.getCombatTracker().recordDamage(source, victim.getHealth());
                victim.setHealth(0.0F);
                // A canceled native death has already notified observers. Do not fire it twice.
                if (!attempt.notified) {
                    CommonHooks.onLivingDeath(victim, source);
                }
                victim.setHealth(0.0F);
                forceDie(victim, source);
            }
            return victim.isRemoved() || victim.dead || victim.isDeadOrDying();
        } finally {
            if (previous == null) {
                DEATH_ATTEMPT.remove();
            } else {
                DEATH_ATTEMPT.set(previous);
            }
        }
    }

    private static void forceDie(LivingEntity victim, DamageSource source) {
        if (victim.isRemoved() || victim.dead) {
            return;
        }

        Entity attacker = source.getEntity();
        LivingEntity killCredit = victim.getKillCredit();
        if (killCredit != null) {
            killCredit.awardKillScore(victim, source);
        }

        if (victim.isSleeping()) {
            victim.stopSleeping();
        }

        if (!victim.level().isClientSide() && victim.hasCustomName()) {
            Const.LOGGER.info("Named entity {} died: {}", victim, victim.getCombatTracker().getDeathMessage().getString());
        }

        victim.dead = true;
        victim.getCombatTracker().recheckStatus();
        if (victim.level() instanceof ServerLevel serverLevel) {
            if (attacker == null || attacker.killedEntity(serverLevel, victim, source)) {
                victim.gameEvent(GameEvent.ENTITY_DIE);
                victim.dropAllDeathLoot(serverLevel, source);
                createWitherRose(victim, killCredit);
            }
            victim.level().broadcastEntityEvent(victim, (byte) 3);
        }

        victim.setPose(Pose.DYING);
    }

    private static void createWitherRose(LivingEntity victim, @Nullable LivingEntity killCredit) {
        if (victim.level().isClientSide() || !(killCredit instanceof WitherBoss)) {
            return;
        }

        BlockPos pos = victim.blockPosition();
        BlockState rose = Blocks.WITHER_ROSE.defaultBlockState();
        if (victim.level().isEmptyBlock(pos) && rose.canSurvive(victim.level(), pos)) {
            victim.level().setBlock(pos, rose, 3);
            return;
        }

        victim.level().addFreshEntity(new ItemEntity(
                victim.level(), victim.getX(), victim.getY(), victim.getZ(), new ItemStack(Items.WITHER_ROSE)));
    }
}
