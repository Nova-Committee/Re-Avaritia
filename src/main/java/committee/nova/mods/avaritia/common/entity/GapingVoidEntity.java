package committee.nova.mods.avaritia.common.entity;

import com.google.common.base.Predicate;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import committee.nova.mods.avaritia.init.registry.ModSounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 8:34
 * Version: 1.0
 */
public class GapingVoidEntity extends Entity {

    private static final GameProfile AVARITIA_FAKE_PLAYER = new GameProfile(UUID.fromString("32283731-bbef-487c-bb69-c7e32f84ed27"), "[Avaritia]");
    public static final EntityDataAccessor<Integer> AGE_PARAMETER = SynchedEntityData.defineId(GapingVoidEntity.class, EntityDataSerializers.INT);
    public static final int maxLifetime = 186;
    public static final Predicate<Entity> SUCK_PREDICATE = input -> {
        if (input instanceof Player p) {
            return !p.isCreative() || !p.isFallFlying();
        }

        return true;
    };
    public static final Predicate<Entity> COMNOM_PREDICATE = input -> {
        if (!(input instanceof LivingEntity)) {
            return false;
        }

        if (input instanceof Player p) {
            return !p.isCreative();
        } else return !(input instanceof ImmortalItemEntity);
    };
    public static double collapse = .95;
    public static double suckRange = 20.0;
    private FakePlayer fakePlayer;

    private LivingEntity user;

