package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityTridentItem;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class InfinityThrownTrident extends AbstractArrow implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Boolean> CHANNELING = SynchedEntityData.defineId(InfinityThrownTrident.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOCKWAVE = SynchedEntityData.defineId(InfinityThrownTrident.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RADIUS = SynchedEntityData.defineId(InfinityThrownTrident.class, EntityDataSerializers.INT);

    private static final Predicate<Entity> SLAY_MOB = entity -> !entity.isSpectator() && entity instanceof Enemy;
    private ItemStack tridentItem = new ItemStack(ModItems.infinity_trident.get());
    private boolean dealtDamage;
    private boolean noReturn;
    private final int loyaltyLevel = 5;
    public int returningTicks;

    public InfinityThrownTrident(EntityType<? extends InfinityThrownTrident> type, Level worldIn) {
        super(type, worldIn);
    }

    public InfinityThrownTrident(Level world, LivingEntity thrower, ItemStack thrownStackIn) {
        super(ModEntities.infinity_thrown_trident.get(), thrower, world);
        setStackAndLoyalty(thrownStackIn.copy());
    }


    private void setStackAndLoyalty(@NotNull ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof InfinityTridentItem trident) {
            tridentItem = stack;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOCKWAVE, false);
        this.entityData.define(CHANNELING, false);
        this.entityData.define(RADIUS, 1);
    }

    @Override
    public void tick() {
        if (inGroundTime > 4) {
            dealtDamage = true;
            noReturn = !isAcceptableReturnOwner();
        }
        if (!noReturn && (dealtDamage || isNoPhysics())) {
            Entity entity = getOwner();
            if (entity != null) {
                if (isAcceptableReturnOwner()) {
                    setNoPhysics(true);
                    Vec3 returnVector = entity.getEyePosition().subtract(position());
                    setPosRaw(getX(), getY() + returnVector.y * 0.015D * loyaltyLevel, getZ());
                    if (level().isClientSide) {
                        yOld = getY();
                    }
                    setDeltaMovement(getDeltaMovement().scale(0.95D).add(returnVector.normalize().scale(0.05D * loyaltyLevel)));
                    if (returningTicks == 0) {
                        playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                    }
                    ++this.returningTicks;
                } else {
                    if (!level().isClientSide && pickup == Pickup.ALLOWED) {
                        spawnAtLocation(getPickupItem(), 0.1F);
                    }
                    discard();
                }
            }
        }
        super.tick();
    }

    private boolean isAcceptableReturnOwner() {
        Entity entity = getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayer) || !entity.isSpectator();
        }
        return false;
    }

    @NotNull
    @Override
    protected ItemStack getPickupItem() {
        return this.tridentItem.copy();
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(@NotNull Vec3 startVec, @NotNull Vec3 endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        InfinityTridentItem trident = (InfinityTridentItem) tridentItem.getItem();
        float damage = Float.MAX_VALUE;
        Entity thrower = getOwner();
        DamageSource damagesource = damageSources().trident(this, thrower == null ? this : thrower);
        dealtDamage = true;
        if (hitEntity.hurt(damagesource, damage)) {
            //Vanilla's trident exits on endermen here, we allow hitting them instead
            if (hitEntity instanceof LivingEntity livingHit) {
                if (thrower instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingHit, thrower);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) thrower, livingHit);
                }
                this.doPostHurtEffects(livingHit);
            }
        }
        setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        float volume = 1.0F;
        SoundEvent sound = SoundEvents.TRIDENT_HIT;
        AABB area = new AABB(hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), hitEntity.getX(), hitEntity.getY(), hitEntity.getZ()).inflate(this.entityData.get(RADIUS));
        List<Mob> mobs = this.entityData.get(SHOCKWAVE) ? this.getCommandSenderWorld().getEntitiesOfClass(Mob.class, area) : Collections.emptyList();

        if (thrower instanceof Player && this.entityData.get(SHOCKWAVE)) {
            mobs.forEach(mobEntity -> {
                mobEntity.hurt(mobEntity.damageSources().playerAttack((Player) thrower), damage);
            });
            this.getCommandSenderWorld().getEntitiesOfClass(ItemEntity.class, area.inflate(1)).forEach(itemEntity -> {
                itemEntity.setNoPickUpDelay();
                itemEntity.teleportTo(thrower.blockPosition().getX(), thrower.blockPosition().getY() + 1, thrower.blockPosition().getZ());
            });
            this.getCommandSenderWorld().getEntitiesOfClass(ExperienceOrb.class, area.inflate(1))
                    .forEach(entityXPOrb ->
                            entityXPOrb.teleportTo(thrower.blockPosition().getX(), thrower.blockPosition().getY(), thrower.blockPosition().getZ()));
        }
        if (this.level() instanceof ServerLevel && this.entityData.get(CHANNELING)) {
            BlockPos blockpos = hitEntity.blockPosition();
            if (this.level().canSeeSky(blockpos)) {
                ToolUtils.trySummonLightning(level(), 1, blockpos,
                        thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
                sound = SoundEvents.TRIDENT_THUNDER;
                volume = 5.0F;
                mobs.forEach(mobEntity -> {
                    if (this.level().canSeeSky(mobEntity.blockPosition())) {
                        ToolUtils.trySummonLightning(level(), 1, mobEntity.blockPosition(),
                                thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
                    }
                });
            }
        }
        playSound(sound, volume, 1.0F);
    }

    @Override//onBlockHit
    protected void onHitBlock(BlockHitResult result) {
        float damage = Float.MAX_VALUE;
        lastState = level().getBlockState(result.getBlockPos());
        lastState.onProjectileHit(level(), lastState, result, this);
        Vec3 motion = result.getLocation().subtract(getX(), getY(), getZ());
        setDeltaMovement(motion);
        Vec3 vec3d1 = motion.normalize().scale(0.05F);
        setPosRaw(getX() - vec3d1.x, getY() - vec3d1.y, getZ() - vec3d1.z);
        //Vanilla Copy end

        SoundEvent sound = getHitGroundSoundEvent();
        float volume = 1.0F;
        float pitch = 1.2F / (random.nextFloat() * 0.2F + 0.9F);

        BlockPos hitPosition = result.getBlockPos();
        Entity thrower = getOwner();
        //If we hit a block try
        InfinityTridentItem trident = (InfinityTridentItem) tridentItem.getItem();

        if (this.level() instanceof ServerLevel && this.entityData.get(CHANNELING)) {
            if (ToolUtils.trySummonLightning(level(),  1, hitPosition.above(), thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null)) {
                sound = SoundEvents.TRIDENT_THUNDER;
                volume = 5.0F;
                pitch = 1.0F;
            }
        }

        AABB area = new AABB(hitPosition.getX(), hitPosition.getY(), hitPosition.getZ(), hitPosition.getX(), hitPosition.getY(), hitPosition.getZ()).inflate(this.entityData.get(RADIUS));
        List<Mob> mobs = this.entityData.get(SHOCKWAVE) ? this.getCommandSenderWorld().getEntitiesOfClass(Mob.class, area) : Collections.emptyList();
        if (thrower instanceof Player && this.entityData.get(SHOCKWAVE)) {
            mobs.forEach(mobEntity -> {
                mobEntity.hurt(mobEntity.damageSources().playerAttack((Player) thrower), damage);
            });
            this.getCommandSenderWorld().getEntitiesOfClass(ItemEntity.class, area.inflate(1)).forEach(itemEntity -> {
                itemEntity.setNoPickUpDelay();
                itemEntity.teleportTo(thrower.blockPosition().getX(), thrower.blockPosition().getY() + 1, thrower.blockPosition().getZ());
            });
            this.getCommandSenderWorld().getEntitiesOfClass(ExperienceOrb.class, area.inflate(1))
                    .forEach(entityXPOrb ->
                            entityXPOrb.teleportTo(thrower.blockPosition().getX(), thrower.blockPosition().getY(), thrower.blockPosition().getZ()));
        }

        if (this.level() instanceof ServerLevel && this.entityData.get(CHANNELING)) {
            if (this.level().canSeeSky(hitPosition)) {
                ToolUtils.trySummonLightning(level(), 1, hitPosition,
                        thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
                sound = SoundEvents.TRIDENT_THUNDER;
                volume = 5.0F;
                mobs.forEach(mobEntity -> {
                    if (this.level().canSeeSky(mobEntity.blockPosition())) {
                        ToolUtils.trySummonLightning(level(), 1, mobEntity.blockPosition(),
                                thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
                    }
                });
            }
        }

        //Vanilla Copy continue
        playSound(sound, volume, pitch);
        inGround = true;
        shakeTime = 7;
        setCritArrow(false);
        setPierceLevel((byte) 0);
        setSoundEvent(SoundEvents.ARROW_HIT);
        setShotFromCrossbow(false);
        resetPiercedEntities();
    }

    private boolean tryCreateShockwave(int charge, float damage, @Nullable LivingEntity thrower) {
        if (level() instanceof ServerLevel serverLevel) {
            if (thrower != null) {
                DamageSource src = ModDamageTypes.causeRandomDamage(serverLevel, this, thrower);
                float damageToDo = damage + charge;
                int distance = charge + 1;
                for (Entity entity : serverLevel.getEntities(thrower, getBoundingBox().inflate(distance), SLAY_MOB)) {
                    entity.hurt(src, damageToDo);
                }
                AreaEffectCloud particle = new AreaEffectCloud(serverLevel, getX(), getY(), getZ());
                particle.setOwner(thrower);
                particle.setParticle(ParticleTypes.CRIT);
                particle.setRadius(distance);
                particle.setDuration(0);
                serverLevel.addFreshEntity(particle);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean tryPickup(@NotNull Player player) {
        return super.tryPickup(player) || isNoPhysics() && ownedBy(player) && player.getInventory().add(getPickupItem());
    }

    @NotNull
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(@NotNull Player entity) {
        if (ownedBy(entity) || getOwner() == null) {
            super.playerTouch(entity);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Trident", Tag.TAG_COMPOUND)) {
            setStackAndLoyalty(ItemStack.of(compound.getCompound("Trident")));
        }
        dealtDamage = compound.getBoolean("DealtDamage");
        noReturn = compound.getBoolean("NoReturn");
        //this.entityData.set(SHOCKWAVE, ((InfinityTridentItem) ModItems.infinity_trident.get()).getCurrentLoyalty(thrownStack));
       // this.entityData.set(CHANNELING, ((InfinityTridentItem) ModItems.infinity_trident.get()).getCurrentChanneling(thrownStack));
        //this.entityData.set(RADIUS, ItemInfinity.getSelectedTier(thrownStack).getRadius());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("Trident", tridentItem.serializeNBT());
        compound.putBoolean("DealtDamage", dealtDamage);
        compound.putBoolean("NoReturn", noReturn);
    }

    @Override
    protected void tickDespawn() {
        if (this.pickup != Pickup.ALLOWED) {
            super.tickDespawn();
        } else if (noReturn && !level().isClientSide) {
            spawnAtLocation(getPickupItem(), 0.1F);
            discard();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F + 0.5F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeItem(tridentItem);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        setStackAndLoyalty(buffer.readItem());
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return tridentItem.copy();
    }
}