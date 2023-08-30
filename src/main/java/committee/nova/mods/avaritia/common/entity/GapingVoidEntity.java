package committee.nova.mods.avaritia.common.entity;

import com.google.common.base.Predicate;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 8:34
 * Version: 1.0
 */
public class GapingVoidEntity extends Entity {

    public static final EntityDataAccessor<Integer> AGE_PARAMETER = SynchedEntityData.defineId(GapingVoidEntity.class, EntityDataSerializers.INT);
    public static final int maxLifetime = 186;
    public static final Predicate<Entity> SUCK_PREDICATE = input -> {
        if (input instanceof Player p) {
            return !p.isCreative() || !p.isFallFlying();
        }

        return true;
    };
    public static final Predicate<Entity> OMNOM_PREDICATE = input -> {
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
        setSharedFlagOnFire(true);
        noCulling = true;
        if (getCommandSenderWorld() instanceof ServerLevel) {
            fakePlayer = FakePlayerFactory.get((ServerLevel) getCommandSenderWorld(), Static.avaritiaFakePlayer);
        }
    }

    public GapingVoidEntity(Level level) {
        this(ModEntities.GAPING_VOID.get(), level);
    }

    public GapingVoidEntity(Level level, LivingEntity shooter) {
        this(ModEntities.GAPING_VOID.get(), level);
        this.setUser(shooter);
    }

    public void setUser(LivingEntity user) {
        this.user = user;
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

    public int getAge() {
        return this.entityData.get(AGE_PARAMETER);
    }

    private void setAge(int age) {
        this.entityData.set(AGE_PARAMETER, age);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(AGE_PARAMETER, 0);

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setAge(tag.getInt("age"));
        if (getCommandSenderWorld() instanceof ServerLevel) {
            fakePlayer = FakePlayerFactory.get((ServerLevel) getCommandSenderWorld(), Static.avaritiaFakePlayer);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", getAge());

    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new  ClientboundAddEntityPacket(this);
    }

    @Override
    public void tick() {
        //super.tick();

        double posX = this.getX();
        double posY = this.getY();
        double posZ = this.getZ();
        BlockPos position = this.getOnPos();
        int age = getAge();

        if (age >= maxLifetime && !getCommandSenderWorld().isClientSide) {
            getCommandSenderWorld().explode(this, posX, posY, posZ, 6.0f, Explosion.BlockInteraction.NONE);
            int range = 4;
            AABB axisAlignedBB = new AABB(position.offset(-range, -range, -range), position.offset(range, range, range));
            List<Entity> nommed = getCommandSenderWorld().getEntitiesOfClass(Entity.class, axisAlignedBB, OMNOM_PREDICATE);
            nommed.stream()
                    .filter(entity -> entity != this)
                    .forEach(entity -> {
                        if (entity instanceof EnderDragon dragon) {
                            dragon.hurt(dragon.head, new ModDamageTypes(user), 1000.0f);
                            dragon.setHealth(0);
                        } else if (entity instanceof WitherBoss wither) {
                            wither.setInvulnerableTicks(0);
                            wither.hurt(new ModDamageTypes(user), 1000.0f);
                        } else entity.hurt(new ModDamageTypes(user), 1000.0f);
                    });
            remove(RemovalReason.KILLED);
        } else {
            if (age == 0) {
                getCommandSenderWorld().playSound(fakePlayer, getX(), getY(), getZ(), ModSounds.GAPING_VOID, SoundSource.HOSTILE, 8.0F, 1.0F);
            }
            setAge(age + 1);
        }

        if (getCommandSenderWorld().isClientSide) {
            return;
        }
        if (fakePlayer == null) {
            remove(RemovalReason.KILLED);
            return;
        }

        // poot poot

        for (int i = 0; i < 50; i++) {

            getCommandSenderWorld().addParticle(ParticleTypes.PORTAL, position.getX(), position.getY(), position.getZ(), random.nextGaussian() * 3,
                    random.nextGaussian() * 3, random.nextGaussian() * 3);
        }

        // *slurping noises*

        double particlespeed = 4.5;
        double size = getVoidScale(age) * 0.5 - 0.2;
        int range = (int) (size * suckRange);
        AABB axisAlignedBB = new AABB(position.offset(-range, -range, -range), position.offset(range, range, range));

        List<Entity> sucked = getCommandSenderWorld().getEntitiesOfClass(Entity.class, axisAlignedBB, SUCK_PREDICATE);

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
        AABB alignedBB = new AABB(position.offset(-nomrange, -nomrange, -nomrange), position.offset(nomrange, nomrange, nomrange));
        List<Entity> nommed = getCommandSenderWorld().getEntitiesOfClass(Entity.class, alignedBB, OMNOM_PREDICATE);

        for (Entity nommee : nommed) {
            if (nommee != this) {
                Vec3 nomedPos = nommee.getLookAngle();
                Vec3 diff = this.getLookAngle().subtract(nomedPos);

                double len = diff.length();

                if (len <= nomrange) {
                    if (nommee instanceof EnderDragon dragon) {
                        dragon.hurt(dragon.head, DamageSource.OUT_OF_WORLD, 5.0f);
                    }
                    nommee.hurt(DamageSource.OUT_OF_WORLD, 5.0f);
                }
            }
        }

        // every half second, SMASH STUFF
        if (age % 10 == 0) {
            Vec3 posFloor = this.position();

            int blockrange = (int) Math.round(nomrange);

            for (int y = -blockrange; y <= blockrange; y++) {
                for (int z = -blockrange; z <= blockrange; z++) {
                    for (int x = -blockrange; x <= blockrange; x++) {
                        Vec3 pos2 = new Vec3(x, y, z);
                        Vec3 rPos = posFloor.add(pos2);
                        BlockPos blockPos = new BlockPos(rPos);

                        if (blockPos.getY() < 0 || blockPos.getY() > 255) {
                            continue;
                        }

                        double dist = pos2.lengthSqr();
                        if (dist <= nomrange && !getCommandSenderWorld().getBlockState(blockPos).isAir()) {
                            BlockState state = getCommandSenderWorld().getBlockState(blockPos);
                            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(getCommandSenderWorld(), blockPos, state, fakePlayer);
                            MinecraftForge.EVENT_BUS.post(event);
                            if (!event.isCanceled()) {
                                float resist = state.getBlock().getExplosionResistance();
                                if (resist <= 10.0) {
                                    state.getBlock().canDropFromExplosion(state, getCommandSenderWorld(), blockPos, new Explosion(getCommandSenderWorld(), null, blockPos.getX(),
                                            blockPos.getY(), blockPos.getZ(), 6.0f, false, Explosion.BlockInteraction.DESTROY));
                                    getCommandSenderWorld().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_19883_) {
        return true;
    }
}
