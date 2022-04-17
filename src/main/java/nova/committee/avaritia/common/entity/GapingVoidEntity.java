package nova.committee.avaritia.common.entity;

import com.google.common.base.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import nova.committee.avaritia.init.proxy.ServerProxy;

import java.util.Random;

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
            if (p.isCreative() && p.isFallFlying()) {
                return false;
            }
        }

        return true;
    };
    public static final Predicate<Entity> OMNOM_PREDICATE = input -> {
        if (!(input instanceof LivingEntity)) {
            return false;
        }

        if (input instanceof Player p) {
            if (p.isCreative()) {
                return false;
            }
        } else if (input instanceof ImmortalItemEntity) {
            return false;
        }

        return true;
    };
    public static double collapse = .95;
    public static double suckRange = 20.0;
    private static Random randy = new Random();
    private FakePlayer fakePlayer;

    public GapingVoidEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        setSharedFlagOnFire(true);
        noCulling = true;
        if (level instanceof ServerLevel) {
            fakePlayer = FakePlayerFactory.get((ServerLevel) level, ServerProxy.avaritiaFakePlayer);
        }
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
        if (level instanceof ServerLevel) {
            fakePlayer = FakePlayerFactory.get((ServerLevel) level, ServerProxy.avaritiaFakePlayer);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", getAge());

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return null;
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