    public GapingVoidEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        noPhysics = true;
        if (level() instanceof ServerLevel) {
            fakePlayer = FakePlayerFactory.get((ServerLevel) level(), AVARITIA_FAKE_PLAYER);
        }
    }

    public GapingVoidEntity(Level level) {
        this(ModEntityTypes.GAPING_VOID.get(), level);
    }

    public GapingVoidEntity(Level level, LivingEntity shooter) {
        this(ModEntityTypes.GAPING_VOID.get(), level);
        this.setUser(shooter);
    }

    public static double getVoidScale(double age) {
        double life = age / (double) maxLifetime;

        double curve;
        if (life < collapse) {
            curve = 0.005 + ease(1 - ((collapse - life) / collapse)) * 0.995;
        } else {
            curve = ease(1 - ((life - collapse) / (1 - collapse)));
        }
        return 10.0 * curve;
    }

    private static double ease(double in) {
        double t = in - 1;
        return Math.sqrt(1 - t * t);
    }

    public void setUser(LivingEntity user) {
        this.user = user;
    }

    public int getAge() {
        return this.entityData.get(AGE_PARAMETER);
    }

    private void setAge(int age) {
        this.entityData.set(AGE_PARAMETER, age);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(AGE_PARAMETER, 0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setAge(input.getIntOr("age", 0));
        if (level() instanceof ServerLevel) {
            fakePlayer = FakePlayerFactory.get((ServerLevel) level(), AVARITIA_FAKE_PLAYER);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("age", getAge());

    }

    @Override
    public void tick() {
        double posX = this.getX();
        double posY = this.getY();
        double posZ = this.getZ();
        BlockPos position = this.getOnPos();
        int age = getAge();

        if (age >= maxLifetime && !level().isClientSide()) {
            level().explode(this, posX, posY, posZ, 6.0f, Level.ExplosionInteraction.BLOCK);
            int range = 4;
            AABB axisAlignedBB = new AABB(new Vec3(posX - range, posY - range, posZ - range), new Vec3(posX + range, posY + range, posZ + range));
            List<Entity> nommed = level().getEntitiesOfClass(Entity.class, axisAlignedBB, COMNOM_PREDICATE);
            nommed.stream()
                    .filter(entity -> entity != this)
                    .forEach(entity -> {
                        if (entity instanceof EnderDragon dragon && level() instanceof ServerLevel serverLevel) {
                            dragon.hurt(serverLevel, dragon.head, ModDamageTypes.source(user), 1000.0f);
                            dragon.setHealth(0);
                        } else if (entity instanceof WitherBoss wither) {
                            wither.setInvulnerableTicks(0);
                            damageEntity(wither, ModDamageTypes.source(user), 1000.0f);
                        } else {
                            var damageSource = user != null ? ModDamageTypes.source(user) : this.damageSources().fellOutOfWorld();
                            damageEntity(entity, damageSource, 1000.0f);
                        }
                    });
            remove(RemovalReason.KILLED);
        } else {
            if (age == 0) {
                level().playSound(fakePlayer, getX(), getY(), getZ(), ModSounds.GAPING_VOID, SoundSource.HOSTILE, 8.0F, 1.0F);
            }
            setAge(age + 1);
        }

        if (level().isClientSide()) {
            return;
        }
        if (fakePlayer == null) {
            remove(RemovalReason.KILLED);
            return;
        }

        // poot poot

        for (int i = 0; i < 50; i++) {

            level().addParticle(ParticleTypes.PORTAL, position.getX(), position.getY(), position.getZ(), random.nextGaussian() * 3,
                    random.nextGaussian() * 3, random.nextGaussian() * 3);
        }

        // *slurping noises*

        double size = getVoidScale(age) * 0.5 - 0.2;
        int range = (int) (size * suckRange);
        AABB axisAlignedBB = new AABB(new Vec3(posX - range, posY - range, posZ - range), new Vec3(posX + range, posY + range, posZ + range));

        List<Entity> sucked = level().getEntitiesOfClass(Entity.class, axisAlignedBB, SUCK_PREDICATE);

        double radius = getVoidScale(age) * 0.5;

        for (Entity suckee : sucked) {
            if (suckee != this) {
                double dx = posX - suckee.getX();
                double dy = posY - suckee.getY();
                double dz = posZ - suckee.getZ();

                double lensquared = dx * dx + dy * dy + dz * dz;
                double len = Math.sqrt(lensquared);
                double lenn = len / suckRange;

                if (len <= suckRange) {
                    double strength = (1 - lenn) * (1 - lenn);
                    double power = 0.075 * radius;
                    Vec3 motion = suckee.getDeltaMovement();
                    double motionX = motion.x + (dx / len) * strength * power;
                    double motionY = motion.y + (dy / len) * strength * power;
                    double motionZ = motion.z + (dz / len) * strength * power;
                    suckee.setDeltaMovement(motionX, motionY, motionZ);

                }
            }
        }

        // om nom nom
        int nomrange = (int) (radius * 0.95);
        AABB alignedBB = new AABB(new Vec3(posX - nomrange, posY - nomrange, posZ - nomrange), new Vec3(posX + nomrange, posY + nomrange, posZ + nomrange));
        List<Entity> nommed = level().getEntitiesOfClass(Entity.class, alignedBB, COMNOM_PREDICATE);

        for (Entity nommee : nommed) {
            if (nommee != this) {
                Vec3 nomedPos = nommee.getLookAngle();
                Vec3 diff = this.getLookAngle().subtract(nomedPos);

                double len = diff.length();

                if (len <= nomrange) {
                    if (nommee instanceof EnderDragon dragon && level() instanceof ServerLevel serverLevel) {
                        dragon.hurt(serverLevel, dragon.head, this.damageSources().fellOutOfWorld(), 5.0f);
                    }
                    damageEntity(nommee, this.damageSources().fellOutOfWorld(), 5.0f);
                }
            }
        }

        // every half second, SMASH STUFF
        if (age % 10 == 0) {
            Vec3 posFloor = this.position();

            int blockrange = Math.round(nomrange);

            for (int y = -blockrange; y <= blockrange; y++) {
                for (int z = -blockrange; z <= blockrange; z++) {
                    for (int x = -blockrange; x <= blockrange; x++) {
                        Vec3 pos2 = new Vec3(x, y, z);
                        Vec3 rPos = posFloor.add(pos2);
                        BlockPos blockPos = BlockPos.containing(rPos.x, rPos.y, rPos.z);

                        if (blockPos.getY() < 0 || blockPos.getY() > 255) {
                            continue;
                        }

                        double dist = pos2.lengthSqr();
                        if (dist <= nomrange && !level().getBlockState(blockPos).isAir()) {
                            BlockState state = level().getBlockState(blockPos);
                            BreakBlockEvent event = new BreakBlockEvent(level(), blockPos, state, fakePlayer);
                            NeoForge.EVENT_BUS.post(event);
                            if (!event.isCanceled()) {
                                float resist = state.getBlock().getExplosionResistance();
                                if (resist <= 10.0) {
                                    state.getBlock().dropFromExplosion(null);
                                    level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canBeCollidedWith(@NotNull Entity entity) {
        return super.canBeCollidedWith(entity);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_19883_) {
        return true;
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float amount) {
        return false;
    }

    private void damageEntity(Entity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (level() instanceof ServerLevel serverLevel) {
            entity.hurtServer(serverLevel, source, amount);
        } else {
            entity.hurtOrSimulate(source, amount);
        }
    }
}
