package committee.nova.mods.avaritia.common.entity.ball;

import committee.nova.mods.avaritia.client.particle.ShockwaveParticleOptions;
import committee.nova.mods.avaritia.init.registry.ModMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/**
 * @author cnlimiter
 */
public class BurningBallEntity extends ThrowableProjectile {
    public BurningBallEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        this.discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity owner = this.getOwner();
            BlockPos pos = result.getBlockPos();
            if (owner instanceof ServerPlayer player) {
                var entities = level().getEntitiesOfClass(LivingEntity.class, new AABB(pos.offset(-10, -10, -10).getCenter(), pos.offset(10, 10, 10).getCenter()),
                        livingEntity -> !livingEntity.isSpectator() && livingEntity.isAlive() && !livingEntity.equals(player));
                entities.forEach(entity -> entity.addEffect(new MobEffectInstance((Holder<MobEffect>) ModMobEffects.BURNING, 600, 3)));
                spawnParticles(serverLevel, new ShockwaveParticleOptions(new Vector3f(1F, 0f, 0f), 10F, true, "minecraft:flame"), getX(), getY(), getZ(), 1, 0, 0, 0, 0,true);
            }
        }

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity owner = this.getOwner();
            var pos = result.getEntity().blockPosition();
            if (owner instanceof ServerPlayer player) {
                var entities = level().getEntitiesOfClass(LivingEntity.class, new AABB(pos.offset(-10, -10, -10).getCenter(), pos.offset(10, 10, 10).getCenter()),
                        livingEntity -> !livingEntity.isSpectator() && livingEntity.isAlive() && !livingEntity.equals(player));
                entities.forEach(entity -> entity.addEffect(new MobEffectInstance((Holder<MobEffect>) ModMobEffects.BURNING, 600, 3)));
                spawnParticles(serverLevel, new ShockwaveParticleOptions(new Vector3f(1F, 0f, 0f), 10F, true, "minecraft:flame"), getX(), getY(), getZ(), 1, 0, 0, 0, 0,true);
            }
        }
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, false, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }

    @Override
    public boolean ignoreExplosion(@NotNull Explosion explosion) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }
}
