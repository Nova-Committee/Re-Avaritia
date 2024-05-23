package committee.nova.mods.avaritia.common.entity.arrow;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import io.github.fabricators_of_create.porting_lib.entity.PortingLibEntity;
import io.github.fabricators_of_create.porting_lib.entity.events.EntityEventFactory;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Name: Avaritia-forge / TraceArrowEntity
 * Author: cnlimiter
 * CreateTime: 2023/9/23 16:34
 * Description:
 */

public class TraceArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(TraceArrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPECTRAL_TIME = SynchedEntityData.defineId(TraceArrowEntity.class, EntityDataSerializers.INT);;
    private static final EntityDataAccessor<Integer> JUMP_COUNT = SynchedEntityData.defineId(TraceArrowEntity.class, EntityDataSerializers.INT);
    private Potion potion;
    private final Set<MobEffectInstance> effects;
    private boolean fixedColor;
    private LivingEntity homingTarget;
    private Vec3 seekOrigin;
    private int homingTime;
    private static final List<String> projectileAntiImmuneEntities = Lists.newArrayList("minecraft:enderman", "minecraft:wither", "minecraft:ender_dragon", "draconicevolution:guardian_wither");
    private final Entity owner = this.getOwner() == null ? this : this.getOwner();

    public TraceArrowEntity(EntityType<? extends AbstractArrow> entityType, Level world) {
        super(entityType, world);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
        this.homingTarget = null;
        this.seekOrigin = null;
        this.homingTime = 0;
    }

    public TraceArrowEntity(Level world, double xPos, double yPos, double zPos) {
        super(ModEntities.TRACE_ARROW.get(), xPos, yPos, zPos, world);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
        this.homingTarget = null;
        this.seekOrigin = null;
        this.homingTime = 0;
    }

    public TraceArrowEntity(Level world, LivingEntity shooter) {
        super(ModEntities.TRACE_ARROW.get(), shooter, world);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
        this.homingTarget = null;
        this.seekOrigin = null;
        this.homingTime = 0;
    }

    public void setSpectral(int spectralTime) {
        this.entityData.set(SPECTRAL_TIME, spectralTime);
    }

    public int getSpectralTime() {
        return this.entityData.get(SPECTRAL_TIME);
    }

    public void setJumpCount(int jumpCount) {
        this.entityData.set(JUMP_COUNT, jumpCount);
    }

    public int getJumpCount() {
        return this.entityData.get(JUMP_COUNT);
    }

    @Override
    public void tick() {
        this.updateHoming();
        this.superTick();
        if (this.level().isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
            }
        } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
            this.level().broadcastEntityEvent(this, (byte)0);
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }

    }

    private void superTick() {
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        if (!this.level().isClientSide) {
            this.setSharedFlag(6, this.isCurrentlyGlowing());
        }

        this.baseTick();
        boolean flag = this.isNoPhysics();
        Vec3 vector3d = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double f = vector3d.horizontalDistance();
            this.setYRot((float)(Mth.atan2(vector3d.x, vector3d.z) * 57.2957763671875D));
            this.setXRot((float)(Mth.atan2(vector3d.y, f) * 57.2957763671875D));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level().getBlockState(blockpos);
        Vec3 vector3d3;
        if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
            if (!voxelshape.isEmpty()) {
                vector3d3 = this.position();

                for (AABB axisalignedbb : voxelshape.toAabbs()) {
                    if (axisalignedbb.move(blockpos).contains(vector3d3)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            if (this.lastState != blockstate && this.shouldFall()) {
                this.startFalling();
            } else if (!this.level().isClientSide) {
                this.tickDespawn();
            }

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            Vec3 vector3d2 = this.position();
            vector3d3 = vector3d2.add(vector3d);
            HitResult raytraceresult = this.level().clip(new ClipContext(vector3d2, vector3d3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (raytraceresult.getType() != HitResult.Type.MISS) {
                vector3d3 = raytraceresult.getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityraytraceresult = this.findHitEntity(vector3d2, vector3d3);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult)raytraceresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                        raytraceresult = null;
                        entityraytraceresult = null;
                    }
                }

                if (raytraceresult != null && raytraceresult.getType() != HitResult.Type.MISS && !flag && !EntityEventFactory.onProjectileImpact(this, raytraceresult)) {
                    this.onHit(raytraceresult);
                    this.hasImpulse = true;
                }

                if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                raytraceresult = null;
            }

            vector3d = this.getDeltaMovement();
            double d3 = vector3d.x;
            double d4 = vector3d.y;
            double d0 = vector3d.z;
            if (this.isCritArrow()) {
                for(int i = 0; i < 4; ++i) {
                    this.level().addParticle(ParticleTypes.CRIT, this.getX() + d3 * (double)i / 4.0D, this.getY() + d4 * (double)i / 4.0D, this.getZ() + d0 * (double)i / 4.0D, -d3, -d4 + 0.2D, -d0);
                }
            }

            double d5 = this.getX() + d3;
            double d1 = this.getY() + d4;
            double d2 = this.getZ() + d0;
            double f1 = vector3d.horizontalDistance();
            if (flag) {
                this.setYRot((float)(Mth.atan2(-d3, -d0) * 57.2957763671875D));
            } else {
                this.setYRot((float)(Mth.atan2(d3, d0) * 57.2957763671875D));
            }

            this.setXRot((float)(Mth.atan2(d4, f1) * 57.2957763671875D));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f2 = 0.99F;
            float f3 = 0.05F;
            if (this.isInWater()) {
                for(int j = 0; j < 4; ++j) {
                    float f4 = 0.25F;
                    this.level().addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
                }

                f2 = this.getWaterInertia();
            }

            this.setDeltaMovement(vector3d.scale((double)f2));
            if (!this.isNoGravity() && !flag) {
                Vec3 vector3d4 = this.getDeltaMovement();
                this.setDeltaMovement(vector3d4.x, vector3d4.y - 0.05000000074505806D, vector3d4.z);
            }

            this.setPos(d5, d1, d2);
            this.checkInsideBlocks();
        }

    }

    @Override
    protected void onHitEntity(EntityHitResult p_213868_1_) {
        Entity entity = p_213868_1_.getEntity();
        float f = (float)this.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double)f * this.getBaseDamage(), 0.0D, 2.147483647E9D));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long j = this.random.nextInt(i / 2 + 2);
            i = (int)Math.min(j + (long)i, 2147483647L);
        }

        Entity owner = this.getOwner() == null ? this : this.getOwner();
        DamageSource damagesource = this.getDamageSource(entity);
        boolean isEnderman = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && !isEnderman) {
            entity.setSecondsOnFire(5);
        }

        if (entity instanceof Player player) {
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof ShieldItem) {
                player.getCooldowns().addCooldown(player.getUseItem().getItem(), 100);
                this.level().broadcastEntityEvent(player, (byte)30);
                player.stopUsingItem();
            }
        }

        if (entity.hurt(damagesource, (float)i)) {
            if (entity instanceof LivingEntity livingentity) {
                if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    Vec3 vector3d = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)this.knockback * 0.6D);
                    if (vector3d.lengthSqr() > 0.0D) {
                        livingentity.push(vector3d.x, 0.1D, vector3d.z);
                    }
                }

                if (!this.level().isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)owner, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer)owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level().isClientSide && owner instanceof ServerPlayer serverplayerentity) {
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, Arrays.asList(entity));
                    }
                }
            }

            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.0D));
                this.setPos(entity.position());
                this.seekNextTarget();
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
            }
        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.0D));
            this.setYRot(this.getYRot() + 180.0F);
            this.setPos(entity.position());
            this.yRotO += 180.0F;
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.seekNextTarget();
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
            }
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        this.lastState = this.level().getBlockState(hitResult.getBlockPos());
        BlockState blockstate = this.level().getBlockState(hitResult.getBlockPos());
        blockstate.onProjectileHit(this.level(), blockstate, hitResult, this);
        Vec3 vec3 = hitResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05000000074505806D);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.seekNextTarget();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
    }

    private DamageSource getDamageSource(Entity target) {
        Entity owner = this.getOwner();
        DamageSource damagesource;
        if (owner == null) {
            damagesource = target.damageSources().arrow(this, this);
        } else {
            damagesource = target.damageSources().arrow(this, owner);
            if (owner instanceof LivingEntity) {
                ((LivingEntity)owner).setLastHurtMob(target);
            }
        }

        if (projectileAntiImmuneEntities.contains(Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType())).toString())) {
            damagesource = ModDamageTypes.infinity(level(), owner);
        }

        return damagesource;
    }

    public void setEffectsFromItem(ItemStack p_184555_1_) {
        if (p_184555_1_.getItem() == Items.TIPPED_ARROW) {
            this.potion = PotionUtils.getPotion(p_184555_1_);
            Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(p_184555_1_);
            if (!collection.isEmpty()) {

                for (MobEffectInstance effectinstance : collection) {
                    this.effects.add(new MobEffectInstance(effectinstance));
                }
            }

            int i = getCustomColor(p_184555_1_);
            if (i == -1) {
                this.updateColor();
            } else {
                this.setFixedColor(i);
            }
        } else if (p_184555_1_.getItem() == Items.ARROW) {
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }

    }

    public static int getCustomColor(ItemStack p_191508_0_) {
        CompoundTag compoundnbt = p_191508_0_.getTag();
        return compoundnbt != null && compoundnbt.contains("CustomPotionColor", 99) ? compoundnbt.getInt("CustomPotionColor") : -1;
    }

    private void updateColor() {
        this.fixedColor = false;
        if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
            this.entityData.set(ID_EFFECT_COLOR, -1);
        } else {
            this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
        }

    }

    public void addEffect(MobEffectInstance p_184558_1_) {
        this.effects.add(p_184558_1_);
        this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_EFFECT_COLOR, -1);
        this.entityData.define(SPECTRAL_TIME, 0);
        this.entityData.define(JUMP_COUNT, 0);
    }

    private void makeParticle(int p_184556_1_) {
        int i = this.getColor();
        if (i != -1 && p_184556_1_ > 0) {
            double d0 = (double)(i >> 16 & 255) / 255.0D;
            double d1 = (double)(i >> 8 & 255) / 255.0D;
            double d2 = (double)(i & 255) / 255.0D;

            for(int j = 0; j < p_184556_1_; ++j) {
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
            }
        }

    }

    public int getColor() {
        return this.entityData.get(ID_EFFECT_COLOR);
    }

    private void setFixedColor(int p_191507_1_) {
        this.fixedColor = true;
        this.entityData.set(ID_EFFECT_COLOR, p_191507_1_);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.potion != Potions.EMPTY && this.potion != null) {
            compound.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
        }

        if (this.fixedColor) {
            compound.putInt("Color", this.getColor());
        }

        if (!this.effects.isEmpty()) {
            ListTag listnbt = new ListTag();

            for (MobEffectInstance effectinstance : this.effects) {
                listnbt.add(effectinstance.save(new CompoundTag()));
            }

            compound.put("CustomPotionEffects", listnbt);
        }

        if (this.getSpectralTime() > 0) {
            compound.putInt("spectral_time", this.entityData.get(SPECTRAL_TIME));
        }

        if (this.getJumpCount() > 0) {
            compound.putInt("jump_count", this.entityData.get(JUMP_COUNT));
        }

    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Potion", 8)) {
            this.potion = PotionUtils.getPotion(compound);
        }

        for (MobEffectInstance effectinstance : PotionUtils.getCustomEffects(compound)) {
            this.addEffect(effectinstance);
        }

        if (compound.contains("Color", 99)) {
            this.setFixedColor(compound.getInt("Color"));
        } else {
            this.updateColor();
        }

        if (compound.contains("spectral_time")) {
            this.setSpectral(compound.getInt("spectral_time"));
        }

        if (compound.contains("jump_count")) {
            this.setJumpCount(compound.getInt("jump_count"));
        }

    }

    @Override
    protected void doPostHurtEffects(@NotNull LivingEntity livingEntity) {
        super.doPostHurtEffects(livingEntity);
        Iterator<MobEffectInstance> var2 = this.potion.getEffects().iterator();

        MobEffectInstance effectinstance;
        while(var2.hasNext()) {
            effectinstance = var2.next();
            livingEntity.addEffect(new MobEffectInstance(effectinstance.getEffect(), Math.max(effectinstance.getDuration() / 8, 1), effectinstance.getAmplifier(), effectinstance.isAmbient(), effectinstance.isVisible()));
        }

        if (!this.effects.isEmpty()) {
            var2 = this.effects.iterator();

            while(var2.hasNext()) {
                effectinstance = var2.next();
                livingEntity.addEffect(effectinstance);
            }
        }

        int spectralTime = (Integer)this.entityData.get(SPECTRAL_TIME);
        if (spectralTime > 0) {
            effectinstance = new MobEffectInstance(MobEffects.GLOWING, spectralTime, 0);
            livingEntity.addEffect(effectinstance);
        }

    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
            return new ItemStack(Items.ARROW);
        } else {
            ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.setPotion(itemstack, this.potion);
            PotionUtils.setCustomEffects(itemstack, this.effects);
            if (this.fixedColor) {
                itemstack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
            }

            return itemstack;
        }
    }

    @Environment(EnvType.CLIENT)
    public void handleEntityEvent(byte p_70103_1_) {
        if (p_70103_1_ == 0) {
            int i = this.getColor();
            if (i != -1) {
                double d0 = (double)(i >> 16 & 255) / 255.0D;
                double d1 = (double)(i >> 8 & 255) / 255.0D;
                double d2 = (double)(i & 255) / 255.0D;

                for(int j = 0; j < 20; ++j) {
                    this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
                }
            }
        } else {
            super.handleEntityEvent(p_70103_1_);
        }

    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return PortingLibEntity.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.ON_FIRE) || super.isInvulnerableTo(source);
    }

    public void seekNextTarget() {
        if (this.getJumpCount() <= 16 && this.isCritArrow()) {
            if (this.seekOrigin == null) {
                this.seekOrigin = this.position();
            }

            if (!this.level().isClientSide) {
                TargetingConditions conditions = TargetingConditions.forCombat().selector((living) -> living.hasLineOfSight(this));
                this.homingTarget = this.level().getNearestEntity(LivingEntity.class, conditions, owner instanceof LivingEntity ? (LivingEntity)owner : null, this.seekOrigin.x, this.seekOrigin.y, this.seekOrigin.z, this.getBoundingBox().inflate(64.0D));
                if (this.homingTarget != null) {
                    Vec3 targetPos = this.homingTarget.getEyePosition();
                    double x = targetPos.x - this.getX();
                    double y = targetPos.y - this.getY();
                    double z = targetPos.z - this.getZ();
                    this.shoot(x, y, z, 3.0F, 0.0F);
                    this.setJumpCount(this.getJumpCount() + 1);
                    this.homingTime = 0;
                } else {
                    this.destroyArrow();
                }

            }
        } else {
            this.destroyArrow();
        }
    }

    private void updateHoming() {
        if (this.homingTarget != null) {
            if (this.homingTime++ > 60) {
                this.destroyArrow();
            } else if (!this.homingTarget.isDeadOrDying() && !this.homingTarget.isRemoved()) {
                Vec3 targetPos = this.homingTarget.getEyePosition();
                if (targetPos.distanceToSqr(this.position()) >= 4.0D) {
                    double x = targetPos.x - this.getX();
                    double y = targetPos.y - this.getY();
                    double z = targetPos.z - this.getZ();
                    this.shoot(x, y, z, 3.0F, 0.0F);
                    this.hasImpulse = true;
                }
            } else {
                this.homingTarget = null;
                this.seekNextTarget();
            }
        }
    }

    private void destroyArrow() {
        Level level1 = this.level();
        if (!level1.isClientSide) {
            if (level1 instanceof ServerLevel level) {
                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F, 0.0F, 4.0F, 10);

                if (owner instanceof ServerPlayer player){
                    player.connection.send(packet);
                }
                level.explode(this.getOwner() == null ? this : this.getOwner(), this.getX(), this.getY(), this.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
            }

            this.discard();
        }
        else {
            level1.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F, 0.0F );
        }
    }

}
